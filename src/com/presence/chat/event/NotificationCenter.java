package com.presence.chat.event;


import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class NotificationCenter {
    // Create a PublishSubject that will emit NotificationEvent objects
    private final PublishSubject<NotificationEvent> subject = PublishSubject.create();

    // Method to post a notification
    public void post(NotificationEvent event) {
        subject.onNext(event);
    }

    // Method to get the observable for subscribing to notifications
    public Observable<NotificationEvent> getObservable() {
        return subject;
    }
}
