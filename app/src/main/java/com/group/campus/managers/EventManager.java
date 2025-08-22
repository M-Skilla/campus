package com.group.campus.managers;

import com.group.campus.models.Event;
import java.util.ArrayList;
import java.util.List;

public class EventManager {
    private static EventManager instance;
    private List<Event> events;

    private EventManager() {
        events = new ArrayList<>();
    }

    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public void addEvent(Event event) {
        if (event != null) {
            events.add(event);
        }
    }

    public List<Event> getAllEvents() {
        return new ArrayList<>(events);
    }

    public void removeEvent(Event event) {
        events.remove(event);
    }

    public void removeEventById(String id) {
        events.removeIf(event -> event.getId().equals(id));
    }

    public Event getEventById(String id) {
        for (Event event : events) {
            if (event.getId().equals(id)) {
                return event;
            }
        }
        return null;
    }

    public List<Event> getEventsForDate(int year, int month, int day) {
        List<Event> eventsForDate = new ArrayList<>();
        for (Event event : events) {
            if (event.isOnDate(year, month, day)) {
                eventsForDate.add(event);
            }
        }
        return eventsForDate;
    }

    public void clearAllEvents() {
        events.clear();
    }


    public void setEvents(List<Event> newEvents) {
        if (newEvents != null) {
            this.events = new ArrayList<>(newEvents);
        }
    }

    public boolean hasEventsOnDate(int year, int month, int day) {
        for (Event event : events) {
            if (event.isOnDate(year, month, day)) {
                return true;
            }
        }
        return false;
    }
}
