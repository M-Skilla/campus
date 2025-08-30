package com.group.campus.managers;

import com.group.campus.models.Event;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

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
            sortEventsByDate(); // Sort after adding
        }
    }

    public List<Event> getAllEvents() {
        sortEventsByDate(); // Ensure sorted before returning
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
        // Sort the events for the specific date by time
        sortEventsByDate(eventsForDate);
        return eventsForDate;
    }

    public void clearAllEvents() {
        events.clear();
    }

    public void setEvents(List<Event> newEvents) {
        if (newEvents != null) {
            this.events = new ArrayList<>(newEvents);
            sortEventsByDate(); // Sort after setting new events
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

    /**
     * Sort events by start date in ascending order
     */
    private void sortEventsByDate() {
        sortEventsByDate(events);
    }

    /**
     * Sort a list of events by start date in ascending order
     */
    private void sortEventsByDate(List<Event> eventList) {
        Collections.sort(eventList, new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                if (e1.getStartDate() == null && e2.getStartDate() == null) {
                    return 0;
                }
                if (e1.getStartDate() == null) {
                    return 1; // null dates go to the end
                }
                if (e2.getStartDate() == null) {
                    return -1; // null dates go to the end
                }
                return e1.getStartDate().compareTo(e2.getStartDate());
            }
        });
    }
}
