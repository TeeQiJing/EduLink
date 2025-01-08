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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

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

    public CommentFragment() {
        // Required empty public constructor
    }

    //    public static CommentFragment newInstance(String postID) {
//        CommentFragment fragment = new CommentFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_POST_ID, postID);
//        fragment.setArguments(args);
//        return fragment;
//    }
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

        // Set up upper post
        btnBack = view.findViewById(R.id.BtnBack);
        avatarPost = view.findViewById(R.id.post_user_avatar);
        usernamePost = view.findViewById(R.id.post_user_name);
        timestampPost = view.findViewById(R.id.post_time);
        titlePost = view.findViewById(R.id.post_title);
        contentPost = view.findViewById(R.id.post_content);
//        postLikes = view.findViewById(R.id.post_likes);
//        postComments = view.findViewById(R.id.post_comments);
//        likeOverlayIcon = view.findViewById(R.id.ic_liked);
        forSubmission = view.findViewById(R.id.forSubmit);
        submissionLink = view.findViewById(R.id.submissionLink);
        forSubmission.setVisibility(View.GONE);
        submissionLink.setVisibility(View.GONE);
        submit_review_btn = view.findViewById(R.id.submit_review_btn);
        lecturerSkill = view.findViewById(R.id.lecturerSkills);
        studentSkill = view.findViewById(R.id.studentSkills);
        lecturerSkill.setVisibility(View.GONE);
        studentSkill.setVisibility(View.GONE);


        loadUpperPost(post);
        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (currentUserID != null) {
            Log.d("Cert",currentUserID);

            // Check if the user exists in any of the tables (employee, student, educator)
            db.collection("employer").document(currentUserID).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            // The user is in the employee table
                            Log.d("UserCheck", "User found in employee table");
                            forSubmission.setVisibility(View.GONE);
                            submissionLink.setVisibility(View.VISIBLE);
                            lecturerSkill.setVisibility(View.VISIBLE);
                            studentSkill.setVisibility(View.VISIBLE);
                            List<String> linkArr = post.getLinkSubmitted();
                            StringBuilder sb=new StringBuilder();
                            for(int i=0;i<linkArr.size();i++){
                                sb.append(linkArr.get(i)+"\n");
                            }submissionLink.setText(sb.toString());
                            submit_review_btn.setText("Review");
                            submit_review_btn.setOnClickListener(v ->{

                            });
                        }
                        else {
                            Log.w("UserCheck", "Error checking employee table", task.getException());
                            forSubmission.setVisibility(View.VISIBLE);
                            submissionLink.setVisibility(View.GONE);
                            submit_review_btn.setText("Submit");
                            submit_review_btn.setOnClickListener(v ->{
                                String link=forSubmission.getText().toString();
                                boolean submitted = post.getPeopleSubmitted().contains(currentUserID);
                                if(submitted){
                                    Toast.makeText(getContext(),"You had already submitted",Toast.LENGTH_LONG).show();
                                }else{
                                    post.setPeopleSubmitted(db, currentUserID,link, new CommunityPost.SaveCallback() {
                                        @Override
                                        public void onSuccess(String postId) {
                                            Toast.makeText(getContext(), "Submit successful: ", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(getContext(), "Failed to update like: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }
                    });
        }

        // Update like button UI
//        updateLikeButtonUI(post, currentUserID);

//        postLikes.setOnClickListener(v -> {
//            post.toggleLike(currentUserID, db, new CommunityPost.SaveCallback() {
//                @Override
//                public void onSuccess(String postId) {
//                    updateLikeButtonUI(post, currentUserID);
//                }
//
//                @Override
//                public void onFailure(Exception e) {
//                    Toast.makeText(getContext(), "Failed to update like: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        });

        btnBack.setOnClickListener(v -> {
            // Use FragmentManager to pop the back stack and go back to the previous fragment (CommunityFragment)
            requireActivity().getSupportFragmentManager().popBackStack();
        });

//        rvcomments = view.findViewById(R.id.recycler_comments);
//        inputComment = view.findViewById(R.id.input_comment);
//        btnSendComment = view.findViewById(R.id.btn_send_comment);

//        commentAdapter = new CommentAdapter(commentList);
//        rvcomments.setLayoutManager(new LinearLayoutManager(getContext()));
//        rvcomments.setAdapter(commentAdapter);
//
//        // Load comments from Firestore
//        if (post != null && post.getPostID() != null) {
//            loadComments(post.getPostID());
//        } else {
//            Log.e(TAG, "Post is null or Post ID is missing.");
//        }

        // Handle sending new comment
//        btnSendComment.setOnClickListener(v -> {
//            String commentText = inputComment.getText().toString().trim();
//
//            if (commentText.isEmpty()) {
//                Toast.makeText(getContext(), "Please enter a comment", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//            if (currentUser == null) {
//                Toast.makeText(getContext(), "You need to be logged in to post a comment", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            String userID = currentUser.getUid();
//            String username = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Unknown User";
//            String avatarURL = currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "";
//            long timestamp = System.currentTimeMillis();
//
//            CommunityComment comment = new CommunityComment(
//                    null,  // Comment ID will be generated by Firestore
//                    userID,
//                    commentText,
//                    timestamp
//            );
//
//            // Save the comment to Firestore
//            comment.addComment(post.getPostID(), commentText, new CommunityComment.AddCommentCallback() {
//                @Override
//                public void onSuccess(DocumentReference documentReference) {
//                    Toast.makeText(getContext(), "Comment added successfully!", Toast.LENGTH_SHORT).show();
//                    inputComment.setText(""); // Clear input field
//                    loadComments(post.getPostID()); // Refresh comments
//                }
//
//                @Override
//                public void onFailure(Exception e) {
//                    Toast.makeText(getContext(), "Failed to post comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        });
//        btnSendComment.setOnClickListener(v -> {
//            String commentText = inputComment.getText().toString().trim();
//
//            if (commentText.isEmpty()) {
//                Toast.makeText(getContext(), "Please enter a comment", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//            if (currentUser == null) {
//                Toast.makeText(getContext(), "You need to be logged in to post a comment", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            String userID = currentUser.getUid();
//            String username = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Unknown Student";
//            String avatarURL = currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "";
//            long timestamp = System.currentTimeMillis();
//
//            CommunityComment comment = new CommunityComment(
//                    null,  // Comment ID will be generated by Firestore
//                    userID,
//                    commentText,
//                    timestamp
//            );
//
//            // Save the comment to Firestore
//            comment.addComment(post.getPostID(), commentText, new CommunityComment.AddCommentCallback() {
//                @Override
//                public void onSuccess(DocumentReference documentReference) {
//                    Toast.makeText(getContext(), "Comment added successfully!", Toast.LENGTH_SHORT).show();
//                    inputComment.setText(""); // Clear input field
//                    loadComments(post.getPostID()); // Refresh comments
//                }
//
//                @Override
//                public void onFailure(Exception e) {
//                    Toast.makeText(getContext(), "Failed to post comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        });
        return view;
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
//        contentPost.setText(post.getContent());
//        postLikes.setText(String.valueOf(post.getLikedBy().size()));

//        post.getCommentCount(db, new CommunityPost.FetchCommentCountCallback() {
//            @Override
//            public void onSuccess(int commentCount) {
//                postComments.setText(String.valueOf(commentCount));
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                postComments.setText("0"); // Default to 0 if fetching fails
//                Toast.makeText(getContext(), "Error loading comments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
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