package com.group.campus.ui.suggestions;

import com.group.campus.models.Suggestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple in-memory store to share suggestions between sender inbox and staff view.
 * Not persistent; replace with real data source later.
 */
public class SuggestionsRepository {
    private static final SuggestionsRepository INSTANCE = new SuggestionsRepository();

    public static SuggestionsRepository get() { return INSTANCE; }

    private final List<Suggestion> all = new ArrayList<>();

    private SuggestionsRepository() {}

    public synchronized void add(Suggestion s) {
        all.add(s);
    }

    public synchronized List<Suggestion> getAll() {
        return new ArrayList<>(all);
    }

    public synchronized Map<String, List<Suggestion>> byDepartment() {
        Map<String, List<Suggestion>> map = new HashMap<>();
        for (Suggestion s : all) {
            String dept = s.getReceiverDepartment() == null ? "" : s.getReceiverDepartment();
            map.computeIfAbsent(dept, k -> new ArrayList<>()).add(s);
        }
        return map;
    }

    public synchronized List<Suggestion> getByDepartment(String department) {
        if (department == null) department = "";
        List<Suggestion> out = new ArrayList<>();
        for (Suggestion s : all) {
            if (department.equals(s.getReceiverDepartment())) out.add(s);
        }
        return out;
    }
}
