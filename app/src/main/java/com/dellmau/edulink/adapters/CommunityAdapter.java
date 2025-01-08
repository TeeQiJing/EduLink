package com.dellmau.edulink.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.dellmau.edulink.R;
import com.dellmau.edulink.fragments.CommentFragment;
import com.dellmau.edulink.models.CommunityPost;

import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.PostViewHolder> {

    private List<CommunityPost> postList;

    private FirebaseFirestore db;
    private final Context context;

    public CommunityAdapter(List<CommunityPost> postList, FirebaseFirestore db, Context context) {
        this.postList = postList;
        this.db = db;
        this.context = context;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        CommunityPost post = postList.get(position);

        // Fetch and display user details
        post.fetchUserDetails(db, new CommunityPost.UserDetailsCallback() {
            @Override
            public void onSuccess(String username, String avatarUrl) {
                holder.username.setText(username);
                displayAvatar(avatarUrl, holder.avatar);
            }

            @Override
            public void onFailure(Exception e) {
                holder.username.setText("Unknown Student");
                holder.avatar.setImageResource(R.drawable.gradient_background);
            }
        });

        holder.postTime.setText("· " + post.getTimeAgo());
        holder.postTitle.setText(post.getTitle());
        holder.postContent.setText(post.getContent());
        holder.postLikes.setText(String.valueOf(post.getLikedBy().size()));
        holder.lecturerSkillTV.setText(post.getLecturerSkills());
        holder.studentSkillTV.setText(post.getStudentSkills());

        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Update like button UI
        updateLikeButtonUI(holder, post, currentUserID);

        holder.postLikes.setOnClickListener(v -> {
            post.toggleLike(currentUserID, db, new CommunityPost.SaveCallback() {
                @Override
                public void onSuccess(String postId) {
                    // Notify adapter of the change
                    notifyItemChanged(holder.getAdapterPosition(), "likeChanged");
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(context, "Failed to update like: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Fetch and display comment count
        post.getCommentCount(db, new CommunityPost.FetchCommentCountCallback() {
            @Override
            public void onSuccess(int commentCount) {
                holder.postComments.setText(String.valueOf(commentCount));
            }

            @Override
            public void onFailure(Exception e) {
                holder.postComments.setText("0"); // Default to 0 if fetching fails
                Toast.makeText(context, "Error loading comments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.postComments.setOnClickListener(v -> {
            Fragment commentFragment = CommentFragment.newInstance(post);
            FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, commentFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            if (payloads.contains("likeChanged")) {
                CommunityPost post = postList.get(position);
                String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                updateLikeButtonUI(holder, post, currentUserID);
            }
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }


    private void updateLikeButtonUI(PostViewHolder holder, CommunityPost post, String currentUserID) {
        boolean isLiked = post.getLikedBy() != null && post.getLikedBy().contains(currentUserID);

        if (isLiked) {
            holder.postLikes.setBackgroundColor(holder.postLikes.getContext().getResources().getColor(R.color.liked));
            holder.postLikes.setTextColor(ContextCompat.getColor(context, android.R.color.white)); // White text
            holder.likeOverlayIcon.setVisibility(View.VISIBLE);
        } else {
            holder.postLikes.setBackgroundColor(holder.postLikes.getContext().getResources().getColor(R.color.unliked));
            holder.postLikes.setTextColor(ContextCompat.getColor(context, R.color.blackIconTint)); // Black text
            holder.likeOverlayIcon.setVisibility(View.GONE);

        }

        holder.postLikes.setText(String.valueOf(post.getLikedBy().size())); // Update like count
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView username, postTime, postTitle, postContent,lecturerSkillTV,studentSkillTV;
        public Button postLikes;
        Button postComments;
        ImageView avatar,likeOverlayIcon;


        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.post_user_name);
            postTime = itemView.findViewById(R.id.post_time);
            postTitle = itemView.findViewById(R.id.post_title);
            postContent = itemView.findViewById(R.id.post_content);
            postLikes = itemView.findViewById(R.id.post_likes);
            postComments = itemView.findViewById(R.id.post_comments);
            avatar = itemView.findViewById(R.id.post_user_avatar);
            likeOverlayIcon = itemView.findViewById(R.id.ic_liked);
            lecturerSkillTV = itemView.findViewById(R.id.lecturerSkillTV);
            studentSkillTV = itemView.findViewById(R.id.studentSkillTV);
        }
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

    public void updateList(List<CommunityPost> filteredPosts) {
        this.postList = filteredPosts;
        notifyDataSetChanged();
    }
}
