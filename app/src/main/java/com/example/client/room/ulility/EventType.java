package com.example.client.room.ulility;

public enum EventType {
    NULL(0),
    RECEIVE_REQUEST(1),
    SEND_RESPONSE(2),
    APP_ERROR(3),
    APP_WARNING(4),
    APP_INFO(5),
    APP_DEBUG(6);

    private final int value;

    private EventType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    private static EventType[] types = new EventType[]{
            NULL, RECEIVE_REQUEST, SEND_RESPONSE, APP_ERROR, APP_WARNING, APP_INFO, APP_DEBUG
    };

    public static EventType fromInt(int value) {
        return types[value];
    }
}
