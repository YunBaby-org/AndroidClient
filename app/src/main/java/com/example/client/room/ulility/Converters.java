package com.example.client.room.ulility;

import androidx.room.TypeConverter;

import com.example.client.room.entity.Event;

import java.util.Date;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static int fromEventType(EventType type) {
        return type == null ? 0 : type.getValue();
    }

    @TypeConverter
    public static EventType intToEventType(int value) {
        return EventType.fromInt(value);
    }

    @TypeConverter
    public static Event.ID fromInt(int Id) {
        return Event.ID.fromInt(Id);
    }

    @TypeConverter
    public static int toInt(Event.ID id) {
        return id.getValueID();
    }
}
