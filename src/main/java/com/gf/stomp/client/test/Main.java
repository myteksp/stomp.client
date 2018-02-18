package com.gf.stomp.client.test;


import com.gf.stomp.client.Client;
import com.gf.stomp.client.connection.protocol.StompClient;
import com.gf.stomp.client.log.Log;
import com.gf.stomp.client.log.LogEvent.Type;

public final class Main {
	public static void main(final String[] args) {
		Log.setLogLevel(Type.i);
		StompClient mStompClient = Client.create("wss://athena-backend-api-rest.herokuapp.com/broadcast");
		mStompClient.connect();
		mStompClient.topic("/event/tick").subscribe(topicMessage -> {
			Log.i("test", topicMessage.getPayload());
		});
	}
}
