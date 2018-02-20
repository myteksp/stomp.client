package com.gf.stomp.client;

import java.util.function.Consumer;

import com.gf.stomp.client.connection.protocol.ClientStatelistener;
import com.gf.stomp.client.connection.protocol.GenericClient;
import com.gf.stomp.client.connection.protocol.StompMessage;
import com.gf.stomp.client.log.Log;
import com.gf.stomp.client.log.LogEvent.Type;

public final class Main {

	public static void main(String[] args) {
		Log.setLogLevel(Type.v);
		final GenericClient client = Client.create("https://athena-backend-api-rest.herokuapp.com/broadcast");
		client.connect();
		client.addClientStateListener(new ClientStatelistener() {
			
			@Override
			public void onUnplannedReconnectionStarted() {
				System.out.println("Unplanned: reconnecting");
			}
			
			@Override
			public void onUnplannedReconnectionCompleted() {
				System.out.println("Unplanned: connected");
			}
			
			@Override
			public void onPlannedReconnectionStart() {
				System.out.println("Planned: reconnecting");
			}
			
			@Override
			public void onPlannedReconnectionCompleted() {
				System.out.println("Planned: connected");
			}
		});
		client.topic("/event/tick", new Consumer<StompMessage>() {
			@Override
			public void accept(StompMessage message) {
				System.out.println("TICK: " + message.getPayload());
			}
		});
		client.topic("/event/general", new Consumer<StompMessage>() {
			@Override
			public void accept(StompMessage message) {
				System.out.println("GENERAL: " + message.getPayload());
			}
		});
		client.topic("/event/feed", new Consumer<StompMessage>() {
			@Override
			public void accept(StompMessage message) {
				System.out.println("FEED: " + message.getPayload());
			}
		});
		client.topic("/event/score", new Consumer<StompMessage>() {
			@Override
			public void accept(StompMessage message) {
				System.out.println("SCORE: " + message.getPayload());
			}
		});
		client.topic("/event/bets_update", new Consumer<StompMessage>() {
			@Override
			public void accept(StompMessage message) {
				System.out.println("BETS_UPADTE: " + message.getPayload());
			}
		});
		client.topic("/event/game_started", new Consumer<StompMessage>() {
			@Override
			public void accept(StompMessage message) {
				System.out.println("START: " + message.getPayload());
			}
		});
		client.topic("/event/game_ended", new Consumer<StompMessage>() {
			@Override
			public void accept(StompMessage message) {
				System.out.println("END: " + message.getPayload());
			}
		});
		client.topic("/event/stats", new Consumer<StompMessage>() {
			@Override
			public void accept(StompMessage message) {
				System.out.println("STATS: " + message.getPayload());
			}
		});
	}

}
