package com.group.campus.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.chip.Chip;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import com.group.campus.R;
import com.group.campus.adapters.YearViewAdapter;
import com.group.campus.adapters.MonthViewAdapter;
import com.group.campus.adapters.EventsAdapter;
import com.group.campus.models.Event;
import com.group.campus.managers.EventManager;

import java.util.*;
import java.text.SimpleDateFormat;

public class CalendarFragment extends Fragment implements YearViewAdapter.OnMonthClickListener {

    // Views
    private RecyclerView yearCalendarRecyclerView, monthCalendarRecyclerView, eventsRecyclerView;
    private View eventsLayout;
    private TextView titleText;
    private FloatingActionButton fabAddEvent;
    private Button btnYear, btnMonth, btnEvents;

    // Adapters
    private YearViewAdapter yearAdapter;
    private MonthViewAdapter monthAdapter;
    private EventsAdapter eventsAdapter;
    private EventManager eventManager;

    // Firestore
    private FirebaseFirestore db;
    private static final String COLLECTION_NAME = "events";
    private static final String DOCUMENT_ID = "6HN7DWWYnhuaweIG2T14";

    // Date/Time
    private int currentYear = 2025, currentMonth = 7;
    private final String[] monthNames = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
    private Calendar selectedStartDateTime, selectedEndDateTime;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        db = FirebaseFirestore.getInstance();
        initViews(view);
        setupAdapters();
        setupClickListeners();
        fetchEventsFromFirestore();
        showYearView();

