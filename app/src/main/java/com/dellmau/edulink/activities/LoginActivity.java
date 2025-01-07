package com.dellmau.edulink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dellmau.edulink.activities.ForgotPasswordActivity;
import com.dellmau.edulink.activities.MainActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.dellmau.edulink.databinding.ActivityLoginBinding;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding; // ViewBinding for layout
    private FirebaseAuth mAuth; // FirebaseAuth instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Using DataBinding to set the content view
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth

        binding.tvRegisterHere.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
        });

        // Login Button Click Listener
        binding.btnLogin.setOnClickListener(v -> {
            loginUser();
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));
        });
    }

    private void loginUser() {
        // Get input from EditTexts
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Validate email and password
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform Firebase authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful, now check email verification
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                // If email is verified, allow login
                                updateLoginStreak();
                                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // If email is not verified, inform the user without signing out
                                Toast.makeText(this, "Email not verified. Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                resendVerificationEmail(user); // Offer to resend the verification email
                            }
                        }
                    } else {
                        // Login failed, display error
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Login failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateLoginStreak() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference loginStreakRef = FirebaseFirestore.getInstance().collection("login_streak").document(userId);

        loginStreakRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Existing streak data
                Timestamp lastLoginTimestamp = documentSnapshot.getTimestamp("lastLoginDate");
                long currentStreak = documentSnapshot.getLong("streak");

                // Get today's date (ignoring time)
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);
                Date todayDate = today.getTime();

                if (lastLoginTimestamp != null) {
                    Calendar lastLoginCalendar = Calendar.getInstance();
                    lastLoginCalendar.setTime(lastLoginTimestamp.toDate());
                    lastLoginCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    lastLoginCalendar.set(Calendar.MINUTE, 0);
                    lastLoginCalendar.set(Calendar.SECOND, 0);
                    lastLoginCalendar.set(Calendar.MILLISECOND, 0);
                    Date lastLoginDateOnly = lastLoginCalendar.getTime();

                    if (todayDate.equals(lastLoginDateOnly)) {
                        // User already logged in today, no updates needed
                        Log.d("LoginStreak", "User has already logged in today.");
                    } else if (todayDate.getTime() - lastLoginDateOnly.getTime() == 24 * 60 * 60 * 1000) {
                        // Consecutive login (next day), increment streak
                        currentStreak++;
                        saveLoginStreak(loginStreakRef, currentStreak, todayDate);
                    } else {
                        // Non-consecutive login, reset streak to 1
                        currentStreak = 1;
                        saveLoginStreak(loginStreakRef, currentStreak, todayDate);
                    }
                }
            } else {
                // No existing streak data, create new record
                saveLoginStreak(loginStreakRef, 1, new Date());
            }
        }).addOnFailureListener(e -> Log.e("LoginStreak", "Error fetching login streak data", e));
    }

    private void saveLoginStreak(DocumentReference loginStreakRef, long streak, Date todayDate) {
        Map<String, Object> streakData = new HashMap<>();
        streakData.put("streak", streak);
        streakData.put("lastLoginDate", new Timestamp(todayDate));
        streakData.put("isPointCollected", false); // Set isPointCollected to false whenever updating streak

        loginStreakRef.set(streakData).addOnSuccessListener(aVoid -> {
            Log.d("LoginStreak", "Streak data updated successfully: Streak = " + streak);
        }).addOnFailureListener(e -> {
            Log.e("LoginStreak", "Error updating streak data", e);
        });
    }

    /**
     * Resend email verification link if the user hasn't verified their email.
     */
    private void resendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Failed to send email.";
                        Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Helper method to validate email format.
     */
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
