package com.group.campus.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.chip.ChipGroup;

import com.group.campus.R;
import com.group.campus.adapters.YearViewAdapter;
import com.group.campus.adapters.MonthViewAdapter;
import com.group.campus.adapters.EventsAdapter;
import com.group.campus.models.Event;
import com.group.campus.managers.EventManager;

import java.util.Arrays;
import java.util.List;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class CalendarFragment extends Fragment implements YearViewAdapter.OnMonthClickListener {

    private RecyclerView yearCalendarRecyclerView;
    private RecyclerView monthCalendarRecyclerView;
    private RecyclerView eventsRecyclerView;
    private View eventsLayout;
    private TextView titleText;
    private FloatingActionButton fabAddEvent;

    private Button btnYear, btnMonth, btnEvents;
    private YearViewAdapter yearAdapter;
    private MonthViewAdapter monthAdapter;
    private EventsAdapter eventsAdapter;
    private EventManager eventManager;

    private int currentYear = 2025;
    private int currentMonth = 7; // August (0-indexed)
    private final String[] monthNames = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    // Variables to store selected date/time for event creation
    private Calendar selectedStartDateTime;
    private Calendar selectedEndDateTime;

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
        fabAddEvent = view.findViewById(R.id.fab_add_event);

        btnYear = view.findViewById(R.id.btnYear);
        btnMonth = view.findViewById(R.id.btnMonth);
        btnEvents = view.findViewById(R.id.btnEvents);
    }

    private void setupAdapters() {
        // Initialize EventManager
        eventManager = EventManager.getInstance();

        // Year view adapter
        yearAdapter = new YearViewAdapter(currentYear);
        yearAdapter.setOnMonthClickListener(this);
        yearCalendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        yearCalendarRecyclerView.setAdapter(yearAdapter);

        // Month view adapter
        monthAdapter = new MonthViewAdapter(currentYear, currentMonth);
        monthCalendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        monthCalendarRecyclerView.setAdapter(monthAdapter);

        // Events adapter - now uses real events instead of sample data
        eventsAdapter = new EventsAdapter(eventManager.getAllEvents());
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

        fabAddEvent.setOnClickListener(v -> showNewEventDialog());
    }

    private void showNewEventDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_new_event, null);
        dialog.setContentView(dialogView);

        // Initialize dialog views
        TextView btnCancel = dialogView.findViewById(R.id.btn_cancel);
        TextView btnDone = dialogView.findViewById(R.id.btn_done);
        TextInputEditText etTitle = dialogView.findViewById(R.id.et_title);
        SwitchMaterial switchAllDay = dialogView.findViewById(R.id.switch_all_day);
        SwitchMaterial switchSoundAlert = dialogView.findViewById(R.id.switch_sound_alert);
        TextView tvStartDate = dialogView.findViewById(R.id.tv_start_date);
        TextView tvEndDate = dialogView.findViewById(R.id.tv_end_date);
        TextView tvStartTime = dialogView.findViewById(R.id.tv_start_time);
        TextView tvEndTime = dialogView.findViewById(R.id.tv_end_time);
        View layoutStartDate = dialogView.findViewById(R.id.layout_start_date);
        View layoutEndDate = dialogView.findViewById(R.id.layout_end_date);
        ChipGroup chipGroupEventTypes = dialogView.findViewById(R.id.chipGroup_event_types);

        // Initialize date/time variables
        selectedStartDateTime = Calendar.getInstance();
        selectedEndDateTime = Calendar.getInstance();
        selectedEndDateTime.add(Calendar.HOUR_OF_DAY, 1);

        // Set current date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        String currentDate = dateFormat.format(selectedStartDateTime.getTime());
        tvStartDate.setText(currentDate);
        tvEndDate.setText(currentDate);
        tvStartTime.setText(timeFormat.format(selectedStartDateTime.getTime()));
        tvEndTime.setText(timeFormat.format(selectedEndDateTime.getTime()));

        // Handle chip selection to switch between forms
        chipGroupEventTypes.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_reminder) {
                dialog.dismiss();
                showReminderDialog();
            }
        });

        // Handle all day switch
        switchAllDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvStartTime.setVisibility(View.GONE);
                tvEndTime.setVisibility(View.GONE);
            } else {
                tvStartTime.setVisibility(View.VISIBLE);
                tvEndTime.setVisibility(View.VISIBLE);
            }
        });

        // Handle date/time picker clicks
        layoutStartDate.setOnClickListener(v -> {
            showDateTimePicker(tvStartDate, tvStartTime, switchAllDay.isChecked(), true);
        });

        layoutEndDate.setOnClickListener(v -> {
            showDateTimePicker(tvEndDate, tvEndTime, switchAllDay.isChecked(), false);
        });

        // Handle dialog buttons
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDone.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (!title.isEmpty()) {
                // Create new event
                Event newEvent = new Event(
                    title,
                    selectedStartDateTime.getTime(),
                    selectedEndDateTime.getTime(),
                    switchAllDay.isChecked(),
                    switchSoundAlert.isChecked()
                );

                // Add event to manager
                eventManager.addEvent(newEvent);

                // Refresh UI
                refreshAllViews();

                dialog.dismiss();
            }
        });

        // Make dialog full screen
        dialog.getBehavior().setPeekHeight(getResources().getDisplayMetrics().heightPixels);
        dialog.show();
    }

    private void showReminderDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_reminder_form, null);
        dialog.setContentView(dialogView);

        // Initialize reminder dialog views
        TextView btnCancel = dialogView.findViewById(R.id.btn_cancel);
        TextView btnDone = dialogView.findViewById(R.id.btn_done);
        TextInputEditText etTitle = dialogView.findViewById(R.id.et_title);
        SwitchMaterial switchSoundAlert = dialogView.findViewById(R.id.switch_sound_alert);
        TextView tvSelectedEvent = dialogView.findViewById(R.id.tv_selected_event);
        TextView tvReminderTime = dialogView.findViewById(R.id.tv_reminder_time);
        View layoutEventSelection = dialogView.findViewById(R.id.layout_event_selection);
        View layoutReminderTime = dialogView.findViewById(R.id.layout_reminder_time);
        ChipGroup chipGroupEventTypes = dialogView.findViewById(R.id.chipGroup_event_types);

        // Handle chip selection to switch back to event form
        chipGroupEventTypes.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != R.id.chip_reminder) {
                dialog.dismiss();
                showNewEventDialog();
            }
        });

        // Handle event selection
        layoutEventSelection.setOnClickListener(v -> {
            showEventSelectionDialog(tvSelectedEvent);
        });

        // Handle reminder time selection
        layoutReminderTime.setOnClickListener(v -> {
            showReminderTimeSelectionDialog(tvReminderTime);
        });

        // Handle dialog buttons
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDone.setOnClickListener(v -> {
            // Here you would save the reminder data
            String title = etTitle.getText().toString();
            if (!title.isEmpty()) {
                // Save reminder logic here
                // For now, just dismiss the dialog
                dialog.dismiss();
            }
        });

        // Make dialog full screen
        dialog.getBehavior().setPeekHeight(getResources().getDisplayMetrics().heightPixels);
        dialog.show();
    }

    private void showEventSelectionDialog(TextView tvSelectedEvent) {
        // Get real events from EventManager instead of hardcoded examples
        List<Event> allEvents = eventManager.getAllEvents();
        
        if (allEvents.isEmpty()) {
            // Show message if no events exist
            androidx.appcompat.app.AlertDialog.Builder noEventsBuilder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
            noEventsBuilder.setTitle("No Events Available")
                    .setMessage("Please create some events first before setting reminders.")
                    .setPositiveButton("OK", null);
            noEventsBuilder.create().show();
            return;
        }
        
        // Convert events to display strings
        String[] events = new String[allEvents.size()];
        for (int i = 0; i < allEvents.size(); i++) {
            Event event = allEvents.get(i);
            // Format: "Event Title - Date"
            events[i] = event.getTitle() + " - " + event.getFormattedDate();
        }

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Select Event to Remind")
                .setItems(events, (dialog, which) -> {
                    Event selectedEvent = allEvents.get(which);
                    tvSelectedEvent.setText(selectedEvent.getTitle());
                    tvSelectedEvent.setTextColor(getResources().getColor(android.R.color.white));
                })
                .setNegativeButton("Cancel", null);

        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showReminderTimeSelectionDialog(TextView tvReminderTime) {
        String[] reminderOptions = {
            "5 minutes before",
            "15 minutes before",
            "30 minutes before",
            "1 hour before",
            "2 hours before",
            "1 day before",
            "1 week before"
        };

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Reminder Time")
                .setItems(reminderOptions, (dialog, which) -> {
                    tvReminderTime.setText(reminderOptions[which]);
                })
                .setNegativeButton("Cancel", null);

        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDateTimePicker(TextView dateView, TextView timeView, boolean isAllDay, boolean isStart) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                dateView.setText(dateFormat.format(calendar.getTime()));

                // Update the selected date in the appropriate Calendar object
                if (isStart) {
                    selectedStartDateTime.set(Calendar.YEAR, year);
                    selectedStartDateTime.set(Calendar.MONTH, month);
                    selectedStartDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                } else {
                    selectedEndDateTime.set(Calendar.YEAR, year);
                    selectedEndDateTime.set(Calendar.MONTH, month);
                    selectedEndDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                }

                if (!isAllDay) {
                    // Show time picker after date selection
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                        requireContext(),
                        (timePickerView, hourOfDay, minute) -> {
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            calendar.set(Calendar.MINUTE, minute);
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            timeView.setText(timeFormat.format(calendar.getTime()));

                            // Update selected start or end time
                            if (isStart) {
                                selectedStartDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedStartDateTime.set(Calendar.MINUTE, minute);
                            } else {
                                selectedEndDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedEndDateTime.set(Calendar.MINUTE, minute);
                            }
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    );
                    timePickerDialog.show();
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void showSingleDateTimePicker(TextView dateView, TextView timeView, boolean isAllDay) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                // Update the selected date in selectedStartDateTime
                selectedStartDateTime.set(Calendar.YEAR, year);
                selectedStartDateTime.set(Calendar.MONTH, month);
                selectedStartDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                dateView.setText(dateFormat.format(selectedStartDateTime.getTime()));

                if (!isAllDay) {
                    // Show time picker after date selection
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                        requireContext(),
                        (timePickerView, hourOfDay, minute) -> {
                            selectedStartDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            selectedStartDateTime.set(Calendar.MINUTE, minute);
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            timeView.setText(timeFormat.format(selectedStartDateTime.getTime()));
                        },
                        selectedStartDateTime.get(Calendar.HOUR_OF_DAY),
                        selectedStartDateTime.get(Calendar.MINUTE),
                        true
                    );
                    timePickerDialog.show();
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
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

        // Refresh events list when showing events view
        eventsAdapter.updateEvents(eventManager.getAllEvents());
    }

    private void updateButtonSelection(Button selectedButton) {
        // Reset all buttons
        btnYear.setBackgroundResource(R.drawable.button_unselected_background);
        btnMonth.setBackgroundResource(R.drawable.button_unselected_background);
        btnEvents.setBackgroundResource(R.drawable.button_unselected_background);

        // Set selected button
        selectedButton.setBackgroundResource(R.drawable.button_selected_background);
    }

    private void refreshAllViews() {
        // Refresh events adapter
        eventsAdapter.updateEvents(eventManager.getAllEvents());

        // Refresh month view if it's currently visible
        if (monthCalendarRecyclerView.getVisibility() == View.VISIBLE) {
            monthAdapter.notifyDataSetChanged();
        }

        // Refresh year view if it's currently visible
        if (yearCalendarRecyclerView.getVisibility() == View.VISIBLE) {
            yearAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onMonthClick(int month) {
        currentMonth = month;
        showMonthView();
        updateButtonSelection(btnMonth);
    }
}
