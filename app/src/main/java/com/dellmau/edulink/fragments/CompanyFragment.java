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
import com.dellmau.edulink.adapters.CurrentLessonCardAdapter;
import com.dellmau.edulink.adapters.PopularLessonCardAdapter;
import com.dellmau.edulink.models.Collaboration;
import com.dellmau.edulink.models.CurrentLessonCard;
import com.dellmau.edulink.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CompanyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CompanyFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ArrayList<Collaboration> collaborations;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CompanyAdapter companyAdapter;
    private RecyclerView recView;
    private ArrayList<CollectionReference> collections;
    private TextView greeting;


    public CompanyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CompanyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CompanyFragment newInstance(String param1, String param2) {
        CompanyFragment fragment = new CompanyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

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
        collections = new ArrayList<>();

        collaborations = new ArrayList<>();
        companyAdapter = new CompanyAdapter(requireActivity().getSupportFragmentManager());
        recView = view.findViewById(R.id.company_rec_view);
        companyAdapter.setCollaborations(collaborations);
        recView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        Log.d("LearnFragment", "RecyclerView set up with adapter.");
        fetchData();


        loadUserProfile();


        // Check login streak and show dialog
        checkAndShowLoginStreakDialog();

//        searchBar = view.findViewById(R.id.search_bar);
//        SearchLesson searchLesson = new SearchLesson();
//        searchBar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                requireActivity().getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.fragment_container, searchLesson)
//                        .addToBackStack(null)
//                        .commit();
//            }
//        });
    }

    private void fetchData() {
        // Get the current user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        for (int i = 0; i < 3; i++) {
            String collection = collections.get(i).getId().toString();
            db.collection(collection).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                if (document.getId().equals("/student/" + userId) || document.getId().equals("/educator/" + userId) || document.getId().equals("/employer/" + userId)) {
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
                            companyAdapter.setCollaborations(collaborations);
                        }
                    } else {
                        // Error while fetching data
                        Log.e("Fetch Details", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void loadUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userDocRef = db.collection("users").document(userId);

        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        greeting.setText("Hi, " + user.getUsername());

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