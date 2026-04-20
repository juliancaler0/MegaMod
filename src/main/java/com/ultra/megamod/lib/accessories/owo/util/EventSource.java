package com.ultra.megamod.lib.accessories.owo.util;

public class EventSource<T> {

    private final EventStream<T> stream;

    protected EventSource(EventStream<T> stream) {
        this.stream = stream;
    }

    public Subscription subscribe(T subscriber) {
        this.stream.addSubscriber(subscriber);
        return new Subscription(subscriber);
    }

    public class Subscription implements EventStream.EventSubscription {
        protected final T subscriber;

        public Subscription(T subscriber) {
            this.subscriber = subscriber;
        }

        public void cancel() {
            EventSource.this.stream.removeSubscriber(this.subscriber);
        }
    }
}
