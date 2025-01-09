package com.dellmau.edulink.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dellmau.edulink.R;
import com.dellmau.edulink.models.Employer;
import com.dellmau.edulink.models.Student;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FeedbackFragment extends Fragment {

    private ImageView avatarImageView;
    private TextView tvUsername;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText etFeedback;
    private MaterialButton btnSubmitFeedback;


    String userId;

    String user_role;
    private SharedPreferences sharedPreferences;

    public FeedbackFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        user_role = sharedPreferences.getString("user_role", "");

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_feedback, container, false);

        avatarImageView = rootView.findViewById(R.id.profileImage);
        tvUsername = rootView.findViewById(R.id.tvUsername);
        etFeedback = rootView.findViewById(R.id.etFeedback);
        btnSubmitFeedback = rootView.findViewById(R.id.btnSubmitFeedback);

        Spinner feedbackTypeSpinner = rootView.findViewById(R.id.spinnerFeedbackType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.feedback_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        feedbackTypeSpinner.setAdapter(adapter);


        Toast.makeText(getActivity(), "User Role: " + user_role, Toast.LENGTH_SHORT).show();

        userId = mAuth.getCurrentUser().getUid();
        // Initialize the toolbar
        MaterialToolbar toolbar = rootView.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());


        // Set up the submit button
        btnSubmitFeedback.setOnClickListener(v -> submitFeedback());
        loadUserProfile();
        // Return the root view
        return rootView;
    }

    private void loadUserProfile() {
        // Check if user_role is not null or empty before using it
        if (user_role == null || user_role.isEmpty()) {
            Toast.makeText(getContext(), "User role is not set", Toast.LENGTH_SHORT).show();
            return; // Early exit if user_role is not valid
        }

        // Proceed with loading user profile based on the role
        DocumentReference userDocRef = db.collection(user_role.toLowerCase()).document(userId);  // Fetch from Firestore

        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {

                    if ("Student".equals(user_role)) {
                        Student user = documentSnapshot.toObject(Student.class);
                        // Display user details
                        tvUsername.setText(user.getUsername());

                        // Load avatar if it exists
                        if (user.getAvatar() != null) {
                            String base64Image = user.getAvatar();
                            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            avatarImageView.setImageBitmap(decodedByte);
                        }
                    } else if ("Employer".equals(user_role)) {
                        Employer user = documentSnapshot.toObject(Employer.class);
                        // Display user details
                        tvUsername.setText(user.getUsername());

                        // Load avatar if it exists
                        if (user.getAvatar() != null) {
                            String base64Image = user.getAvatar();
                            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            avatarImageView.setImageBitmap(decodedByte);
                        }
                    }
                }
            } else {
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void submitFeedback() {

        String feedbackText = etFeedback.getText().toString().trim();

        if (feedbackText.isEmpty()) {
            // If feedback is empty, show a Toast message
            Toast.makeText(getContext(), "Please provide feedback", Toast.LENGTH_SHORT).show();
        } else {
            // Create a new feedback document in the Firestore user_review collection

            Map<String, Object> feedbackData = new HashMap<>();
            feedbackData.put("feedback", feedbackText);
            feedbackData.put("role", user_role.toLowerCase());
            feedbackData.put("userIdRef", db.collection(user_role.toLowerCase()).document(userId));


            // Add the feedback data to Firestore
            db.collection("user_review")
                    .add(feedbackData)
                    .addOnSuccessListener(documentReference -> {
                        // Success callback: Feedback submitted successfully
                        Toast.makeText(getContext(), "Feedback sent successfully", Toast.LENGTH_SHORT).show();
                        etFeedback.setText(""); // Clear the feedback text field
                    })
                    .addOnFailureListener(e -> {
                        // Failure callback: Show error message
                        Toast.makeText(getContext(), "Failed to send feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
