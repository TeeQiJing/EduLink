package com.dellmau.edulink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dellmau.edulink.R;


import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private List<String> badgeNames;

    public BadgeAdapter(List<String> badgeNames) {
        this.badgeNames = badgeNames;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        // Get the badge name for the current position
        String badgeName = badgeNames.get(position);

        // Set the badge details based on the badge name
        switch (badgeName) {
            case "\"First Step\"":
                holder.badgesImage.setImageResource(R.drawable.ic_badges1);
                holder.badgesDialogTitle.setText("\"First Step\"");
                break;

            case "\"Milestone Pro\"":
                holder.badgesImage.setImageResource(R.drawable.ic_badges2);
                holder.badgesDialogTitle.setText("\"Milestone Pro\"");
                break;

            case "\"Quiz Whiz\"":
                holder.badgesImage.setImageResource(R.drawable.ic_badges3);
                holder.badgesDialogTitle.setText("\"Quiz Whiz\"");
                break;

            default:
                // Fallback for unrecognized badges
                holder.badgesImage.setImageResource(R.drawable.ic_default_badge);
                holder.badgesDialogTitle.setText("\"Unrecognized Badge\"");
                break;
        }
    }


    @Override
    public int getItemCount() {
        return badgeNames.size();
    }

    public static class BadgeViewHolder extends RecyclerView.ViewHolder {

        ImageView badgesImage;
        TextView badgesDialogTitle;

        public BadgeViewHolder(View itemView) {
            super(itemView);
            badgesImage = itemView.findViewById(R.id.badges_image);
            badgesDialogTitle = itemView.findViewById(R.id.badges_dialog_title);
        }
    }
}

