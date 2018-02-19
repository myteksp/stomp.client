package com.gf.stomp.client.connection.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import com.gf.stomp.client.connection.ConnectionProvider;
import com.gf.stomp.client.connection.LifecycleEvent;
import com.gf.stomp.client.connection.StompHeader;
import com.gf.stomp.client.log.Log;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import okhttp3.OkHttpClient;

public final class StompClient {
	private static final String TAG = StompClient.class.getSimpleName();

	public static final String SUPPORTED_VERSIONS = "1.1,1.0";
	public static final String DEFAULT_ACK = "auto";

	private Disposable mMessagesDisposable;
	private Disposable mLifecycleDisposable;
	private Map<String, Set<FlowableEmitter<? super StompMessage>>> mEmitters = new ConcurrentHashMap<String, Set<FlowableEmitter<? super StompMessage>>>();
	private List<ConnectableFlowable<Void>> mWaitConnectionFlowables;
	private final ConnectionProvider mConnectionProvider;
	private HashMap<String, String> mTopics;
	private boolean mConnected;
	private boolean isConnecting;
	private volatile ConcurrentLinkedQueue<OnConnectedListener> on_connectedListeners;

	public StompClient(ConnectionProvider connectionProvider) {
		mConnectionProvider = connectionProvider;
		mWaitConnectionFlowables = new CopyOnWriteArrayList<>();
		on_connectedListeners = new ConcurrentLinkedQueue<OnConnectedListener>();
	}
	
	public final OkHttpClient getHttpClient() {
		return mConnectionProvider.getHttpClient();
	}

	public final void addOnConnectedListener(final OnConnectedListener listener) {
		final ConcurrentLinkedQueue<OnConnectedListener> q = on_connectedListeners;
		if (q != null) {
			q.add(listener);
		}
	}

	private final void notifyOnConnected() {
		final ConcurrentLinkedQueue<OnConnectedListener> q = on_connectedListeners;
		on_connectedListeners = null;
		if (q != null) {
			for(final OnConnectedListener l : q) 
				try {l.onConnected();}catch(final Throwable t) {}

			q.clear();
		}
	}

	public final void connect() {
		connect(null);
	}

	public final void connect(boolean reconnect) {
		connect(null, reconnect);
	}

	public final void connect(final List<StompHeader> _headers) {
		connect(_headers, false);
	}

	public final void connect(final List<StompHeader> _headers, final boolean reconnect) {
		if (reconnect) disconnect();
		if (mConnected) return;
		mLifecycleDisposable = mConnectionProvider.getLifecycleReceiver()
				.subscribe(new Consumer<LifecycleEvent>() {
					@Override
					public final void accept(final LifecycleEvent lifecycleEvent) throws Exception {
						switch (lifecycleEvent.getType()) {
						case OPENED:
							final List<StompHeader> headers = new ArrayList<StompHeader>();
							headers.add(new StompHeader(StompHeader.VERSION, SUPPORTED_VERSIONS));
							if (_headers != null) headers.addAll(_headers);
							mConnectionProvider.send(new StompMessage(StompCommand.CONNECT, headers, null).compile())
							.subscribe();
							notifyOnConnected();
							break;

						case CLOSED:
							mConnected = false;
							isConnecting = false;
							break;

						case ERROR:
							mConnected = false;
							isConnecting = false;
							break;
						}
					}
				});

		isConnecting = true;
		mMessagesDisposable = mConnectionProvider.messages()
				.map(new Function<String, StompMessage>() {
					@Override
					public final StompMessage apply(final String t) throws Exception {
						return StompMessage.from(t);
					}
				})
				.subscribe(new Consumer<StompMessage>() {
					@Override
					public final void accept(final StompMessage stompMessage) throws Exception {
						if (stompMessage.getStompCommand().equals(StompCommand.CONNECTED)) {
							mConnected = true;
							isConnecting = false;
							for (ConnectableFlowable<Void> flowable : mWaitConnectionFlowables) 
								flowable.connect();

							mWaitConnectionFlowables.clear();
						}
						callSubscribers(stompMessage);
					}
				});
	}

	public final Flowable<Void> send(final String destination) {
		return send(new StompMessage(
				StompCommand.SEND,
				Collections.singletonList(new StompHeader(StompHeader.DESTINATION, destination)),
				null));
	}

