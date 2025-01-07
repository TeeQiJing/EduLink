package com.dellmau.edulink.models;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CommunityComment {
    private String commentID;
    private String commentUserID;
    private String commentContent;
    private long commentTimestamp;
    private String username;
    private String avatarURL;

    public CommunityComment() {}

    public CommunityComment(String commentID, String commentUserID, String comment, long commentTimestamp) {
        this.commentID = commentID;
        this.commentUserID = commentUserID;
        this.commentContent = comment;
        this.commentTimestamp = commentTimestamp;
//        this.username = username;
//        this.avatarURL = avatarURL;
    }

    public String getCommentID() { return commentID; }
    public void setCommentID(String commentID) { this.commentID = commentID; }

    public String getCommentUserID() { return commentUserID; }
    public void setCommentUserID(String commentUserID) { this.commentUserID = commentUserID; }

    public String getCommentContent() { return commentContent; }
    public void setCommentContent(String comment) { this.commentContent = comment; }

    public long getCommentTimestamp() { return commentTimestamp; }
    public void setCommentTimestamp(long commentTimestamp) { this.commentTimestamp = commentTimestamp; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatarURL() { return avatarURL; }
    public void setAvatarURL(String avatarURL) { this.avatarURL = avatarURL; }

    public static void addComment(String postID, String commentContent, AddCommentCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String commentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long commentTimestamp = System.currentTimeMillis();

        Map<String, Object> commentData = new HashMap<>();
        commentData.put("commentUserID", commentUserID);
        commentData.put("commentContent", commentContent);
        commentData.put("commentTimestamp", commentTimestamp);

        db.collection("community").document(postID).collection("comments")
                .add(commentData)
                .addOnSuccessListener(documentReference -> {
                    String commentID = documentReference.getId();
                    documentReference.update("commentID", commentID) // Add the commentID field
                            .addOnSuccessListener(aVoid -> {
                                if (callback != null) callback.onSuccess(documentReference);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("CommunityComment", "Error adding comment", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    // Callback interface for comment operations
    public interface AddCommentCallback {
        void onSuccess(DocumentReference documentReference);
        void onFailure(Exception e);
    }

    // Fetch username and avatar from the 'users' collection
    public void fetchUserDetails(FirebaseFirestore db, UserDetailsCallback callback) {
        Log.d("User ID", commentUserID);
        db.collection("users").document(commentUserID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        this.username = documentSnapshot.getString("username");
                        this.avatarURL = documentSnapshot.getString("avatar");
                        if (callback != null) callback.onSuccess(username, avatarURL);
                    } else {
                        if (callback != null) callback.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Callback interface for fetching user details
    public interface UserDetailsCallback {
        void onSuccess(String username, String avatarUrl);

        void onFailure(Exception e);
    }

    // Calculate how long ago the post was created
    public String getTimeAgo() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - commentTimestamp;

        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;

        if (years > 0) return years + (years == 1 ? " year ago" : " years ago");
        if (months > 0) return months + (months == 1 ? " month ago" : " months ago");
        if (weeks > 0) return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        if (days > 0) return days + (days == 1 ? " day ago" : " days ago");
        if (hours > 0) return hours + (hours == 1 ? " hour ago" : " hours ago");
        if (minutes > 0) return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        return seconds + (seconds == 1 ? " second ago" : " seconds ago");
    }


}
