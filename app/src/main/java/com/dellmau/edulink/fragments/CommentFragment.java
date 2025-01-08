package com.dellmau.edulink.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dellmau.edulink.R;
import com.dellmau.edulink.adapters.CommentAdapter;
import com.dellmau.edulink.models.CommunityComment;
import com.dellmau.edulink.models.CommunityPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CommentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommentFragment extends Fragment {
    private static final String TAG = "CommentFragment";
    private static final String ARG_POST_ID = "postID";
    private RecyclerView rvcomments;
    private CommentAdapter commentAdapter;
    private List<CommunityComment> commentList = new ArrayList<>();
    private FirebaseFirestore db;

    private CommunityPost post;
    private ImageView avatarPost,likeOverlayIcon;
    private TextView usernamePost, timestampPost, titlePost, contentPost, submissionLink;
    private ImageButton btnBack;
    private EditText inputComment,forSubmission;
    private Button submit_review_btn,postLikes,postComments;
    private LinearLayout lecturerSkill, studentSkill;
    private List<String> linkArr,peoArr,peo;

    public CommentFragment() {
        // Required empty public constructor
    }

    public static CommentFragment newInstance(CommunityPost post) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putSerializable("post", post);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            post = (CommunityPost) getArguments().getSerializable("post");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comment, container, false);

        db = FirebaseFirestore.getInstance();

        // Set up upper post UI elements
        SeekBar seekBar5 = view.findViewById(R.id.seekBar5);
        TextView lecturerScore = view.findViewById(R.id.lecturerScore);
        SeekBar seekBar4 = view.findViewById(R.id.seekBar4);
        TextView studentScore = view.findViewById(R.id.studentScore);
        btnBack = view.findViewById(R.id.BtnBack);
        avatarPost = view.findViewById(R.id.post_user_avatar);
        usernamePost = view.findViewById(R.id.post_user_name);
        timestampPost = view.findViewById(R.id.post_time);
        titlePost = view.findViewById(R.id.post_title);
        contentPost = view.findViewById(R.id.post_content);
        forSubmission = view.findViewById(R.id.forSubmit);
        submissionLink = view.findViewById(R.id.submissionLink);
        forSubmission.setVisibility(View.GONE);
        submissionLink.setVisibility(View.GONE);
        submit_review_btn = view.findViewById(R.id.submit_review_btn);
        lecturerSkill = view.findViewById(R.id.lecturerSkills);
        studentSkill = view.findViewById(R.id.studentSkills);
        lecturerSkill.setVisibility(View.GONE);
        studentSkill.setVisibility(View.GONE);

        setupSeekBar(seekBar5, lecturerScore);
        setupSeekBar(seekBar4, studentScore);
        loadUpperPost(post);

        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (currentUserID != null) {
            Log.d("Cert", currentUserID);

            // Check if the user exists in the employer table
            db.collection("employer").document(currentUserID).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            // The user is in the employer table
                            Log.d("UserCheck", "User found in employer table");
                            forSubmission.setVisibility(View.GONE);
                            submissionLink.setVisibility(View.VISIBLE);
                            lecturerSkill.setVisibility(View.VISIBLE);
                            studentSkill.setVisibility(View.VISIBLE);

                            linkArr = post.getLinkSubmitted();
                            peoArr = post.getPeopleSubmitted();
                            StringBuilder sb = new StringBuilder();
                            peo = new ArrayList<>();

                            // Create a counter to track when both queries are done
                            final int[] queryCount = {0};

                            // Query the "student" collection
                            db.collection("student").get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    for (QueryDocumentSnapshot doc : task1.getResult()) {
                                        String userId = doc.getId();
                                        Log.d("Cert", userId);
                                        if (peoArr.contains(userId)) {
                                            peo.add(doc.getString("username"));
                                        }
                                    }
                                }
                                queryCount[0]++;
                                if (queryCount[0] == 2) {
                                    // Once both queries have finished
                                    updateSubmissionLink(linkArr, peo);
                                }
                            });

                            // Query the "educator" collection
                            db.collection("educator").get().addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    for (QueryDocumentSnapshot doc : task2.getResult()) {
                                        String userId = doc.getId();
                                        Log.d("Cert", "Educator UserId: " + userId);  // Log the educator ID
                                        if (peoArr.contains(userId)) {
                                            Log.d("Cert", "Educator found: " + doc.getString("username"));  // Log educator username
                                            peo.add(doc.getString("username"));
                                        }
                                    }
                                } else {
                                    Log.d("Cert", "Error fetching educator data: " + task2.getException());  // Log any errors in fetching educator data
                                }
                                queryCount[0]++;
                                if (queryCount[0] == 2) {
                                    // Once both queries have finished
                                    updateSubmissionLink(linkArr, peo);
                                }
                            });



                            submit_review_btn.setText("Review");
                            submit_review_btn.setOnClickListener(v -> {
                                // Log the scores of lecturer and student when the "Review" button is pressed
                                int lecturerScoreValue = seekBar5.getProgress();
                                int studentScoreValue = seekBar4.getProgress();

                                Log.d("ReviewButton", "Lecturer Score: " + lecturerScoreValue);
                                Log.d("ReviewButton", "Student Score: " + studentScoreValue);

                                // Optionally, you can use these scores further as needed
                                Toast.makeText(getContext(), "Scores logged: Lecturer - " + lecturerScoreValue + ", Student - " + studentScoreValue, Toast.LENGTH_SHORT).show();

//                                updateSkillPoints("lecturerSkillsKey", lecturerScoreValue);
//                                updateSkillPoints("studentSkillKey", studentScoreValue);

                                List<String> peopleSubmitted = post.getPeopleSubmitted();
                                Map<String,Integer>  studentSkill = post.getStudentSkills();
                                Map<String,Integer>  educatorSkill = post.getLecturerSkills();
                                // Extract the key from studentSkill (assuming the map has only one entry)
                                String studentSkillKey = studentSkill.isEmpty() ? null : studentSkill.entrySet().iterator().next().getKey();

                                // Extract the key from educatorSkill (assuming the map has only one entry)
                                String educatorSkillKey = educatorSkill.isEmpty() ? null : educatorSkill.entrySet().iterator().next().getKey();
                                for (String userId : peopleSubmitted) {
                                    // Check if the user is a student or educator
                                    db.collection("student").document(userId).get()
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful() && task1.getResult().exists()) {
                                                    // User is a student
                                                    Log.d("Cert", "Student found: " + userId);

                                                    // Update the student's skill points if they match the target skill
                                                    db.collection("student").document(userId)
                                                            .update("skill_point." + studentSkillKey, FieldValue.increment(studentScoreValue))  // Increment the student's skill points
                                                            .addOnSuccessListener(aVoid -> {
                                                                Log.d("Cert", "Student skill points updated successfully.");
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e("Cert", "Error updating student skill points", e);
                                                            });
                                                } else {
                                                    // Check if the user is an educator (if not a student)
                                                    db.collection("educator").document(userId)
                                                            .update("skill_point." + educatorSkillKey, FieldValue.increment(lecturerScoreValue))  // Increment the student's skill points
                                                            .addOnSuccessListener(aVoid -> {
                                                                Log.d("Cert", "Lecturer skill points updated successfully.");
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e("Cert", "Error updating lecturer skill points", e);
                                                            });
                                                }
                                            });
                                }

                            });
                        } else {
                            Log.w("UserCheck", "Error checking employer table", task.getException());
                            forSubmission.setVisibility(View.VISIBLE);
                            submissionLink.setVisibility(View.GONE);
                            submit_review_btn.setText("Submit");
                            submit_review_btn.setOnClickListener(v -> {
                                String link = forSubmission.getText().toString();
                                boolean submitted = post.getPeopleSubmitted().contains(currentUserID);
                                if (submitted) {
                                    Toast.makeText(getContext(), "You have already submitted", Toast.LENGTH_LONG).show();
                                } else {
                                    post.setPeopleSubmitted(db, currentUserID, link, new CommunityPost.SaveCallback() {
                                        @Override
                                        public void onSuccess(String postId) {
                                            Toast.makeText(getContext(), "Submit successful!", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(getContext(), "Failed to update submission: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }
                    });
        }

        // Back button action
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    private void updateSkillPoints(String skillKey, int score) {
        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (currentUserID != null) {
            // Assuming skill_point is a map in the Firestore user document
            Map<String, Object> skillMap = new HashMap<>();
            skillMap.put(skillKey, score);  // Add the score to the skill map

            // Update Firestore with the new skill points

        }
    }
    // Method to update the submission link once both queries are completed
    private void updateSubmissionLink(List<String> linkArr, List<String> peo) {
        StringBuilder sb = new StringBuilder();
        for (String name : peo) {
            sb.append(name).append("\n");
        }
        if (!linkArr.isEmpty()) {
            sb.append(linkArr.get(0));  // Append the first link if available
        }
        submissionLink.setText(sb.toString());  // Update the UI with the result
    }

    private void setupSeekBar(SeekBar seekBar, TextView lecturerScore) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the TextView with the current progress
                lecturerScore.setText(String.valueOf(progress));

                // Optionally, update the Firestore database with the new score in real-time
//                String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                if (currentUserID != null) {
//                    db.collection("user_scores")
//                            .document(currentUserID)
//                            .update("lecturer_score", progress)
//                            .addOnSuccessListener(aVoid -> Log.d("ScoreUpdate", "Score updated in Firestore"))
//                            .addOnFailureListener(e -> Log.e("ScoreUpdate", "Error updating score", e));
//                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optional: Handle when user starts dragging
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Optional: Handle when user stops dragging
            }
        });
    }

    private void loadComments(String postID) {
        if (postID == null) {
            Log.e(TAG, "Post ID is null. Cannot load comments.");
            return;
        }
        CollectionReference commentsRef = db.collection("community").document(postID).collection("comments");

        commentsRef.orderBy("commentTimestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    commentList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        CommunityComment comment = doc.toObject(CommunityComment.class);
                        if (comment != null) {
                            commentList.add(comment);
                        }
                    }
                    commentAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading comments", e));
    }

    private void loadUpperPost(CommunityPost post){
        db = FirebaseFirestore.getInstance();
        post.fetchUserDetails(db, new CommunityPost.UserDetailsCallback() {
            @Override
            public void onSuccess(String username, String avatarUrl) {
                usernamePost.setText(username);
                displayAvatar(avatarUrl, avatarPost);
            }

            @Override
            public void onFailure(Exception e) {
                usernamePost.setText("Unknown Student");
                avatarPost.setImageResource(R.drawable.gradient_background);
            }
        });

        timestampPost.setText("· " + post.getTimeAgo());
        titlePost.setText(post.getTitle());

    }

    private void displayAvatar(String avatarBase64, ImageView imageView) {
        if (avatarBase64 != null && !avatarBase64.isEmpty()) {
            try {
                byte[] decodedBytes = android.util.Base64.decode(avatarBase64, android.util.Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                imageView.setImageResource(R.drawable.gradient_background); // Fallback to default avatar
            }
        } else {
            imageView.setImageResource(R.drawable.gradient_background); // Fallback to default avatar
        }
    }

    private void updateLikeButtonUI(CommunityPost post, String currentUserID) {
        boolean isLiked = post.getLikedBy().contains(currentUserID);
        if (isLiked) {
            postLikes.setBackgroundColor(postLikes.getContext().getResources().getColor(R.color.liked));
            postLikes.setTextColor(ContextCompat.getColor(postLikes.getContext(), android.R.color.white)); // White text
            likeOverlayIcon.setVisibility(View.VISIBLE);
        } else {
            postLikes.setBackgroundColor(postLikes.getContext().getResources().getColor(R.color.unliked));
            postLikes.setTextColor(ContextCompat.getColor(postLikes.getContext(), R.color.blackIconTint)); // Black text
            likeOverlayIcon.setVisibility(View.GONE);
        }
        postLikes.setText(String.valueOf(post.getLikedBy().size())); // Update like count
    }

}