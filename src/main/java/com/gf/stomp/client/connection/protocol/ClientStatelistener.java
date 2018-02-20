package com.gf.stomp.client.connection.protocol;

public interface ClientStatelistener {
	public void onPlannedReconnectionStart();
	public void onPlannedReconnectionCompleted();
	public void onUnplannedReconnectionStarted();
	public void onUnplannedReconnectionCompleted();
}
