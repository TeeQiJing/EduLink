package com.dellmau.edulink.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.dellmau.edulink.R;
import com.dellmau.edulink.adapters.SearchLessonAdapter;
import com.dellmau.edulink.models.CurrentLessonCard;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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


        result = view.findViewById(R.id.result);
        visited = new HashSet<>();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.result_rec_view);
        searchView = view.findViewById(R.id.search_post);

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
                    // Clear the filtered list
                    lessons.clear();
                    visited.clear();
                    filterLessons.clear();


                    if (newText.equals("")) {
                        result.setText("No Result Found");
                        filterLessons.clear();
                    }
                    else {
                        Log.d("Search", "new Text: " + newText);
                        Log.d("Search", "query text change");
                        fetchCurrentLessonData(newText);
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

    private void fetchCurrentLessonData(String query) {
        Log.d("Search", "query current: " + query);
        // Get the current user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userReference = FirebaseFirestore.getInstance().collection("users").document(userId);

        // Fetch the current lesson data for the user
        FirebaseFirestore.getInstance()
                .collection("current_lesson")  // The collection where current lessons are stored
                .whereEqualTo("userId", userReference)  // Query by userId
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();

                        // Log the query result
                        Log.d("SearchFragment", "Query successful, found " + querySnapshot.size() + " documents.");

                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Loop through all the documents returned
                            for (DocumentSnapshot document : querySnapshot) {
                                // Deserialize the document to the CurrentLessonCard object
                                CurrentLessonCard currentLessonCard = document.toObject(CurrentLessonCard.class);

                                // Log the fetched data to help debug
                                if (currentLessonCard != null && !visited.contains(currentLessonCard.getLessonId().getId())) {
                                    Log.d("LearnFragment", "Fetched current lesson: " + currentLessonCard.getLessonId());
                                    Log.d("LearnFragment", "Progress: " + currentLessonCard.getProgress());
                                    lessons.add(currentLessonCard);
                                    visited.add(currentLessonCard.getLessonId().getId());
                                    Log.d("Search", currentLessonCard.getLessonId().toString());
                                } else {
                                    Log.d("LearnFragment", "CurrentLessonCard is null for document: " + document.getId());
                                }
                            }
                            Log.d("Search", "current lesson: " + lessons.toString());
                            fetchTotalLessonData(query);
                        } else {
                            // No documents found for this user
                            Log.d("LearnFragment", "No current lessons found for user: " + userId);
                            fetchTotalLessonData(query); // Pass null to fetch all lessons as popular
                        }
                    } else {
                        // Error while fetching data
                        Log.e("LearnFragment", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void fetchTotalLessonData(String query) {
        Log.d("Search", "query popular: " + query);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("total_lesson").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("LearnFragment", String.valueOf(lessons.size()));

                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        String docId = doc.getId();
                        Log.d("LearnFragment", docId);

                        // Add to popular if no "current lessons" or lesson is not in the "current" list
                        if (lessons == null || !visited.contains(docId)) {
                            lessons.add(docId);
                            visited.add(docId);
                            Log.d("LearnFragment", lessons.toString());
                        }
                    }
                    Log.d("Search", "popular lesson " + lessons.toString());
                    filterLessons(query);
                    Log.d("LearnFragment", "Popular lessons updated: " + lessons.toString());
                } else {
                    Log.e("LearnFragment", "Error getting total_lesson documents: ", task.getException());
                }
            }
        });
    }
    private void filterLessons(String query) {
        Log.d("Search", "query filter: " + query);


        if (query.isEmpty()) {
            // If no search query, return all lesson IDs
            searchLessonAdapter.setFilteredLesson(lessons);
            result.setText(""); // Clear the result text when showing all lessons
        } else {
            List<Task<DocumentSnapshot>> tasks = new ArrayList<>(); // List to track Firestore tasks

            for (Object lesson : lessons) {
                String lessonId;
                if (lesson instanceof CurrentLessonCard) {
                    lessonId = ((CurrentLessonCard) lesson).getLessonId().getId();
                } else {
                    lessonId = lesson.toString();
                }

                // Create a Firestore task for each lesson
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
                            Log.d("search", title);
                            if (title != null && title.toLowerCase().contains(query.toLowerCase()) && !filterLessons.contains(lesson)) {
                                filterLessons.add(lesson); // Maintain original order by adding to filterLessons
                                Log.d("search", "update filter:" + filterLessons);
                            }
                        }
                    } else {
                        Log.e("FilterLessons", "Error getting document: ", individualTask.getException());
                    }
                });
            }

            // Wait for all Firestore tasks to complete
            Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {
                // Sort the filtered list: CurrentLessonCard objects first, then Strings
                filterLessons.sort((o1, o2) -> {
                    if (o1 instanceof CurrentLessonCard && !(o2 instanceof CurrentLessonCard)) {
                        return -1; // Place CurrentLessonCard before plain strings
                    } else if (!(o1 instanceof CurrentLessonCard) && o2 instanceof CurrentLessonCard) {
                        return 1; // Place strings after CurrentLessonCard
                    } else {
                        return 0; // Maintain relative order for same type
                    }
                });

                if (filterLessons.isEmpty()) {
                    result.setText("No Result Found");
                } else {
                    result.setText("Result");
                    searchLessonAdapter.setFilteredLesson(filterLessons); // Update adapter with ordered results
                }
                Log.d("Search", "Filtered lessons: " + filterLessons.toString());
            });
        }
    }

}