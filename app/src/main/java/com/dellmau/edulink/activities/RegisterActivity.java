package com.dellmau.edulink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dellmau.edulink.models.Educator;
import com.dellmau.edulink.models.Employer;
import com.dellmau.edulink.models.Student;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.dellmau.edulink.databinding.ActivityRegisterBinding;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    Spinner spinnerUserRole, spinnerOrganization;
    EditText etCustomOrganization;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        spinnerUserRole = binding.spinnerUserRole;
        spinnerOrganization = binding.spinnerOrganization;
        etCustomOrganization = binding.etCustomOrganization;

        spinnerUserRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRole = parent.getItemAtPosition(position).toString();
                if (selectedRole.equals("Student") || selectedRole.equals("Educator")) {
                    fetchOrganizations("university_list");
                } else if (selectedRole.equals("Employer")) {
                    fetchOrganizations("company_list");
                } else {
                    spinnerOrganization.setVisibility(View.GONE);
                    etCustomOrganization.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Handle organization selection
        spinnerOrganization.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOrganization = parent.getItemAtPosition(position).toString();
                if (selectedOrganization.equals("Other")) {
                    etCustomOrganization.setVisibility(View.VISIBLE);
                } else {
                    etCustomOrganization.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.tvLoginHere.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        });

        binding.btnSignUp.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
            String selectedRole = spinnerUserRole.getSelectedItem().toString();
            String selectedOrganization = spinnerOrganization.getSelectedItem() != null ? spinnerOrganization.getSelectedItem().toString() : "";

            if (selectedOrganization.equals("Other")) {
                selectedOrganization = etCustomOrganization.getText().toString().trim();
            }

            signUp(username, email, password, confirmPassword, selectedRole, selectedOrganization);
        });
    }

    // Fetch organizations from Firebase
    private void fetchOrganizations(String collectionName) {
        CollectionReference collectionRef = db.collection(collectionName);
        collectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> organizationList = new ArrayList<>();
                organizationList.add("Please Select Your Organization");
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String name = document.getString("name");
                    organizationList.add(name);
                }
                organizationList.add("Other");  // Option for custom input

                ArrayAdapter<String> adapter = new ArrayAdapter<>(RegisterActivity.this, android.R.layout.simple_spinner_item, organizationList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerOrganization.setAdapter(adapter);

                spinnerOrganization.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(RegisterActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signUp(String username, String email, String password, String confirmPassword, String role, String organization) {
        // Validate username
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password complexity
        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty.", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters long.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.matches(".*[A-Z].*")) {  // At least one uppercase letter
            Toast.makeText(this, "Password must include at least one uppercase letter.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.matches(".*[a-z].*")) {  // At least one lowercase letter
            Toast.makeText(this, "Password must include at least one lowercase letter.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.matches(".*\\d.*")) {  // At least one number
            Toast.makeText(this, "Password must include at least one number.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.matches(".*[@#$%^&+=!].*")) {  // At least one special character
            Toast.makeText(this, "Password must include at least one special character (@#$%^&+=!).", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {  // Password and ConfirmPassword must be the same
            Toast.makeText(this, "Confirm Password does not match the New Password", Toast.LENGTH_LONG).show();
            return;
        }
        if(role.equals("Please Select Your Role")){
            Toast.makeText(this, "Please select a valid role", Toast.LENGTH_LONG).show();
            return;
        }
        if(organization.equals("Please Select Your Organization") || organization.equals("")){
            Toast.makeText(this, "Please select a valid organization", Toast.LENGTH_LONG).show();
            return;
        }


        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        // Hash the password using Bcrypt
        String hashedPassword = hashPassword(password);


        // Register the user with Firebase
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();

                            // Generate current date
                            String registrationDate = java.text.DateFormat.getDateInstance().format(new java.util.Date());


                            handleOrganization(role.equals("Employer") ? "company_list" : "university_list", organization, organizationRef -> {
                                Object newUser = createUserModel(role, username, email, hashedPassword, registrationDate, organizationRef);
                                saveUserToFirestore(role.toLowerCase(), userId, newUser);
                            });



                        }
                    } else {
                        if (task.getException() != null) {
                            Log.e("SignupError", task.getException().getMessage());
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // Save user to Firestore
    private void saveUserToFirestore(String collectionName, String userId, Object user) {
        db.collection(collectionName).document(userId).set(user).addOnCompleteListener(databaseTask -> {
            if (databaseTask.isSuccessful()) {
                FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(emailTask -> {
                    if (emailTask.isSuccessful()) {
                        Toast.makeText(this, "Registration successful. Verify your email.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                if (databaseTask.getException() != null) {
                    Log.e("DatabaseError", databaseTask.getException().getMessage());
                    Toast.makeText(this, "Failed to save user data: " + databaseTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private Object createUserModel(String role, String username, String email, String hashedPassword, String registrationDate, DocumentReference organizationRef) {
        Map<String, Integer> skillPoints = new HashMap<>();
        switch (role) {
            case "Student":
                skillPoints.put("UI/UX Design", 0);
                skillPoints.put("Code Simplicity", 0);
                skillPoints.put("Problem Solving", 0);
                skillPoints.put("Backend", 0);
                skillPoints.put("Creativity", 0);
                return new Student(username, email, hashedPassword, registrationDate, registrationDate, 0, "", "", organizationRef, skillPoints);

            case "Educator":
                skillPoints.put("Risk Management", 0);
                skillPoints.put("Adaptability", 0);
                skillPoints.put("Team Leadership", 0);
                skillPoints.put("Industry Knowledge", 0);
                skillPoints.put("Project Management", 0);
                return new Educator(username, email, hashedPassword, registrationDate, registrationDate, 0, "", "", organizationRef, skillPoints);

            case "Employer":
                return new Employer(username, email, hashedPassword, registrationDate, registrationDate, "", "", organizationRef);

            default:
                return null;
        }
    }
    // Handle organization reference
    private void handleOrganization(String collectionName, String organizationName, OnSuccessListener<DocumentReference> callback) {
        CollectionReference collectionRef = db.collection(collectionName);

        // Check if organization exists
        collectionRef.whereEqualTo("name", organizationName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // If organization exists, return the reference
                        DocumentReference organizationRef = task.getResult().getDocuments().get(0).getReference();
                        callback.onSuccess(organizationRef);
                    } else {
                        // If organization doesn't exist, create a new record
                        Map<String, Object> newOrganization = new HashMap<>();
                        newOrganization.put("name", organizationName);

                        collectionRef.add(newOrganization)
                                .addOnSuccessListener(documentReference -> {
                                    // Return the new document reference
                                    callback.onSuccess(documentReference);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(RegisterActivity.this, "Failed to add organization: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    // Method to hash the password using Bcrypt
    private String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12)); // 12 is the work factor
    }

}
