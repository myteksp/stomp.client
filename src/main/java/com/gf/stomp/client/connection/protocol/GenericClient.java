package com.gf.stomp.client.connection.protocol;

import java.util.List;
import java.util.function.Consumer;

import com.gf.stomp.client.connection.StompHeader;

import okhttp3.OkHttpClient;


public interface GenericClient {
	OkHttpClient getHttpClient();
	void scheduleReconnect();
	void connect();
	void connect(final List<StompHeader> headers);
	void disconnect();
	void send(String destination);
	void send(String destination, String data);
	void topic(String destinationPath, final Consumer<StompMessage> consumer);
	boolean isConnected();
	boolean isConnecting();
}
