package com.gf.stomp.client.connection;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.gf.collections.GfCollection;
import com.gf.collections.GfCollections;
import com.gf.stomp.client.log.Log;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public final class OkHttpConnectionProvider implements ConnectionProvider{

	private static final String TAG = OkHttpConnectionProvider.class.getSimpleName();

	private final String mUri;
	private final Map<String, String> mConnectHttpHeaders;
	private final OkHttpClient mOkHttpClient;

	private final List<FlowableEmitter<? super LifecycleEvent>> mLifecycleEmitters;
	private final GfCollection<FlowableEmitter<? super String>> mMessagesEmitters;

	private WebSocket openedSocked;


	public OkHttpConnectionProvider(String uri, Map<String, String> connectHttpHeaders, OkHttpClient okHttpClient) {
		mUri = uri;
		mConnectHttpHeaders = connectHttpHeaders;
		mLifecycleEmitters = Collections.synchronizedList(GfCollections.asLinkedCollection());
		mMessagesEmitters = GfCollections.asLinkedCollection();
		mOkHttpClient = okHttpClient;
	}

	@Override
	public final Flowable<String> messages() {
		final Flowable<String> flowable = Flowable.<String>create(mMessagesEmitters::add, BackpressureStrategy.BUFFER)
				.doOnCancel(() -> {
					final Iterator<FlowableEmitter<? super String>> iterator = mMessagesEmitters.iterator();
					while (iterator.hasNext()) 
						if (iterator.next().isCancelled()) iterator.remove();

					if (mMessagesEmitters.size() < 1) {
						Log.d(TAG, "Close web socket connection now in thread " + Thread.currentThread());
						openedSocked.close(1000, "");
						openedSocked = null;
					}
				});
		createWebSocketConnection();
		return flowable;
	}

	private final void createWebSocketConnection() {
		if (openedSocked != null) 
			throw new IllegalStateException("Already have connection to web socket");

		final Request.Builder requestBuilder = new Request.Builder()
				.url(mUri);

		addConnectionHeadersToBuilder(requestBuilder, mConnectHttpHeaders);

		openedSocked = mOkHttpClient.newWebSocket(requestBuilder.build(),
				new WebSocketListener() {
			@Override
			public final void onOpen(final WebSocket webSocket, final Response response) {
				final LifecycleEvent openEvent = new LifecycleEvent(LifecycleEvent.Type.OPENED);

				final TreeMap<String, String> headersAsMap = headersAsMap(response);

				openEvent.setHandshakeResponseHeaders(headersAsMap);
				emitLifecycleEvent(openEvent);
			}
			@Override
			public final void onMessage(final WebSocket webSocket, final String text) {
				emitMessage(text);
			}
			@Override
			public final void onMessage(final WebSocket webSocket, final ByteString bytes) {
				emitMessage(bytes.utf8());
			}
			@Override
			public final void onClosed(final WebSocket webSocket, final int code, final String reason) {
				emitLifecycleEvent(new LifecycleEvent(LifecycleEvent.Type.CLOSED));
				openedSocked = null;
			}
			@Override
			public final void onFailure(final WebSocket webSocket, final Throwable t, final Response response) {
				emitLifecycleEvent(new LifecycleEvent(LifecycleEvent.Type.ERROR, new Exception(t)));
			}
			@Override
			public final void onClosing(final WebSocket webSocket, final int code, final String reason) {
				webSocket.close(code, reason);
			}
		});
	}

	@Override
	public final Flowable<Void> send(final String stompMessage) {
		return Flowable.create(subscriber -> {
			if (openedSocked == null) {
				subscriber.onError(new IllegalStateException("Not connected yet"));
			} else {
				Log.d(TAG, "Send STOMP message: " + stompMessage);
				openedSocked.send(stompMessage);
				subscriber.onComplete();
			}
		}, BackpressureStrategy.BUFFER);
	}

	@Override
	public final Flowable<LifecycleEvent> getLifecycleReceiver() {
		return Flowable.<LifecycleEvent>create(mLifecycleEmitters::add, BackpressureStrategy.BUFFER)
				.doOnCancel(() -> {
					synchronized (mLifecycleEmitters) {
						Iterator<FlowableEmitter<? super LifecycleEvent>> iterator = mLifecycleEmitters.iterator();
						while (iterator.hasNext()) {
							if (iterator.next().isCancelled()) iterator.remove();
						}
					}
				});
	}

	private final TreeMap<String, String> headersAsMap(final Response response) {
		final TreeMap<String, String> headersAsMap = new TreeMap<>();
		final Headers headers = response.headers();
		for (String key : headers.names()) 
			headersAsMap.put(key, headers.get(key));
		return headersAsMap;
	}

	private final void addConnectionHeadersToBuilder(
			final Request.Builder requestBuilder, 
			final Map<String, String> mConnectHttpHeaders) {
		for (Map.Entry<String, String> headerEntry : mConnectHttpHeaders.entrySet()) 
			requestBuilder.addHeader(headerEntry.getKey(), headerEntry.getValue());
	}

	private final void emitLifecycleEvent(final LifecycleEvent lifecycleEvent) {
		final Throwable ex = lifecycleEvent.getException();
		if (ex == null) {
			Log.d(TAG, "Emit lifecycle event: " + lifecycleEvent.getType().name());
			synchronized (mLifecycleEmitters) {
				for (FlowableEmitter<? super LifecycleEvent> subscriber : mLifecycleEmitters) 
					subscriber.onNext(lifecycleEvent);
			}
		} else {
			Log.e(TAG, "Emit lifecycle event: " + lifecycleEvent.getType().name(), ex);
			synchronized (mLifecycleEmitters) {
				for (FlowableEmitter<? super LifecycleEvent> subscriber : mLifecycleEmitters) 
					subscriber.onNext(lifecycleEvent);
			}
		}
	}

	private final void emitMessage(final String stompMessage) {
		Log.d(TAG, "Emit STOMP message: " + stompMessage);
		for (FlowableEmitter<? super String> subscriber : mMessagesEmitters) 
			subscriber.onNext(stompMessage);
	}
}
