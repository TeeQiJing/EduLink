package com.dellmau.edulink.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dellmau.edulink.R;
import com.dellmau.edulink.adapters.BadgeAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BadgeFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private BadgeAdapter adapter;
    private List<String> badgeNames = new ArrayList<>();

    public BadgeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_badge, container, false);

        // Initialize the toolbar
        MaterialToolbar toolbar = rootView.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // Setup RecyclerView
        recyclerView = rootView.findViewById(R.id.badgeRecycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BadgeAdapter(badgeNames);
        recyclerView.setAdapter(adapter);

        // Fetch badges from Firestore
        fetchBadges();

        return rootView;
    }
    private void fetchBadges() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("user_badges")
                .whereEqualTo("userIdRef", db.collection("users").document(userId))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> fetchedBadgeNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DocumentReference badgeIdRef = document.getDocumentReference("badgeIdRef");
                            if (badgeIdRef != null) {
                                badgeIdRef.get().addOnCompleteListener(badgeTask -> {
                                    if (badgeTask.isSuccessful() && badgeTask.getResult() != null) {
                                        String badgeName = badgeTask.getResult().getString("badges_name");
                                        if (badgeName != null) {
                                            fetchedBadgeNames.add(badgeName);
                                        }
                                    }

                                    // Update the adapter once all badges are fetched
                                    if (fetchedBadgeNames.size() == task.getResult().size()) {
                                        badgeNames.clear();
                                        badgeNames.addAll(fetchedBadgeNames);
                                        adapter.notifyDataSetChanged(); // Notify the adapter
                                    }
                                });
                            }
                        }
                    } else {
                        Log.w("BadgeFragment", "Error getting badges.", task.getException());
                    }
                });
    }



}