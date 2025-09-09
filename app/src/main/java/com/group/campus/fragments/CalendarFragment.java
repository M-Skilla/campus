package com.group.campus.fragments;

import android.app.DatePickerDialog;
import android.view.GestureDetector;
import android.view.MotionEvent;
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
import com.group.campus.models.User;
import com.group.campus.managers.EventManager;
import com.group.campus.managers.ReminderManager;
import com.group.campus.models.Reminder;
import com.group.campus.service.UserRoleService;

import java.util.*;
import java.text.SimpleDateFormat;

public class CalendarFragment extends Fragment implements YearViewAdapter.OnMonthClickListener, MonthViewAdapter.OnDayClickListener {

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

    // Reminder management
    private ReminderManager reminderManager;

    // Gesture detection for swipe navigation
    private GestureDetector gestureDetector;

    // Firestore
    private FirebaseFirestore db;
    private static final String COLLECTION_NAME = "events";

    // Date/Time - Initialize with current date
    private Calendar currentCalendar = Calendar.getInstance();
    private int currentYear = currentCalendar.get(Calendar.YEAR);
    private int currentMonth = currentCalendar.get(Calendar.MONTH);
    private final String[] monthNames = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
    private Calendar selectedStartDateTime, selectedEndDateTime;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private SwitchMaterial currentReminderSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        db = FirebaseFirestore.getInstance();
        initViews(view);
        setupAdapters();
        setupClickListeners();
        setupSwipeNavigation(view);
        fetchEventsFromFirestore();
        showMonthView();
        checkUserRoleAndUpdateFab();

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
        btnYear = view.findViewById(R.id.btnYear);
        btnMonth = view.findViewById(R.id.btnMonth);
        btnEvents = view.findViewById(R.id.btnEvents);

        eventManager = EventManager.getInstance();
        reminderManager = ReminderManager.getInstance();

