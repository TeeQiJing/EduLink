package com.dellmau.edulink.activities;

import android.content.Intent;
import android.content.SharedPreferences;
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
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                checkUserRole(user.getUid());
                            } else {
                                Toast.makeText(this, "Email not verified. Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                resendVerificationEmail(user);
                            }
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Login failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void checkUserRole(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("user_role", userRole);
//        editor.apply();

        // Check 'students' collection
        db.collection("student").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        // User is a student
                        updateLoginStreak();

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("user_role", "Student");
                        editor.apply();
                        Toast.makeText(this, "Student Logged In.", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    } else {
                        // Check 'teachers' collection
                        db.collection("educator").document(userId).get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful() && task2.getResult().exists()) {
                                        updateLoginStreak();

                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("user_role", "Educator");
                                        editor.apply();
                                        Toast.makeText(this, "Educator Logged In.", Toast.LENGTH_SHORT).show();
                                        // User is an Educator
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        finish();
                                    } else {
                                        // Check 'admins' collection
                                        db.collection("employer").document(userId).get()
                                                .addOnCompleteListener(task3 -> {
                                                    if (task3.isSuccessful() && task3.getResult().exists()) {
                                                        updateLoginStreak();
                                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                                        editor.putString("user_role", "Employer");
                                                        editor.apply();
                                                        // User is an Employer
                                                        Toast.makeText(this, "Employer Logged In.", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(this, "No role assigned. Please contact support.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });
                    }
                });
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

    private void updateLoginStreak() {
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference loginStreakRef = FirebaseFirestore.getInstance().collection("login_streak").document(userId);

        loginStreakRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Timestamp lastLoginTimestamp = documentSnapshot.getTimestamp("lastLoginDate");
                long currentStreak = documentSnapshot.getLong("streak");

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
                        Log.d("LoginStreak", "Student has already logged in today.");
                    } else if (todayDate.getTime() - lastLoginDateOnly.getTime() == 24 * 60 * 60 * 1000) {
                        currentStreak++;
                        saveLoginStreak(loginStreakRef, currentStreak, todayDate);
                    } else {
                        currentStreak = 1;
                        saveLoginStreak(loginStreakRef, currentStreak, todayDate);
                    }
                }
            } else {
                saveLoginStreak(loginStreakRef, 1, new Date());
            }
        }).addOnFailureListener(e -> Log.e("LoginStreak", "Error fetching login streak data", e));
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