        return view;
    }

    private void initViews(View view) {
        // RecyclerViews
        yearCalendarRecyclerView = view.findViewById(R.id.yearCalendarRecyclerView);
        monthCalendarRecyclerView = view.findViewById(R.id.monthCalendarRecyclerView);
        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView);

        // Other views
        eventsLayout = view.findViewById(R.id.eventsLayout);
        titleText = view.findViewById(R.id.titleText);
        fabAddEvent = view.findViewById(R.id.fab_add_event);

        // Buttons
        btnYear = view.findViewById(R.id.btnYear);
        btnMonth = view.findViewById(R.id.btnMonth);
        btnEvents = view.findViewById(R.id.btnEvents);
    }

    private void setupAdapters() {
        eventManager = EventManager.getInstance();

        yearAdapter = new YearViewAdapter(currentYear);
        yearAdapter.setOnMonthClickListener(this);
        yearCalendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        yearCalendarRecyclerView.setAdapter(yearAdapter);

        monthAdapter = new MonthViewAdapter(currentYear, currentMonth);
        monthCalendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        monthCalendarRecyclerView.setAdapter(monthAdapter);

        eventsAdapter = new EventsAdapter(eventManager.getAllEvents());
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventsRecyclerView.setAdapter(eventsAdapter);
    }

    private void setupClickListeners() {
        btnYear.setOnClickListener(v -> switchView(0));
        btnMonth.setOnClickListener(v -> switchView(1));
        btnEvents.setOnClickListener(v -> switchView(2));
        fabAddEvent.setOnClickListener(v -> showNewEventDialog());
    }

    private void switchView(int viewType) {
        // Reset visibility
        yearCalendarRecyclerView.setVisibility(View.GONE);
        monthCalendarRecyclerView.setVisibility(View.GONE);
        eventsLayout.setVisibility(View.GONE);

        // Update button selection
        updateButtonSelection(viewType == 0 ? btnYear : viewType == 1 ? btnMonth : btnEvents);

        switch (viewType) {
            case 0: // Year view
                yearCalendarRecyclerView.setVisibility(View.VISIBLE);
                titleText.setText(currentYear + " Calendar");
                break;
            case 1: // Month view
                monthCalendarRecyclerView.setVisibility(View.VISIBLE);
                monthAdapter = new MonthViewAdapter(currentYear, currentMonth);
                monthCalendarRecyclerView.setAdapter(monthAdapter);
                titleText.setText(monthNames[currentMonth] + " " + currentYear);
                break;
            case 2: // Events view
                eventsLayout.setVisibility(View.VISIBLE);
                titleText.setText("Events");
                eventsAdapter.updateEvents(eventManager.getAllEvents());
                break;
        }
    }

    private void showYearView() { switchView(0); }
    private void showMonthView() { switchView(1); }
    private void showEventsView() { switchView(2); }

    private void showNewEventDialog() {
        BottomSheetDialog dialog = createDialog(R.layout.dialog_new_event);
        View dialogView = dialog.findViewById(android.R.id.content);

        initializeEventDialog(dialog, dialogView);
    }

    private void showReminderDialog() {
        BottomSheetDialog dialog = createDialog(R.layout.dialog_reminder_form);
        View dialogView = dialog.findViewById(android.R.id.content);

        initializeReminderDialog(dialog, dialogView);
    }

    private BottomSheetDialog createDialog(int layoutRes) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(layoutRes, null);
        dialog.setContentView(dialogView);
        dialog.getBehavior().setPeekHeight(getResources().getDisplayMetrics().heightPixels);
        dialog.show();
        return dialog;
    }

    private void initializeEventDialog(BottomSheetDialog dialog, View dialogView) {
        // Find views
        TextView btnCancel = dialogView.findViewById(R.id.btn_cancel);
        TextView btnDone = dialogView.findViewById(R.id.btn_done);
        TextInputEditText etTitle = dialogView.findViewById(R.id.et_title);
        SwitchMaterial switchAllDay = dialogView.findViewById(R.id.switch_all_day);
        SwitchMaterial switchSoundAlert = dialogView.findViewById(R.id.switch_sound_alert);
        TextView tvStartDate = dialogView.findViewById(R.id.tv_start_date);
        TextView tvEndDate = dialogView.findViewById(R.id.tv_end_date);
        TextView tvStartTime = dialogView.findViewById(R.id.tv_start_time);
        TextView tvEndTime = dialogView.findViewById(R.id.tv_end_time);

        // Initialize date/time
        initializeDateTimeFields(tvStartDate, tvEndDate, tvStartTime, tvEndTime);

        // Set up listeners
        setupChipListeners(dialogView, dialog, true);
        setupAllDaySwitch(switchAllDay, tvStartTime, tvEndTime);
        setupDateTimePickerListeners(dialogView, switchAllDay);
        setupDialogButtons(dialog, btnCancel, btnDone, etTitle, switchAllDay, switchSoundAlert);
    }

    private void initializeReminderDialog(BottomSheetDialog dialog, View dialogView) {
        // Find views
        TextView btnCancel = dialogView.findViewById(R.id.btn_cancel);
        TextView btnDone = dialogView.findViewById(R.id.btn_done);
        TextInputEditText etTitle = dialogView.findViewById(R.id.et_title);
        TextView tvSelectedEvent = dialogView.findViewById(R.id.tv_selected_event);
        TextView tvReminderTime = dialogView.findViewById(R.id.tv_reminder_time);

        // Set up listeners
        setupChipListeners(dialogView, dialog, false);
        setupReminderSelectionListeners(dialogView, tvSelectedEvent, tvReminderTime);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDone.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            if (!title.isEmpty()) dialog.dismiss();
        });
    }

    private void initializeDateTimeFields(TextView tvStartDate, TextView tvEndDate, TextView tvStartTime, TextView tvEndTime) {
        selectedStartDateTime = Calendar.getInstance();
        selectedEndDateTime = Calendar.getInstance();
        selectedEndDateTime.add(Calendar.HOUR_OF_DAY, 1);

        String currentDate = dateFormat.format(selectedStartDateTime.getTime());
        tvStartDate.setText(currentDate);
        tvEndDate.setText(currentDate);
        tvStartTime.setText(timeFormat.format(selectedStartDateTime.getTime()));
        tvEndTime.setText(timeFormat.format(selectedEndDateTime.getTime()));
    }

    private void setupChipListeners(View dialogView, BottomSheetDialog dialog, boolean isEventDialog) {
        Chip chipEvent = dialogView.findViewById(R.id.chip_event);
        Chip chipReminder = dialogView.findViewById(R.id.chip_reminder);

        if (isEventDialog) {
            chipReminder.setOnClickListener(v -> { dialog.dismiss(); showReminderDialog(); });
        } else {
            chipEvent.setOnClickListener(v -> { dialog.dismiss(); showNewEventDialog(); });
        }
    }

    private void setupAllDaySwitch(SwitchMaterial switchAllDay, TextView tvStartTime, TextView tvEndTime) {
        switchAllDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visibility = isChecked ? View.GONE : View.VISIBLE;
            tvStartTime.setVisibility(visibility);
            tvEndTime.setVisibility(visibility);
        });
    }

    private void setupDateTimePickerListeners(View dialogView, SwitchMaterial switchAllDay) {
        dialogView.findViewById(R.id.layout_start_date).setOnClickListener(v ->
            showDateTimePicker(dialogView.findViewById(R.id.tv_start_date),
                             dialogView.findViewById(R.id.tv_start_time),
                             switchAllDay.isChecked(), true));

        dialogView.findViewById(R.id.layout_end_date).setOnClickListener(v ->
            showDateTimePicker(dialogView.findViewById(R.id.tv_end_date),
                             dialogView.findViewById(R.id.tv_end_time),
                             switchAllDay.isChecked(), false));
    }

    private void setupReminderSelectionListeners(View dialogView, TextView tvSelectedEvent, TextView tvReminderTime) {
        dialogView.findViewById(R.id.layout_event_selection).setOnClickListener(v ->
            showEventSelectionDialog(tvSelectedEvent));

        dialogView.findViewById(R.id.layout_reminder_time).setOnClickListener(v ->
            showReminderTimeSelectionDialog(tvReminderTime));
    }

    private void setupDialogButtons(BottomSheetDialog dialog, TextView btnCancel, TextView btnDone,
                                  TextInputEditText etTitle, SwitchMaterial switchAllDay, SwitchMaterial switchSoundAlert) {
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDone.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (!title.isEmpty()) {
                Event newEvent = new Event(title, selectedStartDateTime.getTime(),
                                         selectedEndDateTime.getTime(),
                                         switchAllDay.isChecked(), switchSoundAlert.isChecked());
                eventManager.addEvent(newEvent);
                saveEventToFirestore(newEvent);
                refreshAllViews();
                dialog.dismiss();
            }
        });
    }

    private void showEventSelectionDialog(TextView tvSelectedEvent) {
        List<Event> allEvents = eventManager.getAllEvents();
        
        if (allEvents.isEmpty()) {
            showAlert("No Events Available", "Please create some events first before setting reminders.");
            return;
        }
        
        String[] events = allEvents.stream()
                                  .map(event -> event.getTitle() + " - " + event.getFormattedDate())
                                  .toArray(String[]::new);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Event to Remind")
                .setItems(events, (dialog, which) -> {
                    Event selectedEvent = allEvents.get(which);
                    tvSelectedEvent.setText(selectedEvent.getTitle());
                    tvSelectedEvent.setTextColor(getResources().getColor(android.R.color.white));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showReminderTimeSelectionDialog(TextView tvReminderTime) {
        String[] reminderOptions = {"5 minutes before", "15 minutes before", "30 minutes before",
                                   "1 hour before", "2 hours before", "1 day before", "1 week before"};

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Reminder Time")
                .setItems(reminderOptions, (dialog, which) -> tvReminderTime.setText(reminderOptions[which]))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAlert(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDateTimePicker(TextView dateView, TextView timeView, boolean isAllDay, boolean isStart) {
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            dateView.setText(dateFormat.format(calendar.getTime()));

            updateSelectedDateTime(year, month, dayOfMonth, isStart);

            if (!isAllDay) {
                new TimePickerDialog(requireContext(), (timePickerView, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    timeView.setText(timeFormat.format(calendar.getTime()));
                    updateSelectedTime(hourOfDay, minute, isStart);
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateSelectedDateTime(int year, int month, int dayOfMonth, boolean isStart) {
        Calendar target = isStart ? selectedStartDateTime : selectedEndDateTime;
        target.set(Calendar.YEAR, year);
        target.set(Calendar.MONTH, month);
        target.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    }

    private void updateSelectedTime(int hourOfDay, int minute, boolean isStart) {
        Calendar target = isStart ? selectedStartDateTime : selectedEndDateTime;
        target.set(Calendar.HOUR_OF_DAY, hourOfDay);
        target.set(Calendar.MINUTE, minute);
    }

    private void updateButtonSelection(Button selectedButton) {
        int unselected = R.drawable.button_unselected_background;
        int selected = R.drawable.button_selected_background;

        btnYear.setBackgroundResource(unselected);
        btnMonth.setBackgroundResource(unselected);
        btnEvents.setBackgroundResource(unselected);
        selectedButton.setBackgroundResource(selected);
    }

    private void refreshAllViews() {
        eventsAdapter.updateEvents(eventManager.getAllEvents());

        if (monthCalendarRecyclerView.getVisibility() == View.VISIBLE) {
            monthAdapter.notifyDataSetChanged();
        }
        if (yearCalendarRecyclerView.getVisibility() == View.VISIBLE) {
            yearAdapter.notifyDataSetChanged();
        }
    }

    private void fetchEventsFromFirestore() {
        db.collection(COLLECTION_NAME).document(DOCUMENT_ID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Event> events = parseEventsFromDocument(task.getResult());
                        eventManager.setEvents(events);
                        eventsAdapter.updateEvents(events);
                    } else {
                        showToast("Error fetching events");
                    }
                });
    }

    private List<Event> parseEventsFromDocument(DocumentSnapshot document) {
        List<Event> events = new ArrayList<>();

        if (document.exists()) {
            Object eventsArrayObj = document.get("events");
            if (eventsArrayObj instanceof List) {
                List<Map<String, Object>> eventsArray = (List<Map<String, Object>>) eventsArrayObj;

                for (Map<String, Object> eventData : eventsArray) {
                    Object titleObj = eventData.get("title");
                    Object startDateObj = eventData.get("startDate");
                    Object endDateObj = eventData.get("endDate");

                    if (titleObj != null && startDateObj != null && endDateObj != null) {
                        events.add(new Event(titleObj.toString(), startDateObj.toString(), endDateObj.toString()));
                    }
                }
            }
        }

        return events;
    }

    private void saveEventToFirestore(Event event) {
        db.collection(COLLECTION_NAME).document(DOCUMENT_ID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateFirestoreWithNewEvent(task.getResult(), event);
                    } else {
                        showToast("Error fetching existing events");
                    }
                });
    }

    private void updateFirestoreWithNewEvent(DocumentSnapshot document, Event event) {
        List<Map<String, Object>> eventsArray = new ArrayList<>();

        if (document.exists()) {
            Object existingEventsObj = document.get("events");
            if (existingEventsObj instanceof List) {
                eventsArray = (List<Map<String, Object>>) existingEventsObj;
            }
        }

        Map<String, Object> newEventData = new HashMap<>();
        newEventData.put("title", event.getTitle());
        newEventData.put("startDate", event.getStartDateString());
        newEventData.put("endDate", event.getEndDateString());
        eventsArray.add(newEventData);

        Map<String, Object> documentData = new HashMap<>();
        documentData.put("events", eventsArray);

        db.collection(COLLECTION_NAME).document(DOCUMENT_ID).set(documentData)
                .addOnSuccessListener(aVoid -> {
                    showToast("Event saved successfully");
                    fetchEventsFromFirestore();
                })
                .addOnFailureListener(e -> showToast("Error saving event: " + e.getMessage()));
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMonthClick(int month) {
        currentMonth = month;
        showMonthView();
        updateButtonSelection(btnMonth);
    }
}
