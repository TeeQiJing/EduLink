package com.dellmau.edulink.adapters;

import android.annotation.SuppressLint;
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
import com.dellmau.edulink.fragments.LearnFragment;
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
        collaborations = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.company_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("nama", String.valueOf(position));
        fetchData(collaborations.get(position), holder);
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFragment(collaborations.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return collaborations.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCollaborations(ArrayList<Collaboration> list) {
        collaborations = list;
        notifyDataSetChanged();
    }

    private void changeFragment(Collaboration collaboration) {
//         Pass the lesson ID as a fresh argument to the fragment
        Bundle bundle = new Bundle();
        bundle.putString("key", collaboration.getCompany().getId()); // Use the lesson ID for the new lesson


        LearnFragment learnFragment = new LearnFragment();
        learnFragment.setArguments(bundle);

        // Replace the current fragment with the new LessonFragment
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,  // Animation for fragment entry
                        R.anim.slide_out_left,  // Animation for fragment exit
                        R.anim.slide_in_left,   // Animation for returning to the fragment
                        R.anim.slide_out_right  // Animation for exiting back
                )
                .replace(R.id.fragment_container, learnFragment)  // Use replace to load a fresh fragment
                .addToBackStack(null)  // Ensure you can go back to the previous fragment
                .commit();
    }

    public void setCompany() {

    }

    private void fetchData(Collaboration collaboration, ViewHolder holder) {
        Log.d("nama", collaboration.getCompany().getId());
        db.collection("company_list")
                .document(collaboration.getCompany().getId())  // Ensure you're using the correct path
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        Log.d("nama", task.toString());
                        if (document.exists()) {
                            String name = document.getString("name");
                            Log.d("nama", name);

                            // Set the fetched values to the UI
                            holder.textView.setText(name);
                            Log.d("nama", "set name");
                            switch (name){
                                case "Dell Technologies":
                                    holder.imageView.setImageResource(R.drawable.ic_delltechnologies);
                                    Log.d("nama", "set image");
                                    break;
                                case "ASUS Tech":
                                    holder.imageView.setImageResource(R.drawable.ic_asus);
                                    Log.d("nama", "set image");
                                    break;
                                case "Acer Technologies":
                                    holder.imageView.setImageResource(R.drawable.ic_acer);
                                    Log.d("nama", "set image");
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
