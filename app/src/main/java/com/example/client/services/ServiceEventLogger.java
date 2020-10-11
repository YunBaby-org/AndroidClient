package com.example.client.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ServiceEventLogger {
    private final List<IServiceEventListener> listeners = Collections.synchronizedList(new ArrayList<>());

    public ServiceEventLogger() {
    }

    public void emitEvent(Event e) {
        synchronized (listeners) {
            for (IServiceEventListener listener : listeners)
                listener.onEventOccurred(e);
        }
    }

    public void addListener(ServiceEventLogger.IServiceEventListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(ServiceEventLogger.IServiceEventListener listener) throws Exception {
        synchronized (listeners) {
            if (!listeners.remove(listener)) {
                throw new Exception("Listener not found");
            }
        }
    }

    public static class Event {

        private Date date;
        private Level level;
        private int contentId;
        private String content;

        public Event(Level level, String content) {
            this.level = level;
            this.content = content;
        }

        public Event(Level level, int contentId) {
            this.level = level;
            this.contentId = contentId;
        }

        public static Event Critical(String content) {
            return new Event(Level.Critical, content);
        }

        public static Event Error(String content) {
            return new Event(Level.Error, content);
        }

        public static Event Warn(String content) {
            return new Event(Level.Warning, content);
        }

        public static Event Info(String content) {
            return new Event(Level.Info, content);
        }

        public static Event Debug(String content) {
            return new Event(Level.Debug, content);
        }

        public static Event Critical(int content) {
            return new Event(Level.Critical, content);
        }

        public static Event Error(int content) {
            return new Event(Level.Error, content);
        }

        public static Event Warn(int content) {
            return new Event(Level.Warning, content);
        }

        public static Event Info(int content) {
            return new Event(Level.Info, content);
        }

        public static Event Debug(int content) {
            return new Event(Level.Debug, content);
        }

        public Date getDate() {
            return date;
        }

        public Level getLevel() {
            return level;
        }

        public String getContent() {
            return content;
        }

        public int getContentId() {
            return contentId;
        }

        public enum Level {Critical, Error, Warning, Info, Debug}
    }

    public interface IServiceEventListener {
        void onEventOccurred(Event e);
    }
}