        fabAddEvent.setVisibility(View.GONE); // Hide FAB by default
    }

    /**
     * Check user role and update FAB visibility based on role
     */
    private void checkUserRoleAndUpdateFab() {
        UserRoleService roleService = new UserRoleService();

        roleService.getCurrentUser(new UserRoleService.UserCallback() {
            @Override
            public void onUserLoaded(User user) {
                if (roleService.isStaff(user)) {
                    fabAddEvent.setVisibility(View.VISIBLE);
                    setupFabClickListener();
                } else {
                    fabAddEvent.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {
                // Hide FAB on error for security
                fabAddEvent.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Setup FAB click listener for staff users
     */
    private void setupFabClickListener() {
        fabAddEvent.setOnClickListener(v -> {
            // Show add event dialog or navigate to add event screen
            showNewEventDialog();
        });
    }

    private void setupAdapters() {
        eventManager = EventManager.getInstance();
        reminderManager = ReminderManager.getInstance();
        reminderManager.init(getContext());

        yearAdapter = new YearViewAdapter(currentYear);
        yearAdapter.setOnMonthClickListener(this);
        yearCalendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        yearCalendarRecyclerView.setAdapter(yearAdapter);

        monthAdapter = new MonthViewAdapter(currentYear, currentMonth);
        monthAdapter.setOnDayClickListener(this);
        monthCalendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        monthCalendarRecyclerView.setAdapter(monthAdapter);

        eventsAdapter = new EventsAdapter(eventManager.getAllEvents());
        eventsAdapter.setOnEventClickListener(this::onEventClick);
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
                monthAdapter.setOnDayClickListener(this);
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
            String title = etTitle.getText().toString().trim();
            String selectedEventText = tvSelectedEvent.getText().toString();
            String reminderTimeText = tvReminderTime.getText().toString();

            if (!title.isEmpty() && !selectedEventText.equals("Select Event") && !reminderTimeText.equals("Select Time")) {
                createSoundReminder(selectedEventText, reminderTimeText);
                showToast("Sound reminder set successfully!");
                dialog.dismiss();
            } else {
                showToast("Please fill in all fields");
            }
        });
    }

    private void initializeDateTimeFields(TextView tvStartDate, TextView tvEndDate, TextView tvStartTime, TextView tvEndTime) {
        selectedStartDateTime = Calendar.getInstance();
        selectedEndDateTime = Calendar.getInstance();
        selectedEndDateTime.add(Calendar.HOUR_OF_DAY, 0);

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
                // Add null checks for the switches to prevent NullPointerException
                boolean isAllDay = switchAllDay != null ? switchAllDay.isChecked() : false;
                boolean hasSoundAlert = switchSoundAlert != null ? switchSoundAlert.isChecked() : false;

                Event newEvent = new Event(title, selectedStartDateTime.getTime(),
                        selectedEndDateTime.getTime(),
                        isAllDay, hasSoundAlert);
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
                    // Store the full display text (title + date) to match with createSoundReminder logic
                    String fullDisplayText = selectedEvent.getTitle() + " - " + selectedEvent.getFormattedDate();
                    tvSelectedEvent.setText(fullDisplayText);
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
            yearAdapter.refreshEvents();
        }
    }

    private void fetchEventsFromFirestore() {
        db.collection(COLLECTION_NAME).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Event> events = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            if (event != null) {
                                events.add(event);
                            }
                        }
                        eventManager.setEvents(events);
                        eventsAdapter.updateEvents(events);
                        // Refresh all calendar views to show event indicators immediately
                        refreshAllViews();
                    } else {
                        showToast("Error fetching events");
                    }
                });
    }

    private void saveEventToFirestore(Event event) {
        db.collection(COLLECTION_NAME).add(event)
                .addOnSuccessListener(documentReference -> {
                    event.setId(documentReference.getId()); // Set the document ID to the event object
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

    @Override
    public void onDayWithEventsClick(int year, int month, int day) {
        // Show dialog with events for the selected day
        if (eventManager.hasEventsOnDate(year, month, day)) {
            showDayEventsDialog(year, month, day);
        }
    }

    // Add method to show events for a specific day in a dialog
    private void showDayEventsDialog(int year, int month, int day) {
        // Get events for this specific day
        List<Event> dayEvents = eventManager.getEventsForDate(year, month, day);

        if (dayEvents.isEmpty()) {
            showToast("No events found for this day");
            return;
        }

        // Create a BottomSheetDialog to match the screenshot style
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_day_events, null);
        dialog.setContentView(dialogView);

        // Find views in dialog
        TextView tvDayTitle = dialogView.findViewById(R.id.tv_day_title);
        RecyclerView rvDayEvents = dialogView.findViewById(R.id.rv_day_events);

        // Set title - format: "September 23" or similar
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        SimpleDateFormat dayFormatter = new SimpleDateFormat("MMMM dd", Locale.getDefault());
        tvDayTitle.setText(dayFormatter.format(calendar.getTime()));

        // Check if selected date is before current date
        Calendar currentDate = Calendar.getInstance();
        boolean isPastDate = calendar.before(currentDate);

        // Set up RecyclerView for events
        EventsAdapter dayEventsAdapter = new EventsAdapter(dayEvents);
        dayEventsAdapter.setOnEventClickListener(event -> {
            dialog.dismiss();
            showEventDetailsDialog(event, isPastDate);
        });
        rvDayEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDayEvents.setAdapter(dayEventsAdapter);

        dialog.show();
    }

    // Add the event click handler method
    private void onEventClick(Event event) {
        Calendar currentDate = Calendar.getInstance();
        Calendar eventDate = Calendar.getInstance();
        eventDate.setTime(event.getStartDate());

        boolean isPastDate = eventDate.before(currentDate);
        showEventDetailsDialog(event, isPastDate);
    }

    // Update the showEventDetailsDialog method to work with Event object
    private void showEventDetailsDialog(Event event, boolean hideReminder) {
        // Create dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_event_details, null);
        builder.setView(dialogView);

        // Find views in dialog
        TextView tvEventName = dialogView.findViewById(R.id.tv_event_name);
        TextView tvEventDate = dialogView.findViewById(R.id.tv_event_date);
        TextView tvEventTime = dialogView.findViewById(R.id.tv_event_time);
        TextView tvSetReminder = dialogView.findViewById(R.id.set_reminder_text);
        SwitchMaterial switchReminder = dialogView.findViewById(R.id.switch_reminder);
        Button btnClose = dialogView.findViewById(R.id.btn_close);
        currentReminderSwitch = switchReminder;

        // Set event details
        tvEventName.setText(event.getTitle());
        tvEventDate.setText(event.getFormattedDate());

        // Format time
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvEventTime.setText(timeFormatter.format(event.getStartDate()));


        // Hide reminder switch if it's a past date
        if (hideReminder) {
            switchReminder.setVisibility(View.GONE);
            tvSetReminder.setVisibility(View.GONE);
        } else {
            boolean hasReminder = hasReminderForEvent(event);
            switchReminder.setChecked(hasReminder);

            switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    showReminderDialogForEvent(event);
                } else {
                    removeReminderForEvent(event);
                }
            });
        }

        // Create and show dialog
        androidx.appcompat.app.AlertDialog dialog = builder.create();

        btnClose.setOnClickListener(v -> {
            currentReminderSwitch = null; // Clear reference when dialog is closed
            dialog.dismiss();
        });

        dialog.show();
    }

    // Helper method to check if event has a reminder
    private boolean hasReminderForEvent(Event event) {
        List<Reminder> allReminders = reminderManager.getAllReminders();
        for (Reminder reminder : allReminders) {
            if (reminder.getEventTitle().equals(event.getTitle()) && reminder.isActive()) {
                return true;
            }
        }
        return false;
    }

    // Helper method to remove reminder for an event
    private void removeReminderForEvent(Event event) {
        List<Reminder> allReminders = reminderManager.getAllReminders();
        for (Reminder reminder : allReminders) {
            if (reminder.getEventTitle().equals(event.getTitle()) && reminder.isActive()) {
                reminderManager.removeReminder(reminder.getId());
                showToast("Reminder removed");
                break;
            }
        }
    }

    // Show reminder dialog specifically for an event
    private void showReminderDialogForEvent(Event event) {
        String[] reminderOptions = {"5 minutes before", "15 minutes before", "30 minutes before",
                "1 hour before", "2 hours before", "1 day before", "1 week before"};

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Set Reminder")
                .setItems(reminderOptions, (dialog, which) -> {
                    String selectedReminderTime = reminderOptions[which];
                    createReminderForEvent(event, selectedReminderTime);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User cancelled, turn switch back off
                    if (currentReminderSwitch != null) {
                        currentReminderSwitch.setChecked(false);
                    }
                })
                .show();
    }

    // Create reminder for a specific event
    private void createReminderForEvent(Event event, String reminderTimeText) {
        // Parse reminder time
        int reminderMinutes = parseReminderTime(reminderTimeText);
        if (reminderMinutes == -1) {
            showToast("Invalid reminder time");
            return;
        }

        // Create reminder
        String reminderId = "reminder_" + System.currentTimeMillis();
        Reminder reminder = new Reminder(
                reminderId,
                event.getTitle(),
                event.getStartDate(),
                reminderMinutes
        );

        // Schedule the reminder
        reminderManager.addReminder(reminder);
        showToast("Reminder set for " + reminderTimeText);
    }

    private int parseReminderTime(String reminderTimeText) {
        switch (reminderTimeText) {
            case "5 minutes before":
                return 5;
            case "15 minutes before":
                return 15;
            case "30 minutes before":
                return 30;
            case "1 hour before":
                return 60;
            case "2 hours before":
                return 120;
            case "1 day before":
                return 1440;
            case "1 week before":
                return 10080;
            default:
                return -1;
        }
    }

    private void setupSwipeNavigation(View view) {
        gestureDetector = new GestureDetector(getContext(), new SwipeGestureListener());
        // Add touch listener to year view
        yearCalendarRecyclerView.setOnTouchListener((v, event) -> {
            if (yearCalendarRecyclerView.getVisibility() == View.VISIBLE) {
                return gestureDetector.onTouchEvent(event);
            }
            return false;
        });
        // Add touch listener to month view
        monthCalendarRecyclerView.setOnTouchListener((v, event) -> {
            if (monthCalendarRecyclerView.getVisibility() == View.VISIBLE) {
                return gestureDetector.onTouchEvent(event);
            }
            return false;
        });
    }
    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                    return true;
                }
            }
            return false;
        }
    }
    private void onSwipeLeft() {
        if (yearCalendarRecyclerView.getVisibility() == View.VISIBLE) {
            navigateToNextYear();
        } else if (monthCalendarRecyclerView.getVisibility() == View.VISIBLE) {
            navigateToNextMonth();
        }
    }
    private void onSwipeRight() {
        if (yearCalendarRecyclerView.getVisibility() == View.VISIBLE) {
            navigateToPreviousYear();
        } else if (monthCalendarRecyclerView.getVisibility() == View.VISIBLE) {
            navigateToPreviousMonth();
        }
    }
    private void navigateToNextYear() {
        currentYear++;
        yearAdapter = new YearViewAdapter(currentYear);
        yearAdapter.setOnMonthClickListener(this);
        yearCalendarRecyclerView.setAdapter(yearAdapter);
        titleText.setText(currentYear + " Calendar");
        refreshAllViews();
    }
    private void navigateToPreviousYear() {
        currentYear--;
        yearAdapter = new YearViewAdapter(currentYear);
        yearAdapter.setOnMonthClickListener(this);
        yearCalendarRecyclerView.setAdapter(yearAdapter);
        titleText.setText(currentYear + " Calendar");
        refreshAllViews();
    }
    private void navigateToNextMonth() {
        currentMonth++;
        if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        }
        monthAdapter = new MonthViewAdapter(currentYear, currentMonth);
        monthAdapter.setOnDayClickListener(this);
        monthCalendarRecyclerView.setAdapter(monthAdapter);
        titleText.setText(monthNames[currentMonth] + " " + currentYear);
        refreshAllViews();
    }
    private void navigateToPreviousMonth() {
        currentMonth--;
        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        }
        monthAdapter = new MonthViewAdapter(currentYear, currentMonth);
        monthAdapter.setOnDayClickListener(this);
        monthCalendarRecyclerView.setAdapter(monthAdapter);
        titleText.setText(monthNames[currentMonth] + " " + currentYear);
        refreshAllViews();
    }

    private void createSoundReminder(String selectedEventText, String reminderTimeText) {
        // Find the selected event
        Event selectedEvent = null;
        List<Event> allEvents = eventManager.getAllEvents();

        for (Event event : allEvents) {
            String eventDisplayText = event.getTitle() + " - " + event.getFormattedDate();
            if (eventDisplayText.equals(selectedEventText)) {
                selectedEvent = event;
                break;
            }
        }

        if (selectedEvent == null) {
            showToast("Selected event not found");
            return;
        }

        // Parse reminder time
        int reminderMinutes = parseReminderTime(reminderTimeText);
        if (reminderMinutes == -1) {
            showToast("Invalid reminder time");
            return;
        }

        // Create reminder
        String reminderId = "reminder_" + System.currentTimeMillis();
        Reminder reminder = new Reminder(
                reminderId,
                selectedEvent.getTitle(),
                selectedEvent.getStartDate(),
                reminderMinutes
        );

        // Schedule the sound alert
        reminderManager.addReminder(reminder);
    }
}
