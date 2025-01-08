package com.dellmau.edulink.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dellmau.edulink.R;
import com.dellmau.edulink.models.Student;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText etFeedback;
    private MaterialButton btnSubmitFeedback;
    private ImageView avatarImageView;
    private EditText etUsername, etBio, etOldPass, etNewPass, etConfirmPass;
    private LinearLayout btnEditProfile, btnChangePassword;
    SharedPreferences sharedPreferences;
    String user_role;
    public SettingsFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        btnEditProfile = rootView.findViewById(R.id.btnEditProfile);
        btnChangePassword = rootView.findViewById(R.id.btnChangePassword);


        // Initialize the toolbar
        MaterialToolbar toolbar = rootView.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        btnEditProfile.setOnClickListener(v -> showEditProfileConfirmationDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordConfirmationDialog());
        sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        user_role = sharedPreferences.getString("user_role", "");

        return rootView;
    }

    private void showEditProfileConfirmationDialog(){

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);



        // Build the AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        etUsername = dialogView.findViewById(R.id.ETUsername);
        etBio = dialogView.findViewById(R.id.ETBio);
        avatarImageView = dialogView.findViewById(R.id.profileImage);

        avatarImageView.setOnClickListener(v -> openImagePicker());
        loadUserProfile();


        // Set up Cancel button
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> dialog.dismiss()); // Dismiss dialog on cancel



        // Set up Save button
        Button saveBtn = dialogView.findViewById(R.id.save_button);
        saveBtn.setOnClickListener(v -> {
            dialog.dismiss(); // Dismiss dialog
            saveUserProfile(); // Call the saveUserProfile method
        });

        // Show the dialog
        dialog.show();

    }
    private void showChangePasswordConfirmationDialog(){

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);



        // Build the AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        etOldPass = dialogView.findViewById(R.id.ETOldPass);
        etNewPass = dialogView.findViewById(R.id.ETNewPass);
        etConfirmPass = dialogView.findViewById(R.id.ETConfirmPass);


        // Set up Cancel button
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> dialog.dismiss()); // Dismiss dialog on cancel


        // Set up Save button
        Button saveBtn = dialogView.findViewById(R.id.save_button);
        saveBtn.setOnClickListener(v -> {

            saveNewPassword(dialog); // Call the saveNewPassword method
        });

        // Show the dialog
        dialog.show();

    }

    private void saveNewPassword(AlertDialog dialog) {
        String oldPassword = etOldPass.getText().toString().trim();
        String newPassword = etNewPass.getText().toString().trim();
        String confirmPassword = etConfirmPass.getText().toString().trim();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 8) {
            Toast.makeText(getContext(), "New password must be at least 8 characters long.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.matches(".*[A-Z].*")) {  // At least one uppercase letter
            Toast.makeText(getContext(), "Password must include at least one uppercase letter.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!newPassword.matches(".*[a-z].*")) {  // At least one lowercase letter
            Toast.makeText(getContext(), "Password must include at least one lowercase letter.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!newPassword.matches(".*\\d.*")) {  // At least one number
            Toast.makeText(getContext(), "Password must include at least one number.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!newPassword.matches(".*[@#$%^&+=!].*")) {  // At least one special character
            Toast.makeText(getContext(), "Password must include at least one special character (@#$%^&+=!).", Toast.LENGTH_LONG).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(getContext(), "New password and confirm password do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = mAuth.getCurrentUser().getEmail();
        if (email == null) {
            Toast.makeText(getContext(), "Student email not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reauthenticate user with old password
        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update to new password
                        mAuth.getCurrentUser().updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(getContext(), "Password updated successfully.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Failed to update password: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                    dialog.dismiss();
                                });
                    } else {
                        Toast.makeText(getContext(), "Authentication failed. Please check your old password.", Toast.LENGTH_SHORT).show();

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
                    Student user = documentSnapshot.toObject(Student.class);
                    if (user != null) {
                        etUsername.setText(user.getUsername());
                        etBio.setText(user.getUser_bio().isEmpty() ? "" : user.getUser_bio());


                        // Load avatar if available
                        if (user.getAvatar().isEmpty()) {
                            avatarImageView.setImageResource(R.drawable.ic_avatar); // Default avatar
                        } else {
                            loadAvatar(user.getAvatar());
                        }


                    }
                } else {
                    Log.e("SettingsFragment", "Document does not exist or is null");
                    Toast.makeText(getContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("SettingsFragment", "Error fetching user profile", task.getException());
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
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
                    // Show the selected image in the avatar preview
                    InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    // Resize the image for preview purposes
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 1000, 1000, true);
                    avatarImageView.setImageBitmap(scaledBitmap);

                } catch (Exception e) {
                    Log.e("SettingsFragment", "Error processing image", e);
                }
            }
        }
    }

    private void saveUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userDocRef = db.collection(user_role.toLowerCase()).document(userId);

        // Get updated values from EditText fields
        String updatedUsername = etUsername.getText().toString().trim();
        String updatedBio = etBio.getText().toString().trim();

        // Prepare a map to store updated fields
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("username", updatedUsername);
        updatedData.put("user_bio", updatedBio);

        // Handle avatar image
        avatarImageView.setDrawingCacheEnabled(true);
        avatarImageView.buildDrawingCache();
        Bitmap avatarBitmap = avatarImageView.getDrawingCache();
        if (avatarBitmap != null) {
            try {
                // Compress and convert the bitmap to Base64
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                avatarBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
                String updatedAvatar = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
                updatedData.put("avatar", updatedAvatar);
            } catch (Exception e) {
                Log.e("SettingsFragment", "Error processing avatar image", e);
                Toast.makeText(getContext(), "Failed to process avatar image", Toast.LENGTH_SHORT).show();
                return; // Stop further processing
            }
        }

        // Update the user profile in Firestore
        userDocRef.update(updatedData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("SettingsFragment", "Error updating profile", task.getException());
                Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void loadAvatar(String base64Image) {
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        avatarImageView.setImageBitmap(decodedByte);
    }

}