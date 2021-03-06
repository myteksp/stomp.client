package com.gf.stomp.client;

import java.util.function.Consumer;

import com.gf.stomp.client.connection.protocol.ClientStatelistener;
import com.gf.stomp.client.connection.protocol.GenericClient;
import com.gf.stomp.client.connection.protocol.StompMessage;
import com.gf.stomp.client.log.Log;
import com.gf.stomp.client.log.LogEvent.Type;

import io.reactivex.schedulers.Schedulers;

public final class Main {

	public static void main(String[] args) {
		Log.setLogLevel(Type.v);
		final GenericClient client = Client.create("https://athena-backend-api-rest.herokuapp.com/broadcast", Schedulers.io());
		client.connect();
		client.addClientStateListener(new ClientStatelistener() {
			
			@Override
			public void onReconnectionStarted() {
				System.out.println("Unplanned: reconnecting");
			}
			
			@Override
			public void onReconnectionCompleted() {
				System.out.println("Unplanned: connected");
			}
		});
		client.topic("/event/game_resumed", new Consumer<StompMessage>() {
			@Override
			public void accept(StompMessage message) {
				System.out.println("RESUMED: " + message.getPayload());
			}
		});
		client.topic("/event/game_paused", new Consumer<StompMessage>() {
			@Override
			public void accept(StompMessage message) {
				System.out.println("PAUSED: " + message.getPayload());
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
				//System.out.println("STATS: " + message.getPayload());
			}
		});
	}

}
