package com.gf.stomp.client.test;


import com.gf.stomp.client.Stomp;
import com.gf.stomp.client.cl.StompClient;
import com.gf.stomp.client.log.Log;

public final class Main {
	public static void main(final String[] args) {
		 StompClient mStompClient = Stomp.over(okhttp3.WebSocket.class, "wss://athena-backend-api-rest.herokuapp.com/broadcast");
		 mStompClient.connect();
		 mStompClient.topic("/event/tick").subscribe(topicMessage -> {
		     Log.d("test", topicMessage.getPayload());
		 });
	}
}
