package com.gf.stomp.client.connection;

import java.util.TreeMap;

public final class LifecycleEvent {
	private final Type type;
	private final String message;
	private final Exception exception;
	private TreeMap<String, String> handshakeResponseHeaders;

	public LifecycleEvent(Type type) {
		this.type = type;
		this.message = null;
		this.exception = null;
		this.handshakeResponseHeaders = new TreeMap<String, String>();
	}

	public LifecycleEvent(final Type type, final Exception exception) {
		this.type = type;
		this.message = null;
		this.exception = exception;
		this.handshakeResponseHeaders = new TreeMap<String, String>();
	}

	public LifecycleEvent(final Type type, final String message) {
		this.type = type;
		this.message = message;
		this.exception = null;
		this.handshakeResponseHeaders = new TreeMap<String, String>();
	}

	public final Type getType() {
		return type;
	}

	public final Exception getException() {
		return exception;
	}

	public final String getMessage() {
		return message;
	}

	public final void setHandshakeResponseHeaders(final TreeMap<String, String> handshakeResponseHeaders) {
		this.handshakeResponseHeaders = handshakeResponseHeaders;
	}

	public final TreeMap<String, String> getHandshakeResponseHeaders() {
		return handshakeResponseHeaders;
	}

	//===========type===========
	public static enum Type {
		OPENED, CLOSED, ERROR
	}
}
