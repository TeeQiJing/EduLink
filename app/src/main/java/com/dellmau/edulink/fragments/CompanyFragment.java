package com.dellmau.edulink.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dellmau.edulink.R;
import com.dellmau.edulink.adapters.CompanyAdapter;

import com.dellmau.edulink.models.Collaboration;
//import com.dellmau.edulink.models.User;
import com.dellmau.edulink.models.Educator;
import com.dellmau.edulink.models.Employer;
import com.dellmau.edulink.models.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.search.SearchBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CompanyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CompanyFragment extends Fragment {



    private ArrayList<Collaboration> collaborations;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CompanyAdapter companyAdapter;
    private RecyclerView recView;
    private ArrayList<CollectionReference> collections;
    private TextView greeting;
    private SearchBar searchBar;


    public CompanyFragment() {
        // Required empty public constructor
    }



//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_company, container, false);
        greeting = rootView.findViewById(R.id.company_greeting);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        collections = new ArrayList<>(Arrays.asList(db.collection("employer"), db.collection("educator"), db.collection("student")));


        collaborations = new ArrayList<>();
        companyAdapter = new CompanyAdapter(requireActivity().getSupportFragmentManager());
        recView = view.findViewById(R.id.company_rec_view);
//        recView.setAdapter(companyAdapter);
        recView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        Log.d("LearnFragment", "RecyclerView set up with adapter.");
        fetchData();


//        loadUserProfile();


        // Check login streak and show dialog
        checkAndShowLoginStreakDialog();

        searchBar = view.findViewById(R.id.company_search_bar);
        SearchCompany searchCompany = new SearchCompany();
        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, searchCompany)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void fetchData() {
        // Get the current user ID
        String userId = mAuth.getCurrentUser().getUid();
        for (int i = 0; i < 3; i++) {
            int index = i;
            collections.get(i).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                Log.d("company", document.getId().toString());
                                Log.d("company", userId);
                                Log.d("company", String.valueOf(index));
                                if (index == 2 && userId.equals(document.getId()) ) {
                                    Log.d("company", "yay");
                                    loadUserProfile(document.toObject(Student.class));
                                    DocumentReference company = document.getDocumentReference("organization");
                                    fetchDetails(company);
                                }
                                else if (index == 1 && userId.equals(document.getId())) {
                                    loadUserProfile(document.toObject(Educator.class));
                                    DocumentReference company = document.getDocumentReference("organization");
                                    fetchDetails(company);
                                }
                                else if (index == 0 && userId.equals(document.getId())) {
                                    loadUserProfile(document.toObject(Employer.class));
                                    DocumentReference company = document.getDocumentReference("organization");
                                    fetchDetails(company);
                                }
                            }
                        } else {
                            System.out.println("No documents found in the collection.");
                        }
                    } else {
                        System.err.println("Error getting documents: " + task.getException());
                    }
                }
            });
        }
    }

    private void fetchDetails(DocumentReference documentReference) {
        db.collection("collaboration")
                .whereEqualTo("university", documentReference)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();

                        // Log the query result
                        Log.d("details", task.toString());

                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Loop through all the documents returned
                            for (DocumentSnapshot document : querySnapshot) {
                                // Deserialize the document to the CurrentLessonCard object
                                Collaboration collaboration = document.toObject(Collaboration.class);

                                // Log the fetched data to help debug
                                if (collaboration != null) {
                                    collaborations.add(collaboration);
                                }
                            }
                            Log.d("details", collaborations.get(0).getCompany().getId());
                            companyAdapter.setCollaborations(collaborations);
                            recView.setAdapter(companyAdapter);
                        }
                    } else {
                        // Error while fetching data
                        Log.e("Fetch Details", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void loadUserProfile(Object object) {
        String userId = mAuth.getCurrentUser().getUid();

        if (object instanceof Student) {
            db.collection("student").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Student student = documentSnapshot.toObject(Student.class);
                        if (student != null) {
                            greeting.setText("Hi, " + student.getUsername());

                            // Safely handle the avatar field

                        }else {
                            greeting.setText("Hi, User");
                        }
                    } else {
                        Log.e("LearnFragment", "Document does not exist or is null");
                        Toast.makeText(getContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("LearnFragment", "Error fetching user profile", task.getException());
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if (object instanceof Educator) {
            db.collection("educator").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Educator educator = documentSnapshot.toObject(Educator.class);
                        if (educator != null) {
                            greeting.setText("Hi, " + educator.getUsername());

                            // Safely handle the avatar field

                        }else {
                            greeting.setText("Hi, User");
                        }
                    } else {
                        Log.e("LearnFragment", "Document does not exist or is null");
                        Toast.makeText(getContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("LearnFragment", "Error fetching user profile", task.getException());
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if (object instanceof Employer) {
            db.collection("employer").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Employer employer = documentSnapshot.toObject(Employer.class);
                        if (employer != null) {
                            greeting.setText("Hi, " + employer.getUsername());

                            // Safely handle the avatar field

                        }else {
                            greeting.setText("Hi, User");
                        }
                    } else {
                        Log.e("LearnFragment", "Document does not exist or is null");
                        Toast.makeText(getContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("LearnFragment", "Error fetching user profile", task.getException());
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void checkAndShowLoginStreakDialog() {
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference loginStreakRef = db.collection("login_streak").document(userId);

        // Attach a real-time listener to the login streak document
        loginStreakRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Log.e("LearnFragment", "Error listening to login streak changes", error);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                Boolean isPointCollected = documentSnapshot.getBoolean("isPointCollected");
                Long streak = documentSnapshot.getLong("streak");

                if (isPointCollected == null || streak == null) {
                    Log.e("LearnFragment", "Login streak document is missing required fields.");
                    return;
                }

                Log.d("Streak", "Real-time streak: " + streak);

                if (!isPointCollected) {
                    // Show the login streak dialog if isLogin is false
                    showLoginStreakDialog(loginStreakRef, streak);
                } else {
                    Log.d("LoginStreak", "Dialog already shown today, skipping.");
                }
            } else {
                Log.e("LearnFragment", "Login streak document does not exist.");
            }
        });
    }


    private void showLoginStreakDialog(DocumentReference loginStreakRef, long streak) {
        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_login_streak, null);

        // Initialize dialog components
        TextView streakNumberTextView = dialogView.findViewById(R.id.streak_number);
        streakNumberTextView.setText(String.valueOf(streak)); // Set streak number

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        dialogBuilder.setView(dialogView);

        AlertDialog streakDialog = dialogBuilder.create();
        streakDialog.show();

        // Collect Points button
        Button collectPointsButton = dialogView.findViewById(R.id.collect_points_button);
        int points = (int) streak * 5;
        collectPointsButton.setOnClickListener(v -> {



            String userId = mAuth.getCurrentUser().getUid();
            DocumentReference userPointsRef = db.collection("users").document(userId);

            userPointsRef.update("xp", FieldValue.increment(points))
                    .addOnSuccessListener(aVoid -> {
                        loginStreakRef.update("isPointCollected", true);
                        Log.d("LoginStreak", "isPointCollected updated to true after showing dialog");
                        Toast.makeText(requireContext(), points + " Points Collected!", Toast.LENGTH_SHORT).show();
                        streakDialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LoginStreak", "Error updating isPointCollected field", e);
                        Toast.makeText(requireContext(), "Error collecting points: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            streakDialog.dismiss();
        });
    }
}