package com.example.client.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.client.room.ulility.EventType;

import java.util.Date;

@Entity
public class Event {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "time")
    public Date time;

    @ColumnInfo(name = "type")
    public EventType type;

    @ColumnInfo(name = "title")
    public int title;

    @ColumnInfo(name = "description")
    public String description;

    public Event() {
    }

    @Ignore
    public Event(EventType type, int title) {
        this.type = type;
        this.title = title;
        this.time = new Date();
    }

    @Ignore
    public Event(EventType type, int title, String description) {
        this.type = type;
        this.title = title;
        this.time = new Date();
        this.description = description;
    }

    public static Event debug(int title, String description) {
        return new Event(EventType.APP_DEBUG, title, description);
    }

    public static Event info(int title, String description) {
        return new Event(EventType.APP_INFO, title, description);
    }

    public static Event warning(int title, String description) {
        return new Event(EventType.APP_WARNING, title, description);
    }

    public static Event error(int title, String description) {
        return new Event(EventType.APP_ERROR, title, description);
    }

    public static Event request(int title, String description) {
        return new Event(EventType.RECEIVE_REQUEST, title, description);
    }

    public static Event response(int title, String description) {
        return new Event(EventType.SEND_RESPONSE, title, description);
    }

    public static Event debug(int title) {
        return new Event(EventType.APP_DEBUG, title, null);
    }

    public static Event info(int title) {
        return new Event(EventType.APP_INFO, title, null);
    }

    public static Event warning(int title) {
        return new Event(EventType.APP_WARNING, title, null);
    }

    public static Event error(int title) {
        return new Event(EventType.APP_ERROR, title, null);
    }

    public static Event request(int title) {
        return new Event(EventType.RECEIVE_REQUEST, title, null);
    }

    public static Event response(int title) {
        return new Event(EventType.SEND_RESPONSE, title, null);
    }
}
