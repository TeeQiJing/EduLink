package com.dellmau.edulink.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.dellmau.edulink.R;
import com.dellmau.edulink.models.CommunityComment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
//    private final Context context;
    private final List<CommunityComment> commentList;

    // Constructor
//    public CommentAdapter(Context context, List<CommunityComment> commentList) {
//        this.context = context;
//        this.commentList = commentList;
//    }
    public CommentAdapter(List<CommunityComment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommunityComment comment = commentList.get(position);

        comment.fetchUserDetails(FirebaseFirestore.getInstance(), new CommunityComment.UserDetailsCallback() {
            @Override
            public void onSuccess(String username, String avatarUrl) {
                holder.usernameComment.setText(username);
                displayAvatar(avatarUrl, holder.avatarComment);
            }

            @Override
            public void onFailure(Exception e) {
                holder.usernameComment.setText("Unknown User");
                holder.avatarComment.setImageResource(R.drawable.gradient_background);
            }
        });

        holder.contentComment.setText(comment.getCommentContent());
        holder.timestampComment.setText(comment.getTimeAgo());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarComment;
        TextView usernameComment, contentComment, timestampComment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarComment = itemView.findViewById(R.id.comment_user_avatar);
            usernameComment = itemView.findViewById(R.id.comment_user_name);
            contentComment = itemView.findViewById(R.id.comment_content);
            timestampComment = itemView.findViewById(R.id.comment_time);
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
}