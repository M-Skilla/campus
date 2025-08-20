package com.group.campus.fragments;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group.campus.R;
import com.group.campus.adapters.CustomMonthAdapter;
import com.group.campus.adapters.MonthViewAdapter;
import com.group.campus.adapters.EventsAdapter;

import java.util.Arrays;
import java.util.List;

public class CalendarFragment extends Fragment implements CustomMonthAdapter.OnMonthClickListener {

    private RecyclerView yearCalendarRecyclerView;
    private RecyclerView monthCalendarRecyclerView;
    private RecyclerView eventsRecyclerView;
    private View eventsLayout;
    private TextView titleText;

    private Button btnYear, btnMonth, btnEvents;
    private CustomMonthAdapter yearAdapter;
    private MonthViewAdapter monthAdapter;
    private EventsAdapter eventsAdapter;

    private int currentYear = 2025;
    private int currentMonth = 7; // August (0-indexed)
    private final String[] monthNames = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        initViews(view);
        setupAdapters();
        setupClickListeners();

        // Start with year view
        showYearView();

        return view;
    }

    private void initViews(View view) {
        yearCalendarRecyclerView = view.findViewById(R.id.yearCalendarRecyclerView);
        monthCalendarRecyclerView = view.findViewById(R.id.monthCalendarRecyclerView);
        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView);
        eventsLayout = view.findViewById(R.id.eventsLayout);
        titleText = view.findViewById(R.id.titleText);

        btnYear = view.findViewById(R.id.btnYear);
        btnMonth = view.findViewById(R.id.btnMonth);
        btnEvents = view.findViewById(R.id.btnEvents);
    }

    private void setupAdapters() {
        // Year view adapter
        yearAdapter = new CustomMonthAdapter(currentYear);
        yearAdapter.setOnMonthClickListener(this);
        yearCalendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        yearCalendarRecyclerView.setAdapter(yearAdapter);

        // Month view adapter
        monthAdapter = new MonthViewAdapter(currentYear, currentMonth);
        monthCalendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        monthCalendarRecyclerView.setAdapter(monthAdapter);

        // Events adapter
        List<String> sampleEvents = Arrays.asList(
                "Team Meeting",
                "Project Deadline",
                "Campus Event",
                "Study Group",
                "Assignment Due"
        );
        eventsAdapter = new EventsAdapter(sampleEvents);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventsRecyclerView.setAdapter(eventsAdapter);
    }

    private void setupClickListeners() {
        btnYear.setOnClickListener(v -> {
            showYearView();
            updateButtonSelection(btnYear);
        });

        btnMonth.setOnClickListener(v -> {
            showMonthView();
            updateButtonSelection(btnMonth);
        });

        btnEvents.setOnClickListener(v -> {
            showEventsView();
            updateButtonSelection(btnEvents);
        });
    }

    private void showYearView() {
        yearCalendarRecyclerView.setVisibility(View.VISIBLE);
        monthCalendarRecyclerView.setVisibility(View.GONE);
        eventsLayout.setVisibility(View.GONE);

        // Update title to show current year
        titleText.setText(currentYear + " Calendar");
    }

    private void showMonthView() {
        yearCalendarRecyclerView.setVisibility(View.GONE);
        monthCalendarRecyclerView.setVisibility(View.VISIBLE);
        eventsLayout.setVisibility(View.GONE);

        // Update month adapter with current month
        monthAdapter = new MonthViewAdapter(currentYear, currentMonth);
        monthCalendarRecyclerView.setAdapter(monthAdapter);

        // Update title to show current month and year
        titleText.setText(monthNames[currentMonth] + " " + currentYear);
    }

    private void showEventsView() {
        yearCalendarRecyclerView.setVisibility(View.GONE);
        monthCalendarRecyclerView.setVisibility(View.GONE);
        eventsLayout.setVisibility(View.VISIBLE);

        // Update title for events view
        titleText.setText("Events");
    }

    private void updateButtonSelection(Button selectedButton) {
        // Reset all buttons
        btnYear.setBackgroundResource(R.drawable.button_unselected_background);
        btnMonth.setBackgroundResource(R.drawable.button_unselected_background);
        btnEvents.setBackgroundResource(R.drawable.button_unselected_background);

        // Set selected button
        selectedButton.setBackgroundResource(R.drawable.button_selected_background);
    }

    @Override
    public void onMonthClick(int month) {
        currentMonth = month;
        showMonthView();
        updateButtonSelection(btnMonth);

    }
}
