package com.gf.stomp.client.connection;

import io.reactivex.Flowable;
import okhttp3.OkHttpClient;

public interface ConnectionProvider {
	OkHttpClient getHttpClient();
    Flowable<String> messages();
    Flowable<Void> send(String stompMessage);
    Flowable<LifecycleEvent> getLifecycleReceiver();
}
