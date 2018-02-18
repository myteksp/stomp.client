package com.gf.stomp.client;

import java.util.HashMap;
import java.util.Map;

import com.gf.stomp.client.connection.ConnectionProvider;
import com.gf.stomp.client.connection.OkHttpConnectionProvider;
import com.gf.stomp.client.connection.protocol.StompClient;

import okhttp3.OkHttpClient;

public final class Client {
	public static final StompClient create(final String url) {
		return create(url, new HashMap<String, String>());
	}
	
	public static final StompClient create(final String url, final Map<String, String> connectHttpHeaders) {
		return new StompClient(getProvider(createClient(), url, connectHttpHeaders));
	}
	
	
	private static final OkHttpClient createClient() {
		return new OkHttpClient();
	}
	
	private static final ConnectionProvider getProvider(final OkHttpClient okHttpClient, final String url, final Map<String, String> connectHttpHeaders) {
		return new OkHttpConnectionProvider(url, connectHttpHeaders, okHttpClient);
	}
}
