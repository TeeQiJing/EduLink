package com.dellmau.edulink.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.search.SearchBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.dellmau.edulink.R;
import com.dellmau.edulink.adapters.SearchLessonAdapter;
import com.dellmau.edulink.models.CurrentLessonCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchLesson#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchLesson extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ArrayList<Object> lessons;
    private ArrayList<Object> filterLessons;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private SearchLessonAdapter searchLessonAdapter;
    private HashSet<String> visited;
    private Runnable searchRunnable;
    private Handler handler = new Handler(Looper.getMainLooper());
    private TextView result;
    private ImageView backButton;
    private ArrayList<CollectionReference> collections;
    private String key;
    private ArrayList<String> currentLessonId;
    private ArrayList<DocumentReference> collaborations;

    public SearchLesson() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchLesson.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchLesson newInstance(String param1, String param2) {
        SearchLesson fragment = new SearchLesson();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_lesson, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("Search", "first");

        collaborations = new ArrayList<>();
        currentLessonId = new ArrayList<>();
        key = getArguments().getString("key");
        result = view.findViewById(R.id.result);
        visited = new HashSet<>();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.result_rec_view);
        searchView = view.findViewById(R.id.search_post);
        collections = new ArrayList<>(Arrays.asList(db.collection("employer"), db.collection("educator"), db.collection("student")));


        backButton = view.findViewById(R.id.arrow);
        backButton.setOnClickListener(v -> {
            // Use FragmentManager to navigate back
            requireActivity().getSupportFragmentManager().popBackStack();
        });

//        fetchCurrentLessonData("");  // Fetch all courses initially


        searchView.setOnClickListener(v -> {
            if (searchView.getQuery().toString().isEmpty()) {
                // Clear previous data
                lessons.clear();
                visited.clear();
                filterLessons.clear();
                searchLessonAdapter.setFilteredLesson(new ArrayList<>()); // Clear adapter temporarily

                result.setText("All courses");

                // Fetch and display all lessons
                fetchData("");
            }
        });

        fetchData("");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // No action needed for submit
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable); // Cancel previous execution
                }

                searchRunnable = () -> {
                    lessons.clear();
                    collaborations.clear();
                    visited.clear();

                    if (newText.isEmpty()) {
                        // Show all lessons if query is empty
                        result.setText("All courses");
                        filterLessons.clear();
                        fetchData(""); // Fetch without filtering
                    } else {
                        Log.d("Search", "new Text: " + newText);
                        fetchData(newText);
                    }
                };

                // Execute the query after 500ms
                handler.postDelayed(searchRunnable, 500);
                return true;
            }
        });


        lessons = new ArrayList<>();
        filterLessons = new ArrayList<>();
        searchLessonAdapter = new SearchLessonAdapter(requireActivity().getSupportFragmentManager());
        recyclerView.setAdapter(searchLessonAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

    }

