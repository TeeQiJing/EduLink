package com.dellmau.edulink.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dellmau.edulink.R;
import com.dellmau.edulink.adapters.CertAdapter;
import com.dellmau.edulink.models.CertCard;
import com.dellmau.edulink.models.Educator;
import com.dellmau.edulink.models.Employer;
import com.dellmau.edulink.models.Student;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

public class CertFragment extends Fragment {
    public interface DataReadyCallback {
        void onDataReady();
    }
    ArrayList<CertCard> certCardArrayList=new ArrayList<>();
    FirebaseAuth fAuth=FirebaseAuth.getInstance();
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    ArrayList<String> date=new ArrayList<>();
    ArrayList<String> course_name=new ArrayList<>();
    String name;
    String user_role;
    SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        user_role = sharedPreferences.getString("user_role", "");
    }
    private void setUpTestingmodel(DataReadyCallback callback){
        // Get the current user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userReference = FirebaseFirestore.getInstance().collection(user_role.toLowerCase()).document(userId);

        db.collection("current_lesson").whereEqualTo("userId", userReference).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    date.clear();
                    course_name.clear();
                    ArrayList<DocumentReference> lessonRefs = new ArrayList<>();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String progress = document.getString("progress");
                        if ("100".equals(progress)) {
                            DocumentReference lessonRef = document.getDocumentReference("lessonId");
                            date.add(document.getString("date"));
                            if (lessonRef != null) {
                                lessonRefs.add(lessonRef);
                            }
                        }
                    }

                    // If no references, call callback immediately
                    if (lessonRefs.isEmpty()) {
                        callback.onDataReady();
                        return;
                    }

                    // Process all lesson references
                    for (int i = 0; i < lessonRefs.size(); i++) {
                        int index = i; // Capture index for use in async call
                        lessonRefs.get(i).get().addOnCompleteListener(lessonTask -> {
                            if (lessonTask.isSuccessful()) {
                                DocumentSnapshot lessonDoc = lessonTask.getResult();
                                if (lessonDoc != null && lessonDoc.exists()) {
                                    String lessonName = lessonDoc.getString("title");
                                    if (lessonName != null) {
                                        course_name.add(lessonName);
                                    }
                                }
                            }

                            // Check if all references are processed
                            if (index == lessonRefs.size() - 1) {
                                callback.onDataReady();
                            }
                        });
                    }
                } else {
                    callback.onDataReady();
                }
            } else {
                Log.e("FireStore", "Error getting documents: ", task.getException());
                callback.onDataReady();
            }
        });
    }

    private void setup(final View view){
        for(int i=0;i<course_name.size();i++){
            certCardArrayList.add(new CertCard(date.get(i), name,course_name.get(i)));
        }
        updateUI(view);
    }

    private void loadUserName(DataReadyCallback callback){
        String userId = fAuth.getCurrentUser().getUid();
        DocumentReference userDocRef = db.collection(user_role.toLowerCase()).document(userId);
        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    if(user_role.equals("Student")){
                        Student user = documentSnapshot.toObject(Student.class);
                        if (user != null) {
                            name=user.getUsername();
                        }else {
                            name=null;
                        }
                        callback.onDataReady();
                    }else if(user_role.equals("Educator")){
                        Educator user = documentSnapshot.toObject(Educator.class);
                        if (user != null) {
                            name=user.getUsername();
                        }else {
                            name=null;
                        }
                        callback.onDataReady();
                    }else if(user_role.equals("Employer")){
                        Employer user = documentSnapshot.toObject(Employer.class);
                        if (user != null) {
                            name=user.getUsername();
                        }else {
                            name=null;
                        }
                        callback.onDataReady();
                    }


                }else {
                    Log.e("LearnFragment", "Document does not exist or is null");
                    Toast.makeText(getContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Log.e("LearnFragment", "Error fetching user profile", task.getException());
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_cert,container,false);
        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        loadUserName(() -> setUpTestingmodel(() -> setup(view)));
        return view;
    }
    public void updateUI(View view){
        RecyclerView recyclerView=view.findViewById(R.id.cert_recycler);
        CertAdapter adapter=new CertAdapter(requireContext(),certCardArrayList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }
}