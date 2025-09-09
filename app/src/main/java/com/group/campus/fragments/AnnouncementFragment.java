package com.group.campus.fragments;

import static android.view.View.GONE;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchView;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.group.campus.AnnouncementViewActivity;
import com.group.campus.R;
import com.group.campus.adapters.AnnouncementsAdapter;
import com.group.campus.adapters.SearchAnnouncementsAdapter;
import com.group.campus.dialogs.AddAnnouncementDialog;
import com.group.campus.dialogs.AiQueryDialog;
import com.group.campus.models.Announcement;
import com.group.campus.models.User;
import com.group.campus.service.FBMessagingService;
import com.group.campus.service.UserRoleService;
import com.group.campus.utils.AlgoliaApi;
import com.group.campus.utils.AlgoliaClient;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AnnouncementFragment extends Fragment {

    private AnnouncementsAdapter adapter;
    private SearchAnnouncementsAdapter searchAdapter;
    private RecyclerView recyclerView, rvSearch;
    private FirebaseFirestore db;
    private List<Announcement> announcementList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAddAnnouncement;
    private FloatingActionButton fabAi;
    private static final String TAG = "AnnouncementFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_announcement, container, false);

        recyclerView = view.findViewById(R.id.recyclerView_announcements);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        SearchView searchView = view.findViewById(R.id.searchView);
        rvSearch = view.findViewById(R.id.rvSearch);
        fabAddAnnouncement = view.findViewById(R.id.fab_add_announcement);
        fabAi = view.findViewById(R.id.fab_ai);

        fabAddAnnouncement.setVisibility(GONE);

        db = FirebaseFirestore.getInstance();

        announcementList = new ArrayList<>();

        UserRoleService roleService = new UserRoleService();

        roleService.getCurrentUser(new UserRoleService.UserCallback() {
            @Override
            public void onUserLoaded(User user) {
                if (roleService.isStaff(user)) {
                    fabAddAnnouncement.setVisibility(View.VISIBLE);
                    fabAddAnnouncement.setOnClickListener(v -> {
                        AddAnnouncementDialog dialog = AddAnnouncementDialog.newInstance();
                        dialog.show(getParentFragmentManager(), "AddAnnouncementDialog");
                    });

                } else {
                    fabAddAnnouncement.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {
                // Hide FAB on error for security
                fabAddAnnouncement.setVisibility(View.GONE);
            }
        });

        adapter = new AnnouncementsAdapter(announcementList, v -> {
            int position = recyclerView.getChildAdapterPosition(v);
            if (position != RecyclerView.NO_POSITION && position < announcementList.size()) {
                Announcement announcement = announcementList.get(position);
                openAnnouncementView(announcement);
            }
        });

        searchAdapter = new SearchAnnouncementsAdapter(new ArrayList<>(), v-> {
            int position = rvSearch.getChildAdapterPosition(v);
            if (position != RecyclerView.NO_POSITION) {
                // Handle search adapter click if needed
                Toast.makeText(requireContext(), "Search Item Clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        rvSearch.setAdapter(searchAdapter);
        rvSearch.setLayoutManager(new LinearLayoutManager(view.getContext()));

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        swipeRefreshLayout.setRefreshing(true);
        loadAnnouncements();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadAnnouncements();
            }
        });

        EditText editText = searchView.getEditText();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                searchAlgolia(s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        // Setup FAB click listener

        fabAi.setOnClickListener(v -> {
            AiQueryDialog dialog = AiQueryDialog.newInstance();
            dialog.show(getParentFragmentManager(), "AiQueryDialog");
        });



        return view;
    }

    private void searchAlgolia(String queryText) {
        AlgoliaApi service = AlgoliaClient.getService();

        JsonObject body = new JsonObject();
        body.addProperty("query", queryText);
        body.addProperty("hitsPerPage", 10);

        Call<JsonObject> call = service.search("dev_ANNOUNCEMENT", body);
        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Gson gson = new Gson();
                    JsonObject result = response.body();
                    System.out.println("Algolia response: " + result.toString());
                    Type listType = new TypeToken<List<Announcement>>(){}.getType();
                    List<Announcement> announcements = gson.fromJson(
                            result.getAsJsonArray("hits").toString(),
                            listType
                    );
                    searchAdapter.updateList(announcements);

                } else {
                    System.err.println("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }


    private void loadAnnouncements() {
        db.collection("announcement").orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (!isAdded() || getActivity() == null || isDetached()) {
                            return;
                        }

                        if (error != null) {
                            Log.e(TAG, "Listen failed.", error);
                            Toast.makeText(requireContext(), "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        swipeRefreshLayout.setRefreshing(false);

                        if (value != null && !value.isEmpty()) {
                            List<Announcement> announcements = new ArrayList<>();
                            for (DocumentSnapshot document : value.getDocuments()) {
                                try {
                                    Announcement announcement = document.toObject(Announcement.class);
                                    if (announcement != null) {
                                        announcement.setId(document.getId());
                                        announcements.add(announcement);
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(requireContext(), "Error parsing announcement" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("FIREBASE_ERROR", e.getMessage() != null ? e.getMessage() : "Something went wrong");
                                }
                            }

                            adapter.updateList(announcements);

                            if (announcementList.isEmpty()) {
                                Toast.makeText(requireContext(), "List Empty", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            adapter.updateList(new ArrayList<>());
                            Toast.makeText(requireContext(), "No announcements found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void openAnnouncementView(Announcement announcement) {
        Intent intent = new Intent(requireContext(), AnnouncementViewActivity.class);
        intent.putExtra("announcement", announcement);
        startActivity(intent);
    }
}