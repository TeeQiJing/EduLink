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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dellmau.edulink.R;
import com.dellmau.edulink.fragments.LearnFragment;
import com.dellmau.edulink.models.Collaboration;
import com.dellmau.edulink.models.Company;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SearchCompanyAdapter extends RecyclerView.Adapter<SearchCompanyAdapter.ViewHolder> {
    ArrayList<Company> companies;
    FragmentManager fragmentManager;
    FirebaseFirestore db;
    String result = "";

    public SearchCompanyAdapter(FragmentManager fragmentManager) {
        companies = new ArrayList<>();
        this.fragmentManager = fragmentManager;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.company_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = companies.get(position).getName();
        holder.textView.setText(name);
        switch (name) {
            case "Dell Technologies":
                holder.imageView.setImageResource(R.drawable.ic_delltechnologies);
                break;

            case  "ASUS Tech":
                holder.imageView.setImageResource(R.drawable.ic_asus);
                break;

            case  "Acer Technologies":
                holder.imageView.setImageResource(R.drawable.ic_acer);
                break;

            default:
                holder.imageView.setImageResource(R.drawable.ic_avatar);
        }
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchId(companies.get(position).getName());
            }
        });
    }

    private void searchId(String name) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("company_list").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    Log.d("keyyy", "name "+name);
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        String dataName = doc.getString("name");
                        Log.d("keyyy", "dataName " + dataName);


                        // Add to popular if no "current lessons" or lesson is not in the "current" list
                        if (dataName.equals(name)) {
                            Log.d("keyyy", "get inininin " + result);
                             result = doc.getId();
                            Log.d("keyyy", "resultttttttttttt " + result);
                            changeFragment(result);
                        }
                    }

                } else {
                    Log.e("LearnFragment", "Error getting total_lesson documents: ", task.getException());
                }
            }
        });
    }

    private void changeFragment(String name) {
//         Pass the lesson ID as a fresh argument to the fragment
        Bundle bundle = new Bundle();
        bundle.putString("key", result); // Use the lesson ID for the new lesson



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

    @Override
    public int getItemCount() {
        return companies.size();
    }

    public void setCompanies(ArrayList<Company> companies) {
        this.companies = companies;
        notifyDataSetChanged();
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
