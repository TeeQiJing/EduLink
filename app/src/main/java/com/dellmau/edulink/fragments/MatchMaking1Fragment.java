package com.dellmau.edulink.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dellmau.edulink.R;
import com.dellmau.edulink.adapters.ProjectAdapter;
import com.dellmau.edulink.models.Project;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchMaking1Fragment extends Fragment {

    private RecyclerView recyclerView;
    private ProjectAdapter projectAdapter;
    private List<Project> projectList;
    private FirebaseFirestore db;

    // Default User ID (bypassing login)
    private static final String DEFAULT_USER_ID = "4GNNtv5TXxXgs5z9TOwzJhifYX53";

    public MatchMaking1Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_match_making1, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.projectsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the project list and adapter
        projectList = new ArrayList<>();
        projectAdapter = new ProjectAdapter(getContext(), projectList);
        recyclerView.setAdapter(projectAdapter);

        // Fetch projects from Firestore
        getProjects();

        return rootView;
    }

    private void getProjects() {
        Log.d("MatchMaking1Fragment", "Fetching projects from Firestore...");

        CollectionReference projectsRef = db.collection("project");

        projectsRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            Log.d("MatchMaking1Fragment", "Projects fetched: " + querySnapshot.size() + " documents.");
                            for (DocumentSnapshot documentSnapshot : querySnapshot) {
                                Project project = documentSnapshot.toObject(Project.class);

                                if (project != null) {
                                    // Calculate similarity score for the project
                                    calculateSimilarityScore(project);
                                } else {
                                    Log.d("MatchMaking1Fragment", "Project is null!");
                                }
                            }
                        } else {
                            Log.d("MatchMaking1Fragment", "QuerySnapshot is null.");
                        }
                    } else {
                        Log.d("MatchMaking1Fragment", "Error getting documents: ", task.getException());
                    }
                });
    }


    // Callback interface for returning the user skills
    public interface UserSkillsCallback {
        void onSkillsFetched(Map<String, Integer> userSkills);
    }

    // Fetch the user's skills from Firestore based on user role and ID
    private void fetchUserSkills(String userId, String userRole, UserSkillsCallback callback) {
        Map<String, Integer> userSkills = new HashMap<>();

        // Fetch user data from Firestore based on role
        DocumentReference userDocRef = db.collection(userRole.toLowerCase()).document(userId);
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Assuming user skills are stored as a Map in the Firestore document
                Map<String, Object> userSkillsData = (Map<String, Object>) documentSnapshot.get("skill_point");

                if (userSkillsData != null) {
                    // Convert the fetched data into a Map of String (skill name) -> Integer (skill score)
                    for (Map.Entry<String, Object> entry : userSkillsData.entrySet()) {
                        userSkills.put(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
                    }
                }
            }

            // Return the user skills through the callback
            callback.onSkillsFetched(userSkills);
        });
    }

    // Method to predict hidden skills score based on a suggested skill score
    private Map<String, Integer> predictHiddenSkills(String skillName, int suggestedSkillScore) {
        Map<String, Integer> hiddenSkills = new HashMap<>();

        // Student skills combinations
        if (skillName.equalsIgnoreCase("UI/UX Design")) {
            hiddenSkills.put("Problem Solving", suggestedSkillScore + 10);
            hiddenSkills.put("Creativity", suggestedSkillScore + 15);
            hiddenSkills.put("Backend", suggestedSkillScore + 8);
        } else if (skillName.equalsIgnoreCase("Backend")) {
            hiddenSkills.put("Problem Solving", suggestedSkillScore + 12);
            hiddenSkills.put("Code Simplicity", suggestedSkillScore + 10);
            hiddenSkills.put("UI/UX Design", suggestedSkillScore + 7);
        } else if (skillName.equalsIgnoreCase("Creativity")) {
            hiddenSkills.put("Problem Solving", suggestedSkillScore + 5);
            hiddenSkills.put("UI/UX Design", suggestedSkillScore + 15);
            hiddenSkills.put("Backend", suggestedSkillScore + 6);
        } else if (skillName.equalsIgnoreCase("Code Simplicity")) {
            hiddenSkills.put("Backend", suggestedSkillScore + 5);
            hiddenSkills.put("Problem Solving", suggestedSkillScore + 7);
        } else if (skillName.equalsIgnoreCase("Problem Solving")) {
            hiddenSkills.put("Backend", suggestedSkillScore + 8);
            hiddenSkills.put("UI/UX Design", suggestedSkillScore + 10);
            hiddenSkills.put("Creativity", suggestedSkillScore + 6);
        }

        // Educator skills combinations
        else if (skillName.equalsIgnoreCase("Risk Management")) {
            hiddenSkills.put("Project Management", suggestedSkillScore + 12);
            hiddenSkills.put("Team Leadership", suggestedSkillScore + 10);
            hiddenSkills.put("Adaptability", suggestedSkillScore + 7);
        } else if (skillName.equalsIgnoreCase("Adaptability")) {
            hiddenSkills.put("Risk Management", suggestedSkillScore + 5);
            hiddenSkills.put("Industry Knowledge", suggestedSkillScore + 10);
            hiddenSkills.put("Project Management", suggestedSkillScore + 8);
        } else if (skillName.equalsIgnoreCase("Team Leadership")) {
            hiddenSkills.put("Project Management", suggestedSkillScore + 12);
            hiddenSkills.put("Risk Management", suggestedSkillScore + 10);
            hiddenSkills.put("Adaptability", suggestedSkillScore + 8);
        } else if (skillName.equalsIgnoreCase("Industry Knowledge")) {
            hiddenSkills.put("Project Management", suggestedSkillScore + 15);
            hiddenSkills.put("Team Leadership", suggestedSkillScore + 7);
            hiddenSkills.put("Risk Management", suggestedSkillScore + 5);
        } else if (skillName.equalsIgnoreCase("Project Management")) {
            hiddenSkills.put("Risk Management", suggestedSkillScore + 10);
            hiddenSkills.put("Team Leadership", suggestedSkillScore + 15);
            hiddenSkills.put("Adaptability", suggestedSkillScore + 5);
        }

        return hiddenSkills;
    }


    // Method to calculate the match between user skill and suggested skill
    private int calculateSkillMatch(int userSkillScore, int suggestedSkillScore) {
        return Math.min(userSkillScore, suggestedSkillScore); // Use the lower score for the match
    }

    // Method to calculate the similarity score for a project
    // Method to calculate the similarity score for a project
    private void calculateSimilarityScore(Project project) {
        String userId = DEFAULT_USER_ID;

        // Get user role from SharedPreferences (or set it manually for testing purposes)
        String userRole = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE).getString("user_role", "student");

        // Fetch user's skills based on their role
        fetchUserSkills(userId, userRole, userSkills -> {
            Log.d("MatchMaking1Fragment", "Fetched User Skills: " + userSkills);

            // Get suggested skills from the project
            Map<String, Integer> suggestedSkills = project.getSuggested_skills();

            // Log the suggested skills
            Log.d("MatchMaking1Fragment", "Suggested Skills: " + suggestedSkills);

            int totalSimilarityScore = 0;
            int totalSuggestedSkillScore = 0;

            // Iterate through each suggested skill and calculate similarity
            for (Map.Entry<String, Integer> entry : suggestedSkills.entrySet()) {
                String suggestedSkill = entry.getKey();
                int suggestedSkillScore = entry.getValue();

                // Log the skill being checked
                Log.d("MatchMaking1Fragment", "Checking skill: " + suggestedSkill + " | Suggested Score: " + suggestedSkillScore);

                // Check if the user has the suggested skill
                Integer userSkillScore = userSkills.get(suggestedSkill);

                if (userSkillScore != null) {
                    // Calculate similarity for this skill
                    totalSimilarityScore += calculateSkillMatch(userSkillScore, suggestedSkillScore);
                    totalSuggestedSkillScore += suggestedSkillScore;

                    // Now, predict hidden skills and compare them as well
                    Map<String, Integer> hiddenSkills = predictHiddenSkills(suggestedSkill, suggestedSkillScore);

                    // Log the hidden skills
                    Log.d("MatchMaking1Fragment", "Hidden Skills for " + suggestedSkill + ": " + hiddenSkills);

                    for (Map.Entry<String, Integer> hiddenEntry : hiddenSkills.entrySet()) {
                        String hiddenSkill = hiddenEntry.getKey();
                        int predictedHiddenSkillScore = hiddenEntry.getValue();

                        // If the user has the hidden skill, calculate the similarity
                        Integer userHiddenSkillScore = userSkills.get(hiddenSkill);
                        if (userHiddenSkillScore != null) {
                            totalSimilarityScore += calculateSkillMatch(userHiddenSkillScore, predictedHiddenSkillScore);
                            totalSuggestedSkillScore += predictedHiddenSkillScore;
                        }
                    }
                }
            }

            // Return the similarity as a percentage (0 to 100)
            int similarityPercentage = 0;
            if (totalSuggestedSkillScore > 0) {
                similarityPercentage = Math.min(100, (totalSimilarityScore * 100) / totalSuggestedSkillScore);
            }

            // Set the similarity score to the project object directly
            project.setSimilarityScore(similarityPercentage);

            // Log the similarity score
            Log.d("MatchMaking1Fragment", "Similarity Score: " + similarityPercentage);

            // After updating the similarity score, add the project to the list
            projectList.add(project);

            // Sort projects by similarity score in descending order and take top 3
            projectList.sort((project1, project2) -> Integer.compare(project2.getSimilarityScore(), project1.getSimilarityScore()));
            projectList = projectList.subList(0, Math.min(3, projectList.size()));

            // Notify the adapter that data has changed
            projectAdapter.notifyDataSetChanged();
        });
    }

}
