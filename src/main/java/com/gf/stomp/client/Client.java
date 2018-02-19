package com.gf.stomp.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


import com.gf.stomp.client.connection.ConnectionProvider;
import com.gf.stomp.client.connection.OkHttpConnectionProvider;
import com.gf.stomp.client.connection.StompHeader;
import com.gf.stomp.client.connection.protocol.ClientImpl;
import com.gf.stomp.client.connection.protocol.GenericClient;
import com.gf.stomp.client.connection.protocol.StompClient;
import com.gf.stomp.client.connection.protocol.StompMessage;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

public final class Client {
	public static final GenericClient create(final String url) {
		return create(url, new HashMap<String, String>());
	}
	public static final GenericClient create(final String url, final long intervalSeconds) {
		return create(url, new HashMap<String, String>(), intervalSeconds);
	}
	public static final GenericClient create(
			final String url, 
			final Map<String, String> connectHttpHeaders) {
		return create(url, connectHttpHeaders, 60);
	}
	public static final GenericClient create(
			final String uri, 
			final Map<String, String> connectHttpHeaders,
			final long intervalSeconds) {
		final String url = validateUrl(normalizeUrl(uri));
		if (intervalSeconds < 1) {
			return new ClientImpl(
					getStompClient(url, connectHttpHeaders), 
					connectHttpHeaders, 
					url);
		}else {
			return wrapInReconnector(
					new ClientImpl(
							getStompClient(url, connectHttpHeaders), 
							connectHttpHeaders, 
							url), 
					intervalSeconds);
		}
	}
	public static final StompClient getStompClient(
			final String url, 
			final Map<String, String> connectHttpHeaders) {
		return new StompClient(getProvider(createClient(), url, connectHttpHeaders));
	}
	
	
	private static final String validateUrl(final String url) {
		try {
			new URI(url);
			return url;
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	private static final String normalizeUrl(final String url) {
		if (url == null) 
			throw new NullPointerException("Url can not be null.");

		if (url.length() < 7)
			throw new RuntimeException("Invalid url: '" + url + "'");

		if (url.startsWith("wss:")) 
			return url;

		if (url.startsWith("ws:")) 
			return url;

		if (url.startsWith("https:"))
			return "wss:" + url.substring(6);

		if (url.startsWith("http:"))
			return "ws:" + url.substring(5);

		throw new RuntimeException("Unknown url format: '" + url + "'");
	}
	private static final GenericClient wrapInReconnector(final ClientImpl client, final long intervalSeconds) {
		return new GenericClient() {
			private final ClientImpl cl = client;
			private volatile Disposable interval = null;
			@Override
			public final void scheduleReconnect() {cl.scheduleReconnect();}
			@Override
			public final void topic(final String destinationPath, final Consumer<StompMessage> consumer) {cl.topic(destinationPath, consumer);}
			@Override
			public final void send(final String destination, final String data) {cl.send(destination, data);}
			@Override
			public final void send(final String destination) {cl.send(destination);}
			@Override
			public final boolean isConnecting() {return cl.isConnecting();}
			@Override
			public final boolean isConnected() {return cl.isConnected();}
			@Override
			public final void disconnect() {
				cl.disconnect();
				final Disposable in = interval;
				if (in != null) {
					interval = null;
					in.dispose();
				}
			}
			private final void scheduleTicker() {
				final Disposable in = interval;
				if (in != null) {
					interval = null;
					in.dispose();
				}
				interval = Flowable.interval(intervalSeconds, TimeUnit.SECONDS, Schedulers.io())
						.subscribe(new io.reactivex.functions.Consumer<Long>() {
							@Override
							public final void accept(final Long t) throws Exception {
								cl.scheduleReconnect();
							}
						});
			}
			@Override
			public final void connect(List<StompHeader> headers) {
				cl.connect(headers);
				scheduleTicker();
			}
			@Override
			public final void connect() {
				cl.connect();
				scheduleTicker();
			}
			@Override
			public final OkHttpClient getHttpClient() {
				return cl.getHttpClient();
			}
		};
	}
	private static final OkHttpClient createClient() {
		return new Builder()
				.followRedirects(true)
				.followSslRedirects(true)
				.pingInterval(10, TimeUnit.SECONDS)
				.retryOnConnectionFailure(true)
				.build();
	}
	private static final ConnectionProvider getProvider(
			final OkHttpClient okHttpClient, 
			final String url, 
			final Map<String, String> connectHttpHeaders) {
		return new OkHttpConnectionProvider(url, connectHttpHeaders, okHttpClient);
	}
}
