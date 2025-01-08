package com.dellmau.edulink.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.dellmau.edulink.adapters.CurrentLessonCardAdapter;
import com.dellmau.edulink.adapters.PopularLessonCardAdapter;
import com.dellmau.edulink.models.Collaboration;
import com.dellmau.edulink.models.CurrentLessonCard;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;

public class LearnFragment extends Fragment {

    private ArrayList<CurrentLessonCard> currentLessonCards;
    private ArrayList<String> popularLessonCards;
    private ArrayList<String> currentLessonId;
    private CurrentLessonCardAdapter currentLessonCardAdapter;
    private PopularLessonCardAdapter popularLessonCardAdapter;
    private RecyclerView currentRecView;
    private RecyclerView popularRecView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView greeting;
    private SearchBar searchBar;
    ArrayList<CollectionReference> collections;
    private ArrayList<DocumentReference> collaborations;

    public LearnFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_learn, container, false);
        greeting = rootView.findViewById(R.id.greeting);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentLessonId = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        collections = new ArrayList<>(Arrays.asList(db.collection("employer"), db.collection("educator"), db.collection("student")));
        collaborations = new ArrayList<>();


        Log.d("LearnFragment", String.valueOf(R.drawable.gradient_background));

        //Current Lesson

        currentLessonCards = new ArrayList<>();
        currentLessonCardAdapter = new CurrentLessonCardAdapter(requireActivity().getSupportFragmentManager());
        currentRecView = view.findViewById(R.id.current_lesson_rec_view);
        currentRecView.setAdapter(currentLessonCardAdapter);
        currentRecView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        Log.d("LearnFragment", "RecyclerView set up with adapter.");
        fetchData();
        Log.d("learnFragment", currentLessonId.toString());


        //Popular Lesson
        popularLessonCards = new ArrayList<>();
        popularLessonCardAdapter = new PopularLessonCardAdapter(requireActivity().getSupportFragmentManager());
        popularRecView = view.findViewById(R.id.popular_lesson_rec_view);
        popularRecView.setAdapter(popularLessonCardAdapter);
        popularRecView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        // Check login streak and show dialog
