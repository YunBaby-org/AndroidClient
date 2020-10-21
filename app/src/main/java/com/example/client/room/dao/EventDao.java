package com.example.client.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.client.room.entity.Event;
import com.example.client.room.ulility.EventType;

import java.util.Date;
import java.util.List;

@Dao
public interface EventDao {
    @Query("SELECT count(*) FROM Event WHERE type = :type AND time BETWEEN :since AND :until")
    int countItem(EventType type, Date since, Date until);

    @Query("SELECT * FROM Event LIMIT :limit")
    List<Event> getEvents(int limit);

    @Query("SELECT * FROM Event WHERE time >= :since LIMIT :limit")
    List<Event> getEventsSince(Date since, int limit);

    @Insert
    void insertAll(Event... events);

    @Delete
    void delete(Event event);
}
