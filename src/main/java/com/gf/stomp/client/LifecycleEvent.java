package com.gf.stomp.client;

import java.util.TreeMap;

public class LifecycleEvent {
    private final Type mType;
    //Nullable
    private Exception mException;
    //Nullable
    private String mMessage;

    private TreeMap<String, String> handshakeResponseHeaders = new TreeMap<>();

    public LifecycleEvent(Type type) {
        mType = type;
    }

    public LifecycleEvent(Type type, Exception exception) {
        mType = type;
        mException = exception;
    }

    public LifecycleEvent(Type type, String message) {
        mType = type;
        mMessage = message;
    }

    public Type getType() {
        return mType;
    }

    public Exception getException() {
        return mException;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setHandshakeResponseHeaders(TreeMap<String, String> handshakeResponseHeaders) {
        this.handshakeResponseHeaders = handshakeResponseHeaders;
    }

    public TreeMap<String, String> getHandshakeResponseHeaders() {
        return handshakeResponseHeaders;
    }
    
    //===========type===========
    public static enum Type {
        OPENED, CLOSED, ERROR
    }
}
