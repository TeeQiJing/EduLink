package com.dellmau.edulink.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.dellmau.edulink.R;
import com.dellmau.edulink.adapters.CommunityAdapter;
import com.dellmau.edulink.models.CommunityPost;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView postsRecyclerView;
    private CommunityAdapter adapter;
    private final List<CommunityPost> postList = new ArrayList<>();
    private ViewGroup rootView; // To manage the dim background

    private LinearLayoutManager layoutManager;
    private int lastKnownScrollPosition = 0; // To store the scroll position

    public CommunityFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore instance
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_community, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize rootView for dimmed background usage
        rootView = (ViewGroup) requireActivity().findViewById(android.R.id.content);

        // Setup RecyclerView
        postsRecyclerView = view.findViewById(R.id.posts_recycler_view);
        layoutManager = new LinearLayoutManager(getContext()); // Initialize layoutManager
        postsRecyclerView.setLayoutManager(layoutManager); // Set layout manager to RecyclerView
        adapter = new CommunityAdapter(postList, db, getContext());
        postsRecyclerView.setAdapter(adapter);

        if (savedInstanceState != null) {
            lastKnownScrollPosition = savedInstanceState.getInt("scroll_position", 0);
        }
        // Set the RecyclerView's scroll position
        postsRecyclerView.scrollToPosition(lastKnownScrollPosition);

        // Load posts from Firestore
        loadPosts();

        // Setup Floating Action Button
        FloatingActionButton fabPost = view.findViewById(R.id.fab_post);
        fabPost.setVisibility(View.GONE);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d("Cert",userId);

            // Check if the user exists in any of the tables (employee, student, educator)
            db.collection("employer").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            // The user is in the employee table
                            Log.d("UserCheck", "User found in employee table");
                            fabPost.setVisibility(View.VISIBLE);
                            fabPost.setOnClickListener(v -> showNewPostPopup());
                        }
                        else {
                            Log.w("UserCheck", "Error checking employee table", task.getException());
                            fabPost.setVisibility(View.GONE);
                        }
                    });
        }


        // Setup search functionality
        SearchView searchView = view.findViewById(R.id.search_post);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPosts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // Reset the list when the search is cleared
                    adapter.updateList(new ArrayList<>(postList));
                } else {
                    filterPosts(newText);
                }
                return true;
            }
        });
    }

    private void loadPosts() {
        db.collection("community")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error loading posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        postList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String postID = doc.getId();
                            String userID = doc.getString("userID");
                            String title = doc.getString("title");
                            String content = doc.getString("content");
                            long timestamp = parseTimestamp(doc.get("timestamp"));
                            List<String> likedBy = (List<String>) doc.get("likedBy");
                            List<String> linkSubmitted = (List<String>) doc.get("linkSubmitted");
                            List<String> peopleSubmitted = (List<String>) doc.get("peopleSubmitted");
                            Map<String,Integer> lecturerSkills = (Map<String,Integer>)doc.get("lecturerSkills");
                            Map<String,Integer> studentSkills = (Map<String,Integer>)doc.get("studentSkills");
                            String startDate = doc.getString("startDate");
                            String endDate = doc.getString("endDate");
                            int numStudentRequired = doc.getLong("numStudentRequired").intValue();
                            int numEducatorRequired = doc.getLong("numEducatorRequired").intValue();

                            // Create a CommunityPost object
                            CommunityPost post = new CommunityPost(userID, title, content, timestamp, likedBy, linkSubmitted,peopleSubmitted, lecturerSkills, studentSkills,startDate,endDate,numStudentRequired, numEducatorRequired);
                            post.setPostID(postID);
                            postList.add(post);

//                            // Fetch and update the latest comment count
//                            post.fetchCommentCount(db, new CommunityPost.FetchCommentCountCallback() {
//                                @Override
//                                public void onSuccess(int commentCount) {
//                                    adapter.notifyDataSetChanged(); // Update the UI
//                                }
//
//                                @Override
//                                public void onFailure(Exception ex) {
//                                    // Log or handle the error if needed
//                                }
//                            });
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private long parseTimestamp(Object timestampObj) {
        if (timestampObj instanceof Number) {
            return ((Number) timestampObj).longValue();
        } else if (timestampObj instanceof com.google.firebase.Timestamp) {
            return ((com.google.firebase.Timestamp) timestampObj).toDate().getTime();
        } else if (timestampObj instanceof String) {
            try {
                return Long.parseLong((String) timestampObj);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0; // Default fallback
    }

    private void showNewPostPopup() {
        // Inflate the popup layout
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_new_post, null);

        // Create the PopupWindow
        final PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        // Create and add a dim background layer
        View dimBackgroundView = new View(getContext());
        dimBackgroundView.setBackgroundColor(requireContext().getColor(R.color.popup_background_dim));
        dimBackgroundView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        rootView.addView(dimBackgroundView);

        // Show the popup window at the center
        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0);

        // Remove dim background on popup dismiss
        popupWindow.setOnDismissListener(() -> rootView.removeView(dimBackgroundView));

        // Initialize popup views
        EditText postTitle = popupView.findViewById(R.id.project_title);
        Spinner lecturerSkills = popupView.findViewById(R.id.lecturer_skill);
        Spinner studentSkills = popupView.findViewById(R.id.student_skill);
        EditText start_date = popupView.findViewById(R.id.start_date);
        EditText end_date = popupView.findViewById(R.id.end_date);
        EditText postContent = popupView.findViewById(R.id.project_description);
        ImageButton exitButton = popupView.findViewById(R.id.popup_exit);
        Button postButton = popupView.findViewById(R.id.submit_review_btn);
        EditText suggestedLecturerSkill = popupView.findViewById(R.id.suggestedLecturerSkill);
        EditText suggestedStudentSkill = popupView.findViewById(R.id.suggestedStudentSkill);
        EditText numStudentRequired = popupView.findViewById(R.id.numStudentRequired);
        EditText numEducatorRequired = popupView.findViewById(R.id.numEducatorRequired);

        ArrayAdapter<CharSequence> lecture_adapter =ArrayAdapter.createFromResource(
                getActivity(),
                R.array.lecturer_skills_array,
                android.R.layout.simple_spinner_item
        );
        lecture_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lecturerSkills.setAdapter(lecture_adapter);

        ArrayAdapter<CharSequence> student_adapter =ArrayAdapter.createFromResource(
                getActivity(),
                R.array.student_skills_array,
                android.R.layout.simple_spinner_item
        );
        student_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        studentSkills.setAdapter(student_adapter);

        // Exit button click listener
        exitButton.setOnClickListener(v -> popupWindow.dismiss());

        // Post button click listener
        postButton.setOnClickListener(v -> {
            String title = postTitle.getText().toString().trim();
            String content = postContent.getText().toString().trim();
            String lecSkill = lecturerSkills.getSelectedItem().toString().trim();
            String studentSkill = studentSkills.getSelectedItem().toString().trim();
            String startDate = start_date.getText().toString().trim();
            String endDate = end_date.getText().toString().trim();
            int suggestedLecSkill = Integer.parseInt(suggestedLecturerSkill.getText().toString()); // Assuming it's directly an int
            int suggestedStuSkill = Integer.parseInt(suggestedStudentSkill.getText().toString()); // Assuming it's directly an int
            int numStudents = Integer.parseInt(numStudentRequired.getText().toString());          // Assuming it's directly an int
            int numEducators = Integer.parseInt(numEducatorRequired.getText().toString());       // Assuming it's directly an int


            if (title.isEmpty() || lecSkill.isEmpty() || studentSkill.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || content.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, Integer> lecturerSkillsMap = new HashMap<>();
            lecturerSkillsMap.put(lecSkill, suggestedLecSkill);

            Map<String, Integer> studentSkillsMap = new HashMap<>();
            studentSkillsMap.put(studentSkill, suggestedStuSkill);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String userID = currentUser != null ? currentUser.getUid() : "Unknown Student";
            long timestamp = System.currentTimeMillis();

            CommunityPost post = new CommunityPost(userID, title, content, timestamp, new ArrayList<>(),new ArrayList<>(), new ArrayList<>(), lecturerSkillsMap,studentSkillsMap,startDate,endDate,numStudents,numEducators);
            post.saveToFirebase(db, new CommunityPost.SaveCallback() {
                @Override
                public void onSuccess(String postId) {
                    Toast.makeText(getContext(), "Post added successfully!", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                    rootView.removeView(dimBackgroundView);
                    //loadPosts(); // Refresh posts
                    // Update the post with the generated post ID
                    post.setPostID(postId);
                    // Notify the adapter about the new post
                    adapter.notifyDataSetChanged();
                    // Scroll to the top of the list to show the new post
                    postsRecyclerView.scrollToPosition(0);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed to add post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void filterPosts(String query) {
        List<CommunityPost> filteredPosts = new ArrayList<>();
        for (CommunityPost post : postList) {
            String title = post.getTitle() != null ? post.getTitle().toLowerCase() : ""; // Default to empty string if null
            String content = post.getContent() != null ? post.getContent().toLowerCase() : ""; // Default to empty string if null
            String username = post.getUsername() != null ? post.getUsername().toLowerCase() : ""; // Default to empty string if null

            // Check if query matches any field
            if (title.contains(query.toLowerCase()) || content.contains(query.toLowerCase()) || username.contains(query.toLowerCase())) {
                filteredPosts.add(post);
            }
        }
        adapter.updateList(filteredPosts);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Ensure layoutManager is not null before calling findFirstVisibleItemPosition
        if (layoutManager != null) {
            lastKnownScrollPosition = layoutManager.findFirstVisibleItemPosition();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("scroll_position", lastKnownScrollPosition);
    }
}