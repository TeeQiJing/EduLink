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

import com.dellmau.edulink.R;
import com.dellmau.edulink.fragments.LessonFragment;
import com.dellmau.edulink.models.Collaboration;
import com.dellmau.edulink.models.CurrentLessonCard;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.ViewHolder> {
    ArrayList<Collaboration> collaborations;
    FirebaseFirestore db;
    FragmentManager fragmentManager;

    public CompanyAdapter() {}
    public CompanyAdapter(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.company_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        fetchData(collaborations.get(position), holder);
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });
    }

    @Override
    public int getItemCount() {
        return collaborations.size();
    }

    public void setCollaborations(ArrayList<Collaboration> list) {
        collaborations = list;
        notifyDataSetChanged();
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

    private void fetchData(Collaboration collaboration, ViewHolder holder) {

        db.collection("company_list")
                .document(collaboration.getCompany().getId())  // Ensure you're using the correct path
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Fetch all lesson details
                            String name = document.getString("name");


                            // Set the fetched values to the UI
                            holder.textView.setText(name);

                            switch (name){
                                case "Dell Technologies":
                                    holder.imageView.setImageResource(R.drawable.ic_delltechnologies);
                                    break;
                                default:
                                    holder.imageView.setImageResource(R.drawable.ic_avatar);
                            }
                        }
                    } else {
                        Log.e("CompanyAdapter", "Error company details", task.getException());
                    }
                });
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout relativeLayout;
        ImageView imageView;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.company_material_card_view_RLayout);
            imageView = itemView.findViewById(R.id.company_image);
            textView = itemView.findViewById(R.id.company_name);
        }
    }
}
