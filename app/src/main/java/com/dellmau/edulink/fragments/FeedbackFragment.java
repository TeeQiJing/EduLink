package com.dellmau.edulink.fragments;

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
import com.dellmau.edulink.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FeedbackFragment extends Fragment {

    private ImageView avatarImageView;
    private TextView tvUsername;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText etFeedback;
    private MaterialButton btnSubmitFeedback;

    public FeedbackFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadUserProfile();
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

        // Initialize the toolbar
        MaterialToolbar toolbar = rootView.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        Spinner feedbackTypeSpinner = rootView.findViewById(R.id.spinnerFeedbackType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.feedback_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        feedbackTypeSpinner.setAdapter(adapter);

        // Set up the submit button
        btnSubmitFeedback.setOnClickListener(v -> submitFeedback());

        // Return the root view
        return rootView;
    }

    private void loadUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userDocRef = db.collection("users").document(userId);  // Fetch from Firestore

        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
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
            // Simulate feedback submission (you can implement the actual logic)
            Toast.makeText(getContext(), "Feedback sent successfully", Toast.LENGTH_SHORT).show();

            etFeedback.setText("");



        }
    }
}
