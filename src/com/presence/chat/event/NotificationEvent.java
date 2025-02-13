package com.presence.chat.event;

public class NotificationEvent {
    private final String message;
    private final String sender;

    public NotificationEvent(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }
}