//        checkAndShowLoginStreakDialog();

        searchBar = view.findViewById(R.id.search_bar);
        SearchLesson searchLesson = new SearchLesson();
        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, searchLesson)
                        .addToBackStack(null)
                        .commit();
            }
        });

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

    private void fetchData() {
        // Get the current user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        for (int i = 0; i < 3; i++) {
            int index = i;
            collections.get(i).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                Log.d("LearnFragment", document.getId().toString());
                                Log.d("LearnFragment", userId);
                                Log.d("LearnFragment", String.valueOf(index));
                                if (index == 2 && document.getId().equals(userId) ) {
                                    Log.d("LearnFragment", "yay");
                                    loadUserProfile(document.toObject(Student.class));
                                    DocumentReference organization = document.getDocumentReference("organization");
                                    fetchStudentCurrentLessonData(organization);
                                }
                                else if (index == 1 && document.getId().equals(userId)) {
                                    loadUserProfile(document.toObject(Educator.class));
                                    DocumentReference organization = document.getDocumentReference("organization");
                                    fetchStudentCurrentLessonData(organization);
                                }
                                else if (index == 0 && document.getId().equals(userId)) {
                                    loadUserProfile(document.toObject(Employer.class));
                                    DocumentReference company = document.getDocumentReference("organization");
//                                    fetchStudentCurrentLessonData(company);
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



    private void fetchStudentCurrentLessonData(DocumentReference organization) {
        // Get the current user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userReference = FirebaseFirestore.getInstance().collection("student").document(userId);
        Log.d("LearnFragment", "userReference" + userReference);

        // Fetch the current lesson data for the user
        FirebaseFirestore.getInstance()
                .collection("current_lesson")  // The collection where current lessons are stored
                .whereEqualTo("userId", userReference)  // Query by userId
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();


                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Loop through all the documents returned
                            for (DocumentSnapshot document : querySnapshot) {
                                // Deserialize the document to the CurrentLessonCard object
                                CurrentLessonCard currentLessonCard = document.toObject(CurrentLessonCard.class);
                                Log.d("LearnFragment", "currentLessonCard" + currentLessonCard);

                                // Log the fetched data to help debug
                                if (currentLessonCard != null) {
                                    Log.d("LearnFragment", "Fetched current lesson: " + currentLessonCard.getLessonId());
                                    Log.d("LearnFragment", "Progress: " + currentLessonCard.getProgress());
                                    currentLessonCards.add(currentLessonCard);
                                    currentLessonId.add(currentLessonCard.getLessonId().getId());
                                    Log.d("showing", currentLessonCard.getLessonId().getId());
                                } else {
                                    Log.d("LearnFragment", "CurrentLessonCard is null for document: " + document.getId());
                                }
                            }
                            currentLessonCardAdapter.setCard(currentLessonCards);
                            searchCollaboration(currentLessonId, organization);
//                            fetchTotalLessonData(currentLessonId, organization);
                        } else {
                            // No documents found for this user
                            Log.d("LearnFragment", "No current lessons found for user: " + userId);
                            currentLessonCardAdapter.setCard(currentLessonCards); // Ensure empty view if no current lessons
                            searchCollaboration(currentLessonId, organization);
//                            fetchTotalLessonData(null, organization); // Pass null to fetch all lessons as popular
                        }
                    } else {
                        // Error while fetching data
                        Log.e("LearnFragment", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void fetchTotalLessonData(@Nullable ArrayList<String> hold, ArrayList<DocumentReference> collaborations) {
        Log.d("LearnFragment", "get into fetchTotal");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("total_lesson").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("LearnFragment", String.valueOf(currentLessonId.size()));

                    for (int i = 0; i < collaborations.size(); i++) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Log.d("LearnFragment", "doc.getString " + doc.getDocumentReference("company").getId());
                            Log.d("LearnFragment", "docRef " + collaborations.get(i).getId());
                            if (!doc.getDocumentReference("company").getId().equals(collaborations.get(i).getId())) {
                                Log.d("LearnFragment", "return");
                                continue;
                            }
                            String docId = doc.getId();
                            Log.d("LearnFragment", docId);

                            // Add to popular if no "current lessons" or lesson is not in the "current" list
                            if (hold == null || !hold.contains(docId)) {
                                popularLessonCards.add(docId);
                                Log.d("LearnFragment", popularLessonCards.toString());
                            }
                        }
                    }

                    popularLessonCardAdapter.setCards(popularLessonCards);
                    Log.d("LearnFragment", "Popular lessons updated: " + popularLessonCards.toString());
                } else {
                    Log.e("LearnFragment", "Error getting total_lesson documents: ", task.getException());
                }
            }
        });
    }

    private void searchCollaboration(ArrayList<String> currentLessonId, DocumentReference organization) {
        FirebaseFirestore.getInstance()
                .collection("collaboration")  // The collection where current lessons are stored
                .whereEqualTo("university", organization)  // Query by userId
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Loop through all the documents returned
                            for (DocumentSnapshot document : querySnapshot) {
                                // Deserialize the document to the CurrentLessonCard object
                                DocumentReference collaboration = document.getDocumentReference("company");

                                // Log the fetched data to help debug
                                if (collaboration != null) {
                                    collaborations.add(collaboration);
                                } else {
                                    Log.d("LearnFragment", "CurrentLessonCard is null for document: " + document.getId());
                                }
                            }
                            fetchTotalLessonData(currentLessonId, collaborations);
                        } else {
                            // No documents found for this user
                            fetchTotalLessonData(null, collaborations); // Pass null to fetch all lessons as popular
                        }
                    } else {
                        // Error while fetching data
                        Log.e("LearnFragment", "Error getting documents: ", task.getException());
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
}
