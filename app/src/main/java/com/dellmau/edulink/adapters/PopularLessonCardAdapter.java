package com.dellmau.edulink.adapters;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.dellmau.edulink.R;
import com.dellmau.edulink.fragments.LessonFragment;

import java.util.ArrayList;
import java.util.Objects;

public class PopularLessonCardAdapter extends RecyclerView.Adapter<PopularLessonCardAdapter.ViewHolder> {

    ArrayList<String> cards = new ArrayList<>();
    FragmentManager fragmentManager;

    public PopularLessonCardAdapter(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.popular_lesson_card, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String card = cards.get(position);

        fetchData(card, holder);

        holder.RelLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToLesson(card);
            }
        });
    }

    private void navigateToLesson(String card) {
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

    private void fetchData(String card, @NonNull ViewHolder holder) {
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

    public void setCards(ArrayList<String> cards) {
        this.cards = cards;
        notifyDataSetChanged();
        Log.d("PopularLessonCardAdapter", cards.toString());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView level;
        TextView title;
        TextView ratings;
        RelativeLayout RelLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.popular_lesson_image);
            level = itemView.findViewById(R.id.popular_lesson_level);
            title = itemView.findViewById(R.id.popular_lesson_title);
            ratings = itemView.findViewById(R.id.ratings);
            RelLayout = itemView.findViewById(R.id.popular_card_view_RelLayout);
        }
    }

}
