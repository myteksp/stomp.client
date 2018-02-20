package com.gf.stomp.client.connection.protocol;

public interface ClientStatelistener {
	public void onReconnectionStarted();
	public void onReconnectionCompleted();
}