//    private void fetchCurrentLessonData(String query) {
//        Log.d("Search", "query current: " + query);
//
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DocumentReference userReference = FirebaseFirestore.getInstance().collection("users").document(userId);
//
//        FirebaseFirestore.getInstance()
//                .collection("current_lesson")
//                .whereEqualTo("userId", userReference)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        QuerySnapshot querySnapshot = task.getResult();
//
//                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
//                            for (DocumentSnapshot document : querySnapshot) {
//                                CurrentLessonCard currentLessonCard = document.toObject(CurrentLessonCard.class);
//
//                                if (currentLessonCard != null && !visited.contains(currentLessonCard.getLessonId().getId())) {
//                                    lessons.add(currentLessonCard);
//                                    visited.add(currentLessonCard.getLessonId().getId());
//                                }
//                            }
//                        }
//
//                        // Fetch total lessons
//                        fetchTotalLessonData(query);
//                    } else {
//                        Log.e("LearnFragment", "Error getting documents: ", task.getException());
//                    }
//                });
//    }
//
//
//    private void fetchTotalLessonData(String query) {
//        Log.d("Search", "query popular: " + query);
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("total_lesson").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    Log.d("LearnFragment", String.valueOf(lessons.size()));
//
//                    for (QueryDocumentSnapshot doc : task.getResult()) {
//                        String docId = doc.getId();
//                        Log.d("LearnFragment", docId);
//
//                        // Add to popular if no "current lessons" or lesson is not in the "current" list
//                        if (lessons == null || !visited.contains(docId)) {
//                            lessons.add(docId);
//                            visited.add(docId);
//                            Log.d("LearnFragment", lessons.toString());
//                        }
//                    }
//                    Log.d("Search", "popular lesson " + lessons.toString());
//                    filterLessons(query);
//                    Log.d("LearnFragment", "Popular lessons updated: " + lessons.toString());
//                } else {
//                    Log.e("LearnFragment", "Error getting total_lesson documents: ", task.getException());
//                }
//            }
//        });
//    }
//
//
    private void filterLessons(String query) {
        Log.d("Search", "query filter: " + query);

        if (query.isEmpty()) {
            // Show all lessons if query is empty
            searchLessonAdapter.setFilteredLesson(lessons);
            result.setText("All courses");
        } else {
            List<Task<DocumentSnapshot>> tasks = new ArrayList<>(); // List to track Firestore tasks

            for (Object lesson : lessons) {
                String lessonId;
                if (lesson instanceof CurrentLessonCard) {
                    lessonId = ((CurrentLessonCard) lesson).getLessonId().getId();
                } else {
                    lessonId = lesson.toString();
                }

                Task<DocumentSnapshot> task = FirebaseFirestore.getInstance()
                        .collection("total_lesson")
                        .document(lessonId)
                        .get();

                tasks.add(task);

                task.addOnCompleteListener(individualTask -> {
                    if (individualTask.isSuccessful()) {
                        DocumentSnapshot document = individualTask.getResult();
                        if (document.exists()) {
                            String title = document.getString("title");
                            if (title != null && title.toLowerCase().contains(query.toLowerCase()) && !filterLessons.contains(lesson)) {
                                filterLessons.add(lesson);
                            }
                        }
                    } else {
                        Log.e("FilterLessons", "Error getting document: ", individualTask.getException());
                    }
                });
            }

            Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {
                filterLessons.sort((o1, o2) -> {
                    if (o1 instanceof CurrentLessonCard && !(o2 instanceof CurrentLessonCard)) {
                        return -1;
                    } else if (!(o1 instanceof CurrentLessonCard) && o2 instanceof CurrentLessonCard) {
                        return 1;
                    } else {
                        return 0;
                    }
                });

                if (filterLessons.isEmpty()) {
                    result.setText("No Result Found");
                } else {
                    result.setText("Result");
                    searchLessonAdapter.setFilteredLesson(filterLessons);
                }
            });
        }
    }


    private void fetchData(String query) {
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
                                    DocumentReference organization = document.getDocumentReference("organization");
                                    fetchStudentCurrentLessonData(organization, query);
                                }
                                else if (index == 1 && document.getId().equals(userId)) {
                                    DocumentReference organization = document.getDocumentReference("organization");
                                    fetchStudentCurrentLessonData(organization, query);
                                }
                                else if (index == 0 && document.getId().equals(userId)) {
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



    private void fetchStudentCurrentLessonData(DocumentReference organization, String query) {
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
                                if (currentLessonCard != null && currentLessonCard.getCompany().getId().equals(key)) {
                                    Log.d("LearnFragment", "Fetched current lesson: " + currentLessonCard.getLessonId());
                                    Log.d("LearnFragment", "Progress: " + currentLessonCard.getProgress());
                                    lessons.add(currentLessonCard);
                                    currentLessonId.add(currentLessonCard.getLessonId().getId());
                                    Log.d("showing", currentLessonCard.getLessonId().getId());
                                } else {
                                    Log.d("LearnFragment", "CurrentLessonCard is null for document: " + document.getId());
                                }
                            }
                            searchCollaboration(currentLessonId, organization, query);
//                            fetchTotalLessonData(currentLessonId, organization);
                        } else {
                            // No documents found for this user
                            Log.d("LearnFragment", "No current lessons found for user: " + userId);
                            searchCollaboration(currentLessonId, organization, query);
//                            fetchTotalLessonData(null, organization); // Pass null to fetch all lessons as popular
                        }
                    } else {
                        // Error while fetching data
                        Log.e("LearnFragment", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void fetchTotalLessonData(@Nullable ArrayList<String> hold, ArrayList<DocumentReference> collaborations, String query) {
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
                                lessons.add(docId);
                            }
                        }
                    }
                    filterLessons(query);
                } else {
                    Log.e("LearnFragment", "Error getting total_lesson documents: ", task.getException());
                }
            }
        });
    }

    private void searchCollaboration(ArrayList<String> currentLessonId, DocumentReference organization, String query) {
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

                                Log.d("LearnFragment", "collaboration.getId() " + collaboration.getId());
                                Log.d("LearnFragment", "getArguments().getString(\"collaboration\" " + getArguments().getString("key"));
                                // Log the fetched data to help debug
                                if (collaboration != null && collaboration.getId().equals(getArguments().getString("key"))) {
                                    Log.d("LearnFragment", "adddddddddddddddddddddddddddddddd " );
                                    collaborations.add(collaboration);
                                } else {
                                    Log.d("LearnFragment", "CurrentLessonCard is null for document: " + document.getId());
                                }
                            }
                            fetchTotalLessonData(currentLessonId, collaborations, query);
                        } else {
                            // No documents found for this user
                            fetchTotalLessonData(null, collaborations, query); // Pass null to fetch all lessons as popular
                        }
                    } else {
                        // Error while fetching data
                        Log.e("LearnFragment", "Error getting documents: ", task.getException());
                    }
                });
    }

}