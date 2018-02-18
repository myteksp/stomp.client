package com.gf.stomp.client.connection;

import io.reactivex.Flowable;

public interface ConnectionProvider {
    Flowable<String> messages();
    Flowable<Void> send(String stompMessage);
    Flowable<LifecycleEvent> getLifecycleReceiver();
}
