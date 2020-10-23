package com.example.client.room.entity;

import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.client.R;
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

    @ColumnInfo(name = "eventId")
    public ID eventId;

    @ColumnInfo(name = "description")
    public String description;

    public Event() {
    }

    @Ignore
    public Event(EventType type, ID eventId) {
        this.type = type;
        this.eventId = eventId;
        this.time = new Date();
    }

    @Ignore
    public Event(EventType type, ID eventId, String description) {
        this.type = type;
        this.eventId = eventId;
        this.time = new Date();
        this.description = description;
    }

    public enum ID {
        /* 3 Step to add a new Event ID */

        /* Step1: Adding a new enum for the event at below */
        AUTO_REPORT_GPS(0),
        AUTO_REPORT_SCAN_WIFI(1),
        AUTO_REPORT_WIFI(2),
        START_SERVICE_FAILED(3),
        SERVICE_ENCOUNTER_FAILURE(4),
        ENABLE_AUTO_REPORT_GPS(5),
        DISABLE_AUTO_REPORT_GPS(6),
        ENABLE_AUTO_REPORT_WIFI(7),
        DISABLE_AUTO_REPORT_WIFI(8),
        UPDATE_GPS_REPORT_INTERVAL(9),
        UPDATE_WIFI_REPORT_INTERVAL(10),
        STOP_SERVICE(11),
        RECEIVE_REQUEST(12),
        ENABLE_SERVICE(13);

        /* Step2: Put the new enum at the corresponding index value you set at above */
        private static ID[] indexID = new ID[]{
                AUTO_REPORT_GPS, AUTO_REPORT_SCAN_WIFI, AUTO_REPORT_WIFI, START_SERVICE_FAILED, SERVICE_ENCOUNTER_FAILURE,
                ENABLE_AUTO_REPORT_GPS, DISABLE_AUTO_REPORT_GPS, ENABLE_AUTO_REPORT_WIFI, DISABLE_AUTO_REPORT_WIFI,
                UPDATE_GPS_REPORT_INTERVAL, UPDATE_WIFI_REPORT_INTERVAL, STOP_SERVICE, RECEIVE_REQUEST, ENABLE_SERVICE
        };

        public static ID fromInt(int value) {
            return indexID[value];
        }

        private int value;

        private ID(int value) {
            this.value = value;
        }

        public int getValueID() {
            return this.value;
        }

        /* Step3: Add a new entry for your enum, and return the right String Resource ID */
        public int getResId() {
            switch (this) {
                case AUTO_REPORT_GPS:
                    return R.string.event_description_auto_report_gps;
                case AUTO_REPORT_SCAN_WIFI:
                    return R.string.event_description_auto_report_scan_surrounding_wifi;
                case AUTO_REPORT_WIFI:
                    return R.string.event_description_auto_report_send_wifi_signals;
                case START_SERVICE_FAILED:
                    return R.string.event_description_enable_service_failed;
                case SERVICE_ENCOUNTER_FAILURE:
                    return R.string.event_description_warn_service_failure;
                case ENABLE_AUTO_REPORT_GPS:
                    return R.string.event_description_enable_auto_report_gps;
                case DISABLE_AUTO_REPORT_GPS:
                    return R.string.event_description_disable_gps_auto_report;
                case ENABLE_AUTO_REPORT_WIFI:
                    return R.string.event_description_enable_wifi_auto_report;
                case DISABLE_AUTO_REPORT_WIFI:
                    return R.string.event_description_disable_wifi_auto_report;
                case UPDATE_GPS_REPORT_INTERVAL:
                    return R.string.event_description_update_gps_report_interval;
                case UPDATE_WIFI_REPORT_INTERVAL:
                    return R.string.event_description_update_wifi_report_interval;
                case STOP_SERVICE:
                    return R.string.event_description_service_disabled;
                case RECEIVE_REQUEST:
                    return R.string.event_description_receive_request;
                case ENABLE_SERVICE:
                    return R.string.event_description_enable_service;
                default:
                    Log.wtf("Event", "I don't know about this EventID -> " + this.getValueID());
                    return R.string.WTF;
            }
        }
    }

    public static Event debug(ID title, String description) {
        return new Event(EventType.APP_DEBUG, title, description);
    }

    public static Event info(ID title, String description) {
        return new Event(EventType.APP_INFO, title, description);
    }

    public static Event warning(ID title, String description) {
        return new Event(EventType.APP_WARNING, title, description);
    }

    public static Event error(ID title, String description) {
        return new Event(EventType.APP_ERROR, title, description);
    }

    public static Event request(ID title, String description) {
        return new Event(EventType.RECEIVE_REQUEST, title, description);
    }

    public static Event response(ID title, String description) {
        return new Event(EventType.SEND_RESPONSE, title, description);
    }

    public static Event debug(ID title) {
        return new Event(EventType.APP_DEBUG, title, null);
    }

    public static Event info(ID title) {
        return new Event(EventType.APP_INFO, title, null);
    }

    public static Event warning(ID title) {
        return new Event(EventType.APP_WARNING, title, null);
    }

    public static Event error(ID title) {
        return new Event(EventType.APP_ERROR, title, null);
    }

    public static Event request(ID title) {
        return new Event(EventType.RECEIVE_REQUEST, title, null);
    }

    public static Event response(ID title) {
        return new Event(EventType.SEND_RESPONSE, title, null);
    }
}
