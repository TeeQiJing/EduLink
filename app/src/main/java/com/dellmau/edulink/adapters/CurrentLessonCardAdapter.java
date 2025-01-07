package com.dellmau.edulink.adapters;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.dellmau.edulink.R;
import com.dellmau.edulink.fragments.LessonFragment;
import com.dellmau.edulink.models.CurrentLessonCard;

import java.util.ArrayList;
import java.util.Objects;

public class  CurrentLessonCardAdapter extends RecyclerView.Adapter<CurrentLessonCardAdapter.ViewHolder>{
    ArrayList<CurrentLessonCard> cards = new ArrayList<>();
    FragmentManager fragmentManager;
    FirebaseFirestore db;

    public CurrentLessonCardAdapter(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_lesson_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CurrentLessonCard currentCard = cards.get(position);
        Log.d("CurrentLessonCardAdapter", "Binding card: " + currentCard.getLessonId() + ", Progress: " + currentCard.getProgress());

        // Fetch lesson details based on lessonId (which is a DocumentReference)
        fetchLessonDetails(currentCard, holder);

        holder.RLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToLesson(currentCard);
            }
        });
    }

    private void navigateToLesson(CurrentLessonCard currentLessonCard) {
        // Pass the lesson ID as a fresh argument to the fragment
        Bundle bundle = new Bundle();
        bundle.putString("lessonId", currentLessonCard.getLessonId().getId()); // Use the lesson ID for the new lesson
        Log.d("CurrentLessonCardAdapter", "Navigating to lesson with ID: " + currentLessonCard.getLessonId().getId());

        // Create a new instance of LessonFragment
        LessonFragment lessonFragment = new LessonFragment();
        lessonFragment.setArguments(bundle);

        // Replace the current fragment with the new LessonFragment
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,  // Animation for fragment entry
                        R.anim.slide_out_left,  // Animation for fragment exit
                        R.anim.slide_in_left,   // Animation for returning to the fragment
                        R.anim.slide_out_right  // Animation for exiting back
                )
                .replace(R.id.fragment_container, lessonFragment)  // Use replace to load a fresh fragment
                .addToBackStack(null)  // Ensure you can go back to the previous fragment
                .commit();
    }


    private void fetchLessonDetails(CurrentLessonCard currentCard, @NonNull ViewHolder holder) {
        Log.d("CurrentLessonCardAdapter", "Fetching lesson details for lessonId: " + currentCard.getLessonId().getId());

        db.collection("total_lesson")
                .document(currentCard.getLessonId().getId())  // Ensure you're using the correct path
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Fetch all lesson details
                            String title = document.getString("title");
                            String level = document.getString("level");

                            String rating = document.getString("rating");
                            String pattern = document.getString("pattern");

                            // Add more debug logs for fields
                            Log.d("CurrentLessonCardAdapter", "Fetched lesson details: Title: " + title + ", Level: " + level + ", Rating: " + rating + ", Pattern: " + pattern);

                            // Set the fetched values to the UI
                            holder.lessonLevel.setText(level);
                            holder.lessonTitle.setText(title);

                            switch (Objects.requireNonNull(title)){
                                case "GitHub":
                                    holder.imageView.setImageResource(R.drawable.ic_github);
                                    break;
                                case "Java":
                                    holder.imageView.setImageResource(R.drawable.ic_java);
                                    break;
                                case "HTML & CSS":
                                    holder.imageView.setImageResource(R.drawable.ic_html);
                                    break;

                                case "Python":
                                    holder.imageView.setImageResource(R.drawable.ic_python);
                                    break;
                                case "UI UX Design":
                                    holder.imageView.setImageResource(R.drawable.ic_uiux);
                                    break;
                                default:
                                    holder.imageView.setImageResource(R.drawable.ic_avatar);
                            }

                            // Set progress based on the current card progress value
                            if (currentCard.getProgress() != null) {
                                int progressValue = Integer.parseInt(currentCard.getProgress());
                                holder.progressBar.setProgress(progressValue);
                                holder.progress.setText(progressValue + "%");
                            } else {
                                holder.progressBar.setProgress(0);  // Default value if progress is null
                                holder.progress.setText("0%");
                            }
                        } else {
                            Log.e("CurrentLessonCardAdapter", "No document found for lessonId: " + currentCard.getLessonId().getId());
                        }
                    } else {
                        Log.e("CurrentLessonCardAdapter", "Error fetching lesson details", task.getException());
                    }
                });
    }

    private int getImageResource(String imageName) {
        switch (imageName) {
            case "GitHubImage":
                return R.drawable.ic_apple;
            case "PythonImage":
                return R.drawable.ic_certificate;
            case "JavaImage":
                return R.drawable.ic_google;
            case "HTMLCSSImage":
                return R.drawable.ic_facebook;
            case "UIUXImage":
                return R.drawable.ic_chat;
            default:
                return R.drawable.ic_launcher_background;  // Default image if no match found
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public void setCard(ArrayList<CurrentLessonCard> cards) {
        Log.d("CurrentLessonCardAdapter", "Setting " + cards.size() + " cards in adapter.");
        this.cards = cards;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout RLayout;
        ImageView imageView;
        TextView lessonLevel;
        TextView lessonTitle;
        TextView lessonRating; // Add a TextView for rating
        TextView lessonPattern; // Add a TextView for pattern
        ProgressBar progressBar;
        TextView progress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            RLayout = itemView.findViewById(R.id.material_card_view_RLayout);
            imageView = itemView.findViewById(R.id.lesson_image);
            lessonLevel = itemView.findViewById(R.id.lesson_level);
            lessonTitle = itemView.findViewById(R.id.lesson_title);
            progressBar = itemView.findViewById(R.id.progress_bar);
            progress = itemView.findViewById(R.id.progress);
        }
    }
}

