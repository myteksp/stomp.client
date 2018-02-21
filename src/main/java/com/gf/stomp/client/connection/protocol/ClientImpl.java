package com.gf.stomp.client.connection.protocol;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.gf.stomp.client.Client;
import com.gf.stomp.client.connection.LifecycleEvent;
import com.gf.stomp.client.connection.StompHeader;
import com.gf.stomp.client.connection.protocol.StompClient.OnConnectedListener;
import com.gf.stomp.client.log.Log;

import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import okhttp3.OkHttpClient;

public final class ClientImpl implements GenericClient{
	private static final String TAG = "ClientImpl";

	private volatile StompClient cl;
	private final AtomicBoolean isActive;
	private volatile List<StompHeader> headers;
	private final Map<String, Consumer<StompMessage>> consumers;
	private final Queue<ClientStatelistener> stateListener;
	private final Scheduler scheduler;

	public ClientImpl(
			final StompClient cl, 
			final Map<String, String> connectHttpHeaders, 
			final String url, 
			final long ws_ping,
			final Scheduler scheduler) {
		this.scheduler = scheduler;
		stateListener = new ConcurrentLinkedQueue<ClientStatelistener>();
		this.consumers = new ConcurrentHashMap<String, Consumer<StompMessage>>();
		isActive = new AtomicBoolean(false);
		this.cl = subscribe(cl, connectHttpHeaders, url, ws_ping);
	}

	@Override
	public final void addClientStateListener(final ClientStatelistener listener) {
		stateListener.add(listener);
	}

	private final StompClient subscribe(final StompClient cl, final Map<String, String> connectHttpHeaders, final String url, final long ws_ping) {
		final Queue<Disposable> subs = new ConcurrentLinkedQueue<Disposable>();
		for(final Entry<String, Consumer<StompMessage>> e : consumers.entrySet()) {
			final Disposable sub = cl.topic(e.getKey()).subscribe(new io.reactivex.functions.Consumer<StompMessage>() {
				private final Consumer<StompMessage> cs = e.getValue();
				@Override
				public final void accept(final StompMessage t) throws Exception {
					cs.accept(t);
				}
			});
			subs.add(sub);
		}
		cl
		.lifecycle()
		.observeOn(scheduler)
		.subscribe(new io.reactivex.functions.Consumer<LifecycleEvent>() {
			private final Queue<Disposable> subscriptions = subs;
			@Override
			public final void accept(final LifecycleEvent e) throws Exception {
				if (isActive.get()) {
					final StompClient prev = ClientImpl.this.cl;
					switch(e.getType()) {
					case CLOSED:
						for(final ClientStatelistener l : stateListener)
							l.onReconnectionStarted();
						Log.d(TAG, "Re-connectiong due to 'socket-closed' event.");
						final StompClient next = subscribe(Client.getStompClient(url, connectHttpHeaders, ws_ping), connectHttpHeaders, url, ws_ping);
						next.addOnConnectedListener(new OnConnectedListener() {
							@Override
							public final void onConnected() {
								ClientImpl.this.cl = next;
								prev.disconnect();
								for(final Disposable s : subscriptions) {
									try{s.dispose();}catch(final Throwable t) {}
								}
								subscriptions.clear();
								for(final ClientStatelistener l : stateListener)
									l.onReconnectionCompleted();
								prev.disconnect();
							}
						});
						next.connect(headers);
						break;
					case ERROR:
						for(final ClientStatelistener l : stateListener)
							l.onReconnectionStarted();
						Log.d(TAG, "Re-connectiong due to error.", e.getException());
						final StompClient next1 = subscribe(Client.getStompClient(url, connectHttpHeaders, ws_ping), connectHttpHeaders, url, ws_ping);
						next1.addOnConnectedListener(new OnConnectedListener() {
							@Override
							public final void onConnected() {
								ClientImpl.this.cl = next1;
								prev.disconnect();
								for(final Disposable s : subscriptions) {
									try{s.dispose();}catch(final Throwable t) {}
								}
								subscriptions.clear();
								for(final ClientStatelistener l : stateListener)
									l.onReconnectionCompleted();
								prev.disconnect();
							}
						});
						next1.connect(headers);
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
			cl = null;
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