package com.example.client.room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.client.room.dao.EventDao;
import com.example.client.room.entity.Event;
import com.example.client.room.ulility.Converters;

@Database(entities = {Event.class}, version = 3)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract EventDao eventDao();

    private static AppDatabase instance;

    public static AppDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null)
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "app_database")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .build();
            }
        }
        return instance;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP Table Event");
            database.execSQL("CREATE TABLE Event(id INTEGER PRIMARY KEY NOT NULL, time INTEGER, type INTEGER, eventId INTEGER NOT NULL, description TEXT)");
        }
    };
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP Table Event");
            database.execSQL("CREATE TABLE Event(id INTEGER PRIMARY KEY NOT NULL, time INTEGER, type INTEGER, eventId INTEGER, description TEXT)");
        }
    };
}
