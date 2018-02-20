package com.gf.stomp.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import com.gf.stomp.client.connection.ConnectionProvider;
import com.gf.stomp.client.connection.OkHttpConnectionProvider;
import com.gf.stomp.client.connection.protocol.ClientImpl;
import com.gf.stomp.client.connection.protocol.GenericClient;
import com.gf.stomp.client.connection.protocol.StompClient;

import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

public final class Client {
	public static final GenericClient create(final String url) {
		return create(url, new HashMap<String, String>());
	}
	public static final GenericClient create(
			final String uri, 
			final Map<String, String> connectHttpHeaders) {
		return create(uri, connectHttpHeaders, 5);
	}
	public static final GenericClient create(
			final String uri, 
			final Map<String, String> connectHttpHeaders,
			final long ws_ping) {
		final String url = validateUrl(normalizeUrl(uri));
		return new ClientImpl(
				getStompClient(url, connectHttpHeaders, ws_ping), 
				connectHttpHeaders, 
				url, ws_ping);
	}
	public static final StompClient getStompClient(
			final String url, 
			final Map<String, String> connectHttpHeaders,
			final long ws_ping) {
		return new StompClient(getProvider(createClient(ws_ping), url, connectHttpHeaders));
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
	private static final OkHttpClient createClient(final long ws_ping) {
		return new Builder()
				.followRedirects(true)
				.followSslRedirects(true)
				.pingInterval(ws_ping, TimeUnit.SECONDS)
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