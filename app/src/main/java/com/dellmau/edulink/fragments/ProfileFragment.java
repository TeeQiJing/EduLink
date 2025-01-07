package com.dellmau.edulink.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dellmau.edulink.R;
import com.dellmau.edulink.activities.LoginActivity;
import com.dellmau.edulink.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView avatarImageView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvUsername, tvPoints, tvCourses, tvBadge;
    private LinearLayout btnCertificate, btnFeedback, btnSettings, btnLogout, btnBadge;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        avatarImageView = rootView.findViewById(R.id.profileImage);
        tvUsername = rootView.findViewById(R.id.tvUsername);
        tvPoints = rootView.findViewById(R.id.tvPoints);
        tvCourses = rootView.findViewById(R.id.tvCourses);
        btnCertificate = rootView.findViewById(R.id.btnCertificate);
        btnFeedback = rootView.findViewById(R.id.btnFeedback);
        btnSettings = rootView.findViewById(R.id.btnSettings);
        btnLogout = rootView.findViewById(R.id.btnLogout);
        tvBadge = rootView.findViewById(R.id.tvBadge);
        btnBadge = rootView.findViewById(R.id.btnBadges);



        // Load user profile
        loadUserProfile();

        // Set click listeners
        avatarImageView.setOnClickListener(v -> openImagePicker());
        btnCertificate.setOnClickListener(v -> navigateToCertificate());
        btnFeedback.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_right,  // Animation for fragment entry
                            R.anim.slide_out_left, // Animation for fragment exit
                            R.anim.slide_in_left,  // Animation for returning to the fragment
                            R.anim.slide_out_right // Animation for exiting back
                    )
                    .replace(R.id.fragment_container, new FeedbackFragment()) // Replace `fragment_container` with your container ID
                    .addToBackStack(null)
                    .commit();
        });
        btnBadge.setOnClickListener(v -> checkAndNavigate());
        btnSettings.setOnClickListener(v -> {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in_right,  // Animation for fragment entry
                                    R.anim.slide_out_left, // Animation for fragment exit
                                    R.anim.slide_in_left,  // Animation for returning to the fragment
                                    R.anim.slide_out_right // Animation for exiting back
                            )
                            .replace(R.id.fragment_container, new SettingsFragment()) // Replace `fragment_container` with your container ID
                            .addToBackStack(null)
                            .commit();
                }
        );
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        return rootView;
    }

    private void checkAndNavigate() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("user_badges")
                .whereEqualTo("userIdRef", db.collection("users").document(userId))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean hasBadges = false;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DocumentReference badgeIdRef = document.getDocumentReference("badgeIdRef");
                            if (badgeIdRef != null) {
                                hasBadges = true;
                                break; // If at least one badge is found, stop further checks
                            }
                        }

                        if (hasBadges) {
                            // Navigate to BadgeFragment
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .setCustomAnimations(
                                            R.anim.slide_in_right,
                                            R.anim.slide_out_left,
                                            R.anim.slide_in_left,
                                            R.anim.slide_out_right
                                    )
                                    .replace(R.id.fragment_container, new BadgeFragment())
                                    .addToBackStack(null)
                                    .commit();
                        } else {
                            // Navigate to DefaultBadgeFragment
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .setCustomAnimations(
                                            R.anim.slide_in_right,
                                            R.anim.slide_out_left,
                                            R.anim.slide_in_left,
                                            R.anim.slide_out_right
                                    )
                                    .replace(R.id.fragment_container, new DefaultBadgeFragment())
                                    .addToBackStack(null)
                                    .commit();
                        }
                    } else {
                        Log.w("BadgeCheck", "Error checking badges.", task.getException());
                    }
                });
    }

    private void loadUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userDocRef = db.collection("users").document(userId);

        // Fetch user details
        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        tvUsername.setText(user.getUsername());
                        tvPoints.setText(user.getXp() + "\nPoints");
                        if (user.getXp() >= 100) tvBadge.setText(String.valueOf(user.getXp() / 100));
                        else tvBadge.setText("1");


                        // Load avatar if available
                        if (user.getAvatar().isEmpty()) {
                            avatarImageView.setImageResource(R.drawable.ic_avatar); // Default avatar
                        } else {
                            loadAvatar(user.getAvatar());
                        }

                        // Fetch number of courses
                        fetchCoursesCount(userId);
                    }
                } else {
                    Log.e("ProfileFragment", "Document does not exist or is null");
                    Toast.makeText(getContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("ProfileFragment", "Error fetching user profile", task.getException());
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch the number of courses the user is enrolled in
    private void fetchCoursesCount(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);

        db.collection("current_lesson")
                .whereEqualTo("userId", userRef) // Use the DocumentReference for comparison
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int courseCount = task.getResult().size();
                        tvCourses.setText(courseCount + "\nCourses");
                    } else {
                        Log.e("ProfileFragment", "Error fetching courses", task.getException());
                        tvCourses.setText("0\nCourses");
                        Toast.makeText(getContext(), "Failed to fetch course count", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    String base64Image = compressAndConvertToBase64(imageUri);
                    if (base64Image != null) {
                        uploadAvatar(base64Image);
                    }
                } catch (Exception e) {
                    Log.e("ProfileFragment", "Error processing image", e);
                }
            }
        }
    }

    // Compress and convert image to Base64
    public String compressAndConvertToBase64(Uri imageUri) throws Exception {
        InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        // Resize the image (ensure it's no larger than 1000x1000 px)
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 1000, 1000, true);

        // Compress to JPEG with lower quality to reduce size (e.g., 60%)
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);

        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // Convert to Base64
        String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

        // Check if the Base64 string size exceeds Firestore document size limit (1MB)
        if (base64Image.length() > 1024 * 1024) { // 1MB limit for Base64 string
            Toast.makeText(getContext(), "Base64 image size exceeds the limit. Please reduce the image size.", Toast.LENGTH_SHORT).show();
            return null;
        }

        return base64Image;
    }



    public void uploadAvatar(String base64Image) {
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userDocRef = db.collection("users").document(userId);

        userDocRef.update("avatar", base64Image).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                loadAvatar(base64Image);
                Toast.makeText(getContext(), "Avatar updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to update avatar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadAvatar(String base64Image) {
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        avatarImageView.setImageBitmap(decodedByte);
    }

    private void navigateToCertificate() {
        // Implement navigation to certificate activity or fragment
        Toast.makeText(getContext(), "Navigating to Certificate", Toast.LENGTH_SHORT).show();
    }

    private void showLogoutConfirmationDialog() {
        // Inflate the custom layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_logout_confirmation, null);

        // Build the AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Set up Cancel button
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> dialog.dismiss()); // Dismiss dialog on cancel

        // Set up Logout button
        Button logoutButton = dialogView.findViewById(R.id.collect_points_button);
        logoutButton.setOnClickListener(v -> {
            dialog.dismiss(); // Dismiss dialog
            logout(); // Call the logout method
        });

        // Show the dialog
        dialog.show();
    }


    private void logout() {
        mAuth.signOut();
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        // Optionally redirect to login screen or perform other actions
        startActivity(new Intent(getActivity(), LoginActivity.class));
    }
}