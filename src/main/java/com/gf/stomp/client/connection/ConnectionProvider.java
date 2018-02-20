package com.gf.stomp.client.connection;

import java.util.Map;

import io.reactivex.Flowable;
import okhttp3.OkHttpClient;

public interface ConnectionProvider {
	OkHttpClient getHttpClient();
    Flowable<String> messages();
    Flowable<Void> send(String stompMessage);
    Flowable<LifecycleEvent> getLifecycleReceiver();
    Map<String, String> getConnectionHeaders();
    String getUri();
}
