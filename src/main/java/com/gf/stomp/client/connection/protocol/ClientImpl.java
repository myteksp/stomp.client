package com.gf.stomp.client.connection.protocol;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.gf.collections.GfCollection;
import com.gf.collections.GfCollections;
import com.gf.stomp.client.Client;
import com.gf.stomp.client.connection.StompHeader;
import com.gf.stomp.client.log.Log;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

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
		final GfCollection<Disposable> subscriptions = GfCollections
				.asArrayCollection(consumers.entrySet())
				.map(e->{
					return cl
							.topic(e.getKey())
							.subscribe(m->{
								e.getValue().accept(m);
							});
				});
		cl
		.lifecycle()
		.observeOn(Schedulers.io())
		.subscribe(e->{
			if (isActive.get()) {
				final StompClient prev = ClientImpl.this.cl;
				switch(e.getType()) {
				case CLOSED:
					Log.d(TAG, "Re-connectiong due to 'socket-closed' event.");
					ClientImpl.this.cl = subscribe(Client.getStompClient(url, connectHttpHeaders), connectHttpHeaders, url);
					ClientImpl.this.cl.connect(headers);
					prev.disconnect();
					subscriptions.forEach(s->{
						try{s.dispose();}catch(final Throwable t) {}
					});
					subscriptions.clear();
					break;
				case ERROR:
					Log.d(TAG, "Re-connectiong due to error.", e.getException());
					ClientImpl.this.cl = subscribe(Client.getStompClient(url, connectHttpHeaders), connectHttpHeaders, url);
					ClientImpl.this.cl.connect(headers);
					prev.disconnect();
					subscriptions.forEach(s->{
						try{s.dispose();}catch(final Throwable t) {}
					});
					subscriptions.clear();
					break;
				default:
					return;
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
			cl.addOnConnectedListener(()->{
				gotConnected.set(true);
				prev.disconnect();
			});
			cl.connect(headers);
			Flowable.timer(3, TimeUnit.SECONDS, Schedulers.io())
			.subscribe(l->{
				if (!gotConnected.get()) {
					prev.disconnect();
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
		cl.topic(destinationPath).subscribe(m->{
			consumer.accept(m);
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
}
