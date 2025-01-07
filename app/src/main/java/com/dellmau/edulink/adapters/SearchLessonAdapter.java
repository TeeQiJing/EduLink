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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.dellmau.edulink.R;
import com.dellmau.edulink.fragments.LessonFragment;
import com.dellmau.edulink.models.CurrentLessonCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchLessonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Object> lessons;
    private static final int CURRENT_LESSON = 0;
    private static final int POPULAR_LESSON = 1;
    FirebaseFirestore db;
    private FragmentManager fragmentManager;

    public SearchLessonAdapter(FragmentManager fragmentManager) {
        this.db = FirebaseFirestore.getInstance();
        this.fragmentManager = fragmentManager;
        lessons = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == CURRENT_LESSON) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_lesson_card, parent, false);
            return new CurrentLessonCardViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.popular_lesson_card, parent, false);
            return new PopularLessonCardViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (lessons.get(position) instanceof CurrentLessonCard) {
            Log.d("sadapter", "current");
            // Bind popular lesson data
            CurrentLessonCard currentLessonCard = (CurrentLessonCard) lessons.get(position);
            fetchCurrentLessonDetails(currentLessonCard, (CurrentLessonCardViewHolder) holder);
            ((CurrentLessonCardViewHolder) holder).RLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navigateToCurrentLesson((CurrentLessonCard) lessons.get(position));
                }
            });
        } else {
            Log.d("sadapter", "popular");
            // Bind regular lesson data
            fetchData(lessons.get(position).toString(), (PopularLessonCardViewHolder) holder);
            ((PopularLessonCardViewHolder) holder).RelLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navigateToPopularLesson((String) lessons.get(position));
                }
            });
        }
    }

    private void navigateToCurrentLesson(CurrentLessonCard currentLessonCard) {
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

    private void navigateToPopularLesson(String card) {
        Bundle bundle = new Bundle();
        bundle.putString("lessonId", card);

        LessonFragment lessonFragment = new LessonFragment();
        lessonFragment.setArguments(bundle);
        Log.d("PopularLessonCardAdapter", bundle.toString());

        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,  // Animation for fragment entry
                        R.anim.slide_out_left, // Animation for fragment exit
                        R.anim.slide_in_left,  // Animation for returning to the fragment
                        R.anim.slide_out_right // Animation for exiting back
                )
                .replace(R.id.fragment_container, lessonFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public int getItemViewType(int position) {
        if (lessons.get(position) instanceof CurrentLessonCard) {
            return CURRENT_LESSON;
        } else {
            return POPULAR_LESSON;
        }
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    public void setFilteredLesson(ArrayList<Object> filteredLesson) {
        lessons = filteredLesson;
        notifyDataSetChanged();
    }

    private void fetchCurrentLessonDetails(CurrentLessonCard currentCard, @NonNull CurrentLessonCardViewHolder holder) {
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

    private void fetchData(String card, @NonNull PopularLessonCardViewHolder holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("total_lesson").whereEqualTo(FieldPath.documentId(), card).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("PopularLessonCardAdapter", task.toString());
                    for (QueryDocumentSnapshot doc: task.getResult()) {
                        Log.d("PopularLessonCardAdapter", doc.toString());
                        String image = doc.getString("image");
                        String level = doc.getString("level");
                        String rating = doc.getString("rating");
                        String title = doc.getString("title");

//                        holder.image.setImageResource(getImageResource(image));


                        switch (Objects.requireNonNull(title)){
                            case "GitHub":
                                holder.image.setImageResource(R.drawable.ic_github);
                                break;
                            case "Java":
                                holder.image.setImageResource(R.drawable.ic_java);
                                break;
                            case "HTML & CSS":
                                holder.image.setImageResource(R.drawable.ic_html);
                                break;

                            case "Python":
                                holder.image.setImageResource(R.drawable.ic_python);
                                break;
                            case "UI UX Design":
                                holder.image.setImageResource(R.drawable.ic_uiux);
                                break;
                            default:
                                holder.image.setImageResource(R.drawable.ic_avatar);
                        }
                        holder.level.setText(level);
                        holder.ratings.setText(rating);
                        holder.title.setText(title);
                    }
                }
                else {
                    Log.e("PopularLessonCardAdapter", "Error fetching data", task.getException());
                }
            }
        });
    }

    class CurrentLessonCardViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout RLayout;
        ImageView imageView;
        TextView lessonLevel;
        TextView lessonTitle;
        TextView lessonRating; // Add a TextView for rating
        TextView lessonPattern; // Add a TextView for pattern
        ProgressBar progressBar;
        TextView progress;

        public CurrentLessonCardViewHolder(@NonNull View itemView) {
            super(itemView);
            RLayout = itemView.findViewById(R.id.material_card_view_RLayout);
            imageView = itemView.findViewById(R.id.lesson_image);
            lessonLevel = itemView.findViewById(R.id.lesson_level);
            lessonTitle = itemView.findViewById(R.id.lesson_title);
            progressBar = itemView.findViewById(R.id.progress_bar);
            progress = itemView.findViewById(R.id.progress);
        }
    }

    class PopularLessonCardViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView level;
        TextView title;
        TextView ratings;
        RelativeLayout RelLayout;
        public PopularLessonCardViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.popular_lesson_image);
            level = itemView.findViewById(R.id.popular_lesson_level);
            title = itemView.findViewById(R.id.popular_lesson_title);
            ratings = itemView.findViewById(R.id.ratings);
            RelLayout = itemView.findViewById(R.id.popular_card_view_RelLayout);
        }
    }

}
