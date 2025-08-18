package com.group.campus;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group.campus.adapters.EventAdapter;
import com.group.campus.adapters.YearAdapter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class CalendarFragment extends Fragment {

    public RecyclerView recyclerView;
    private Button btnMonth, btnYear, btnEvents;
    private TextView monthTitle;
    private static final String[] DAY_HEADERS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_calendar, container, false);

        recyclerView = root.findViewById(R.id.RecyclerView);
        btnMonth = root.findViewById(R.id.btnMonth);
        btnYear = root.findViewById(R.id.btnYear);
        btnEvents = root.findViewById(R.id.btnEvents);
        monthTitle = root.findViewById(R.id.monthTitle);

        YearMonth currentMonth = YearMonth.now();

        // Default: Month View
        showMonthView(currentMonth);

        btnMonth.setOnClickListener(v -> showMonthView(currentMonth));
        btnYear.setOnClickListener(v -> showYearView(currentMonth));
        btnEvents.setOnClickListener(v -> showEvents());

        return root;
    }

    // ------------------- Month View -------------------
    private void showMonthView(YearMonth month) {
        monthTitle.setText(capitalize(month.getMonth().name()) + " " + month.getYear());

        // Prepare list with weekday headers + days
        List<String> dayItems = new ArrayList<>();
// Add headers
        for (String d : DAY_HEADERS) dayItems.add(d);

// Add empty placeholders for alignment
        LocalDate firstDay = month.atDay(1);
        int startOffset = (firstDay.getDayOfWeek().getValue() + 6) % 7; // Monday=0
        for (int i = 0; i < startOffset; i++) dayItems.add("");

// Add days
        for (int day = 1; day <= month.lengthOfMonth(); day++) dayItems.add(String.valueOf(day));

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        recyclerView.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                TextView tv = new TextView(parent.getContext());
                tv.setGravity(Gravity.CENTER);
                tv.setPadding(8, 16, 8, 16);
                return new RecyclerView.ViewHolder(tv) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ((TextView) holder.itemView).setText(dayItems.get(position));
            }

            @Override
            public int getItemCount() {
                return dayItems.size();
            }
        });
    }

    // ------------------- Year View -------------------
    private void showYearView(YearMonth currentMonth) {
        monthTitle.setText(currentMonth.getYear() + "");
        List<YearMonth> months = new ArrayList<>();
        YearMonth firstMonth = YearMonth.of(currentMonth.getYear(), 1);
        for (int i = 0; i < 12; i++) months.add(firstMonth.plusMonths(i));

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        YearAdapter yearAdapter = new YearAdapter(months, this::showMonthView);
        recyclerView.setAdapter(yearAdapter);
    }

    // ------------------- Events View -------------------
    private void showEvents() {
        monthTitle.setText("Events");

        // Example dummy events
        List<EventAdapter.Event> events = new ArrayList<>();
        events.add(new EventAdapter.Event("Meeting", "Aug 18, 2025", "Discuss project progress"));
        events.add(new EventAdapter.Event("Exam", "Aug 20, 2025", "Maths final exam"));
        events.add(new EventAdapter.Event("Birthday Party", "Aug 25, 2025", "Friend's birthday celebration"));

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        EventAdapter adapter = new EventAdapter(events);
        recyclerView.setAdapter(adapter);
    }

    private String capitalize(String s) {
        String lower = s.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
