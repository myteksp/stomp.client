package com.gf.stomp.client.connection.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.gf.stomp.client.Client;
import com.gf.stomp.client.connection.LifecycleEvent;
import com.gf.stomp.client.connection.StompHeader;
import com.gf.stomp.client.connection.protocol.StompClient.OnConnectedListener;
import com.gf.stomp.client.log.Log;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;

public final class ClientImpl implements GenericClient{
	private static final String TAG = "ClientImpl";

	private volatile StompClient cl;
	private final AtomicBoolean isActive;
	private volatile List<StompHeader> headers;
	private final ConcurrentHashMap<String, Consumer<StompMessage>> consumers;
	private final Map<String, String> connectHttpHeaders;
	private final String url;

	public ClientImpl(
			final StompClient cl, 
			final Map<String, String> connectHttpHeaders, 
			final String url) {
		this.url = url;
		this.connectHttpHeaders = connectHttpHeaders;
		this.consumers = new ConcurrentHashMap<String, Consumer<StompMessage>>();
		isActive = new AtomicBoolean(false);
		this.cl = subscribe(cl, connectHttpHeaders, url);
	}

	private final StompClient subscribe(final StompClient cl, final Map<String, String> connectHttpHeaders, final String url) {
		final List<Disposable> subscriptions = new ArrayList<Disposable>(25);
		for(final Entry<String, Consumer<StompMessage>> e : consumers.entrySet()) {
			final Disposable sub = cl.topic(e.getKey()).subscribe(new io.reactivex.functions.Consumer<StompMessage>() {
				private final Consumer<StompMessage> cs = e.getValue();
				@Override
				public final void accept(final StompMessage t) throws Exception {
					cs.accept(t);
				}
			});
			subscriptions.add(sub);
		}
		cl
		.lifecycle()
		.observeOn(Schedulers.io()).subscribe(new io.reactivex.functions.Consumer<LifecycleEvent>() {
			@Override
			public final void accept(final LifecycleEvent e) throws Exception {
				if (isActive.get()) {
					final StompClient prev = ClientImpl.this.cl;
					switch(e.getType()) {
					case CLOSED:
						Log.d(TAG, "Re-connectiong due to 'socket-closed' event.");
						ClientImpl.this.cl = subscribe(Client.getStompClient(url, connectHttpHeaders), connectHttpHeaders, url);
						ClientImpl.this.cl.connect(headers);
						prev.disconnect();
						for(final Disposable s : subscriptions) {
							try{s.dispose();}catch(final Throwable t) {}
						}
						subscriptions.clear();
						break;
					case ERROR:
						Log.d(TAG, "Re-connectiong due to error.", e.getException());
						ClientImpl.this.cl = subscribe(Client.getStompClient(url, connectHttpHeaders), connectHttpHeaders, url);
						ClientImpl.this.cl.connect(headers);
						prev.disconnect();
						for(final Disposable s : subscriptions) {
							try{s.dispose();}catch(final Throwable t) {}
						}
						subscriptions.clear();
						break;
					default:
						return;
					}
				}
			}
		});

		return cl;
	}

	@Override
	public final void scheduleReconnect() {
		if (isActive.get()) {
			final StompClient prev = cl;
			Log.d(TAG, "Re-connectiong schduled.");
			cl = subscribe(Client.getStompClient(url, connectHttpHeaders), connectHttpHeaders, url);
			final AtomicBoolean gotConnected = new AtomicBoolean(false);
			cl.addOnConnectedListener(new OnConnectedListener() {
				@Override
				public final void onConnected() {
					gotConnected.set(true);
					prev.disconnect();
				}
			});
			cl.connect(headers);
			Flowable.timer(3, TimeUnit.SECONDS, Schedulers.io())
			.subscribe(new io.reactivex.functions.Consumer<Long>() {
				@Override
				public final void accept(final Long l) throws Exception {
					if (!gotConnected.get()) {
						prev.disconnect();
					}
				}
			});
		}

	}

	@Override
	public final boolean isConnecting() {
		if (isActive.get()) {
			return cl.isConnecting();
		}else {
			return false;
		}
	}
	@Override
	public final boolean isConnected() {
		return isActive.get();
	}
	@Override
	public final void disconnect() {
		if (isActive.getAndSet(false)) {
			cl.disconnect();
		}
	}
	@Override
	public final void connect() {
		connect(null);
	}
	@Override
	public final void connect(final List<StompHeader> headers) {
		if (!isActive.getAndSet(true)) {
			this.headers = headers;
			cl.connect(headers);
		}
	}

	//=====as is
	@Override
	public final void topic(final String destinationPath, final Consumer<StompMessage> consumer) {
		this.consumers.put(destinationPath, consumer);
		cl.topic(destinationPath).subscribe(new io.reactivex.functions.Consumer<StompMessage>() {
			@Override
			public final void accept(final StompMessage m) throws Exception {
				consumer.accept(m);
			}
		});
	}
	@Override
	public final void send(final String destination, final String data) {
		cl.send(destination, data);
	}
	@Override
	public final void send(final String destination) {
		cl.send(destination);
	}

	@Override
	public final OkHttpClient getHttpClient() {
		return cl.getHttpClient();
	}
}
