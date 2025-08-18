package com.group.campus;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class Events extends Fragment {
    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText startTimeEditText;
    private EditText endTimeEditText;
    private EditText locationEditText;
    private Button createEventButton;

    public Events() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events, container, false);
//        // Initialize UI fields
//        titleEditText = view.findViewById(R.id.title);
//        descriptionEditText = view.findViewById(R.id.description);
//        startTimeEditText = view.findViewById(R.id.start_time);
//        endTimeEditText = view.findViewById(R.id.end_time);
//        locationEditText = view.findViewById(R.id.location);
//        createEventButton = view.findViewById(R.id.btn_events);

        return view;
    }
}