	public final Flowable<Void> send(final String destination, String data) {
		return send(new StompMessage(
				StompCommand.SEND,
				Collections.singletonList(new StompHeader(StompHeader.DESTINATION, destination)),
				data));
	}

	public final Flowable<Void> send(final StompMessage stompMessage) {
		final Flowable<Void> flowable = mConnectionProvider.send(stompMessage.compile());
		if (!mConnected) {
			ConnectableFlowable<Void> deferred = flowable.publish();
			mWaitConnectionFlowables.add(deferred);
			return deferred;
		} else {
			return flowable;
		}
	}

	private final void callSubscribers(final StompMessage stompMessage) {
		final String messageDestination = stompMessage.findHeader(StompHeader.DESTINATION);
		for (String dest : mEmitters.keySet()) {
			if (dest.equals(messageDestination)) {
				for (FlowableEmitter<? super StompMessage> subscriber : mEmitters.get(dest)) {
					subscriber.onNext(stompMessage);
				}
				return;
			}
		}
	}

	public final Flowable<LifecycleEvent> lifecycle() {
		return mConnectionProvider.getLifecycleReceiver();
	}

	public final void disconnect() {
		if (mMessagesDisposable != null) mMessagesDisposable.dispose();
		if (mLifecycleDisposable != null) mLifecycleDisposable.dispose();
		mConnected = false;
	}

	public final Flowable<StompMessage> topic(final String destinationPath) {
		return topic(destinationPath, null);
	}

	public final Flowable<StompMessage> topic(
			final String destinationPath, 
			final List<StompHeader> headerList) {
		return Flowable.<StompMessage>create(new FlowableOnSubscribe<StompMessage>() {
			@Override
			public void subscribe(final FlowableEmitter<StompMessage> emitter) throws Exception {
				Set<FlowableEmitter<? super StompMessage>> emittersSet = mEmitters.get(destinationPath);
				if (emittersSet == null) {
					emittersSet = new HashSet<>();
					mEmitters.put(destinationPath, emittersSet);
					subscribePath(destinationPath, headerList).subscribe();
				}
				emittersSet.add(emitter);
			}
		},  BackpressureStrategy.BUFFER)
		.doOnCancel(new Action() {
			@Override
			public final void run() throws Exception {
				final Iterator<String> mapIterator = mEmitters.keySet().iterator();
				while (mapIterator.hasNext()) {
					final String destinationUrl = mapIterator.next();
					final Set<FlowableEmitter<? super StompMessage>> set = mEmitters.get(destinationUrl);
					if (set != null) {
						final Iterator<FlowableEmitter<? super StompMessage>> setIterator = set.iterator();
						while (setIterator.hasNext()) {
							final FlowableEmitter<? super StompMessage> subscriber = setIterator.next();
							if (subscriber.isCancelled()) {
								setIterator.remove();
								if (set.size() < 1) {
									mapIterator.remove();
									unsubscribePath(destinationUrl).subscribe();
								}
							}
						}
					}
				}
			}
		});
	}

	private final Flowable<Void> subscribePath(
			final String destinationPath, 
			final List<StompHeader> headerList) {
		if (destinationPath == null) return Flowable.empty();
		final String topicId = UUID.randomUUID().toString();

		if (mTopics == null) mTopics = new HashMap<>();
		mTopics.put(destinationPath, topicId);
		List<StompHeader> headers = new ArrayList<>();
		headers.add(new StompHeader(StompHeader.ID, topicId));
		headers.add(new StompHeader(StompHeader.DESTINATION, destinationPath));
		headers.add(new StompHeader(StompHeader.ACK, DEFAULT_ACK));
		if (headerList != null) headers.addAll(headerList);
		return send(
				new StompMessage(
						StompCommand.SUBSCRIBE,
						headers, 
						null));
	}


	private final Flowable<Void> unsubscribePath(final String dest) {
		final String topicId = mTopics.get(dest);
		Log.d(TAG, "Unsubscribe path: " + dest + " id: " + topicId);
		return send(
				new StompMessage(
						StompCommand.UNSUBSCRIBE,
						Collections.singletonList(
								new StompHeader(StompHeader.ID, topicId)), 
						null));
	}

	public final boolean isConnected() {
		return mConnected;
	}

	public final boolean isConnecting() {
		return isConnecting;
	}



	public static interface OnConnectedListener{
		void onConnected();
	}
}
