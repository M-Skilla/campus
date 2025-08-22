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
