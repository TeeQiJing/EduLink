package com.dellmau.edulink.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dellmau.edulink.R;
import com.dellmau.edulink.models.Question;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizFragment extends Fragment {
    private static final String TAG = "QuizFragment";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView questionTitle;
    private ImageView questionImage;
    private Button optionA, optionB, optionC, optionD, nextButton, previousButton;
    private String quizId;
    private String userId;
    private List<Question> questions = new ArrayList<>();
    private List<String> questionIds = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private boolean isAnswered = false;
    SharedPreferences sharedPreferences;
    String user_role;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        user_role = sharedPreferences.getString("user_role", "");

        // Bind views
        questionTitle = view.findViewById(R.id.questionTitle);
        questionImage = view.findViewById(R.id.questionImage);
        optionA = view.findViewById(R.id.optionA);
        optionB = view.findViewById(R.id.optionB);
        optionC = view.findViewById(R.id.optionC);
        optionD = view.findViewById(R.id.optionD);
        nextButton = view.findViewById(R.id.nextButton);
        previousButton = view.findViewById(R.id.previousButton);

        // Get quizId and userId from arguments
        quizId = getArguments() != null ? getArguments().getString("quizId") : null;
        String quizTitle = getArguments() != null ? getArguments().getString("quizTitle") : null;
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.d(TAG, "Received quizId: " + quizId);
        Log.d(TAG, "Received userId: " + userId);

        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);



        // Set the title programmatically
        topAppBar.setTitle(quizTitle);
        topAppBar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        if (quizId != null && userId != null) {
            loadQuizData(quizId);
        } else {
            Log.e(TAG, "Quiz ID or Student ID is missing");
            Toast.makeText(getContext(), "Quiz ID or Student ID is missing", Toast.LENGTH_SHORT).show();
        }

        // Handle navigation buttons
        nextButton.setOnClickListener(v -> {
            if (currentQuestionIndex < questions.size() - 1) {
                currentQuestionIndex++;
                displayQuestion(questions.get(currentQuestionIndex));
            }
        });

        previousButton.setOnClickListener(v -> {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                displayQuestion(questions.get(currentQuestionIndex));
            }
        });

        return view;
    }

    private void loadQuizData(String quizId) {
        Log.d(TAG, "Loading quiz data for quizId: " + quizId);

        DocumentReference quizReference = db.collection("quiz").document(quizId);

        db.collection("questions")
                .whereEqualTo("ref", quizReference)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "Successfully loaded quiz data");
                        for (DocumentSnapshot document : task.getResult()) {
                            Question question = document.toObject(Question.class);
                            if (question != null) {
                                questions.add(question);
                                questionIds.add(document.getId()); // Store the question ID
                                Log.d(TAG, "Loaded question: " + question.getTitle() + " with ID: " + document.getId());
                            }
                        }

                        if (!questions.isEmpty()) {
                            displayQuestion(questions.get(0)); // Display the first question
                        } else {
                            Log.e(TAG, "No questions found for this quiz");
                            Toast.makeText(getContext(), "No questions found for this quiz.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Failed to load quiz data", task.getException());
                        Toast.makeText(getContext(), "Failed to load quiz data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

//    private void displayQuestion(Question question) {
//        Log.d(TAG, "Displaying question: " + question.getTitle());
//
//        // Reset state for new question
//        isAnswered = false;
//        resetButtonColors();
//
//        // Update visibility of navigation buttons
//        previousButton.setVisibility(currentQuestionIndex > 0 ? View.VISIBLE : View.GONE);
//
//        // Enable answer buttons
//        optionA.setEnabled(true);
//        optionB.setEnabled(true);
//        optionC.setEnabled(true);
//        optionD.setEnabled(true);
//
//        // Set question title
//        questionTitle.setText("Q" + (currentQuestionIndex + 1) + ". " + question.getTitle());
//
//        // Load image if available
//        String imageUrl = question.getImageUrl();
//        if (imageUrl != null && !imageUrl.isEmpty()) {
//            questionImage.setVisibility(View.VISIBLE);
//            Glide.with(getContext()).load(imageUrl).into(questionImage);
//        } else {
//            questionImage.setVisibility(View.GONE);
//        }
//
//        // Set options
//        List<String> options = question.getOptions();
//        if (options.size() >= 4) {
//            optionA.setText(options.get(0));
//            optionB.setText(options.get(1));
//            optionC.setText(options.get(2));
//            optionD.setText(options.get(3));
//
//            optionA.setOnClickListener(v -> processAnswer(question, options.get(0), optionA));
//            optionB.setOnClickListener(v -> processAnswer(question, options.get(1), optionB));
//            optionC.setOnClickListener(v -> processAnswer(question, options.get(2), optionC));
//            optionD.setOnClickListener(v -> processAnswer(question, options.get(3), optionD));
//        }
//
//        // Check if the question has already been answered
//        restoreAnswerState(question);
//
//        // Update nextButton text and behavior
//        if (currentQuestionIndex == questions.size() - 1) {
//            nextButton.setText("Finish");
//            nextButton.setOnClickListener(v -> quitToPreviousFragment());
//        } else {
//            nextButton.setText("Next");
//            nextButton.setOnClickListener(v -> {
//                if (currentQuestionIndex < questions.size() - 1) {
//                    currentQuestionIndex++;
//                    displayQuestion(questions.get(currentQuestionIndex));
//                }
//            });
//        }
//    }

    private void displayQuestion(Question question) {
        Log.d(TAG, "Displaying question: " + question.getTitle());

        // Reset state for new question
        isAnswered = false;
        resetButtonColors();

        // Update visibility of navigation buttons
        previousButton.setVisibility(currentQuestionIndex > 0 ? View.VISIBLE : View.GONE);

        // Enable answer buttons
        optionA.setEnabled(true);
        optionB.setEnabled(true);
        optionC.setEnabled(true);
        optionD.setEnabled(true);

        // Set question title
        questionTitle.setText("Q" + (currentQuestionIndex + 1) + ". " + question.getTitle());

        // Load image if available
        String imageUrl = question.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            questionImage.setVisibility(View.VISIBLE);
            Glide.with(getContext()).load(imageUrl).into(questionImage);
        } else {
            questionImage.setVisibility(View.GONE);
        }

        // Set options
        List<String> options = question.getOptions();
        if (options.size() >= 4) {
            optionA.setText(options.get(0));
            optionB.setText(options.get(1));
            optionC.setText(options.get(2));
            optionD.setText(options.get(3));

            optionA.setOnClickListener(v -> processAnswer(question, options.get(0), optionA));
            optionB.setOnClickListener(v -> processAnswer(question, options.get(1), optionB));
            optionC.setOnClickListener(v -> processAnswer(question, options.get(2), optionC));
            optionD.setOnClickListener(v -> processAnswer(question, options.get(3), optionD));
        }

        // Check if the question has already been answered
        restoreAnswerState(question);

        // Update nextButton text and behavior
        if (currentQuestionIndex == questions.size() - 1) {
            nextButton.setText("Finish");
            nextButton.setOnClickListener(v -> quitToPreviousFragment());
        } else {
            nextButton.setText("Next");
            nextButton.setOnClickListener(v -> {
                if (currentQuestionIndex < questions.size() - 1) {
                    currentQuestionIndex++;
                    displayQuestion(questions.get(currentQuestionIndex));
                }
            });
        }
    }


    private void quitToPreviousFragment() {
        Log.d(TAG, "Navigating back to previous fragment and saving quiz progress.");
        if (quizId == null || userId == null) {
            Log.e(TAG, "Quiz ID or Student ID is missing. Cannot update quiz progress.");
            return;
        }

        // Reference to the `quiz_progress` collection
        db.collection("quiz_progress")
                .whereEqualTo("userIdRef", db.collection(user_role.toLowerCase()).document(userId))
                .whereEqualTo("quizIdRef", db.collection("quiz").document(quizId))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().isEmpty()) {
                        // If no progress exists, add a new record
                        Map<String, Object> progressData = new HashMap<>();
                        progressData.put("userIdRef", db.collection(user_role.toLowerCase()).document(userId));
                        progressData.put("quizIdRef", db.collection("quiz").document(quizId));
                        progressData.put("completedAt", FieldValue.serverTimestamp());

                        db.collection("quiz_progress")
                                .add(progressData)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d(TAG, "Quiz progress successfully saved.");
                                    // Navigate to the previous fragment

                                    navigateBack();
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Error saving quiz progress", e));
                    } else if (task.isSuccessful()) {
                        Log.d(TAG, "Quiz progress already exists. No action needed.");
                        navigateBack(); // Navigate to the previous fragment
                    } else {
                        Log.e(TAG, "Failed to check quiz progress", task.getException());
                    }
                });
    }


    // Helper method to navigate back to the previous fragment
    private void navigateBack() {
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            if (fragmentManager != null) {
                fragmentManager.popBackStack(); // Go back to the previous fragment in the stack
            } else {
                Log.e(TAG, "FragmentManager is null.");
            }
        } else {
            Log.e(TAG, "Activity is null.");
        }
    }

//    private void processAnswer(Question question, String selectedAnswer, Button selectedButton) {
//        if (isAnswered) return;
//
//        isAnswered = true;
//        resetButtonColors();
//
//        saveUserAnswer(question, selectedAnswer);
//
//        if (selectedAnswer.equals(question.getAnswer())) {
//            selectedButton.setBackgroundColor(Color.GREEN);
//        } else {
//            selectedButton.setBackgroundColor(Color.RED);
//            highlightCorrectAnswer(question);
//        }
//
//        nextButton.setVisibility(View.VISIBLE);
//    }

    private void processAnswer(Question question, String selectedAnswer, Button selectedButton) {
        if (isAnswered) return;

        isAnswered = true;
        resetButtonColors();

        saveUserAnswer(question, selectedAnswer);

        if (selectedAnswer.equals(question.getAnswer())) {
            selectedButton.setBackgroundColor(Color.GREEN);
        } else {
            selectedButton.setBackgroundColor(Color.RED);
            highlightCorrectAnswer(question);
        }

        // Show the appropriate button after answering
        if (currentQuestionIndex == questions.size() - 1) {
            nextButton.setText("Finish");
        } else {
            nextButton.setText("Next");
        }
        nextButton.setVisibility(View.VISIBLE);
    }


//    private void restoreAnswerState(Question question) {
//        String questionId = questionIds.get(currentQuestionIndex);
//        Log.d(TAG, "Restoring progress for question ID: " + questionId);
//
//        db.collection("user_question")
//                .whereEqualTo("userIdRef", db.collection("users").document(userId))
//                .whereEqualTo("questionIdRef", db.collection("questions").document(questionId))
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
//                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
//                        String selectedAnswer = document.getString("selectedAnswer");
//                        Boolean isCorrect = document.getBoolean("isCorrect");
//
//                        if (selectedAnswer != null && isCorrect != null) {
//                            highlightAnswer(question, selectedAnswer, isCorrect);
//                            disableOptions(); // Disable only if the question is answered
//                        }
//                    }
//                });
//    }

    private void restoreAnswerState(Question question) {
        String questionId = questionIds.get(currentQuestionIndex);
        Log.d(TAG, "Restoring progress for question ID: " + questionId);

        db.collection("user_question")
                .whereEqualTo("userIdRef", db.collection(user_role.toLowerCase()).document(userId))
                .whereEqualTo("questionIdRef", db.collection("questions").document(questionId))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String selectedAnswer = document.getString("selectedAnswer");
                        Boolean isCorrect = document.getBoolean("isCorrect");

                        if (selectedAnswer != null && isCorrect != null) {
                            highlightAnswer(question, selectedAnswer, isCorrect);
                            disableOptions();
                            nextButton.setVisibility(View.VISIBLE); // Show Next/Finish button if the question is answered
                        }
                    } else {
                        Log.d(TAG, "No answer recorded for this question.");
                        nextButton.setVisibility(View.GONE); // Hide Next/Finish button if no answer is recorded
                    }
                });
    }


    private void saveUserAnswer(Question question, String selectedAnswer) {
        String questionId = questionIds.get(currentQuestionIndex);
        DocumentReference userRef = db.collection(user_role.toLowerCase()).document(userId);
        DocumentReference questionRef = db.collection("questions").document(questionId);

        boolean isCorrect = selectedAnswer.equals(question.getAnswer());
        int questionScore = question.getScore(); // Get the score for this question

        Map<String, Object> userAnswerData = new HashMap<>();
        userAnswerData.put("userIdRef", userRef);
        userAnswerData.put("questionIdRef", questionRef);
        userAnswerData.put("selectedAnswer", selectedAnswer);
        userAnswerData.put("isCorrect", isCorrect);

        db.collection("user_question")
                .add(userAnswerData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Answer saved successfully.");
//                        if (isCorrect) {
//                            incrementUserXP(questionScore); // Pass the score for XP increment
//                        }
                    } else {
                        Log.e(TAG, "Failed to save answer", task.getException());
                    }
                });
    }
//    private void incrementUserXP(int score) {
//        DocumentReference userRef = db.collection(user_role.toLowerCase()).document(userId);
//
//        db.runTransaction(transaction -> {
//                    DocumentSnapshot snapshot = transaction.get(userRef);
//                    Long currentXP = snapshot.getLong("xp");
//                    if (currentXP == null) currentXP = 0L; // Default XP if not set
//
//                    // Increment XP by the score of the current question
//                    transaction.update(userRef, "xp", currentXP + score);
//                    return null;
//                }).addOnSuccessListener(aVoid -> Log.d(TAG, "Student XP incremented by " + score + " successfully."))
//                .addOnFailureListener(e -> Log.e(TAG, "Failed to increment user XP", e));
//    }


    private void highlightAnswer(Question question, String selectedAnswer, boolean isCorrect) {
        if (optionA.getText().equals(selectedAnswer)) optionA.setBackgroundColor(isCorrect ? Color.GREEN : Color.RED);
        if (optionB.getText().equals(selectedAnswer)) optionB.setBackgroundColor(isCorrect ? Color.GREEN : Color.RED);
        if (optionC.getText().equals(selectedAnswer)) optionC.setBackgroundColor(isCorrect ? Color.GREEN : Color.RED);
        if (optionD.getText().equals(selectedAnswer)) optionD.setBackgroundColor(isCorrect ? Color.GREEN : Color.RED);
        highlightCorrectAnswer(question);
    }

    private void highlightCorrectAnswer(Question question) {
        if (optionA.getText().equals(question.getAnswer())) optionA.setBackgroundColor(Color.GREEN);
        if (optionB.getText().equals(question.getAnswer())) optionB.setBackgroundColor(Color.GREEN);
        if (optionC.getText().equals(question.getAnswer())) optionC.setBackgroundColor(Color.GREEN);
        if (optionD.getText().equals(question.getAnswer())) optionD.setBackgroundColor(Color.GREEN);
    }

    private void resetButtonColors() {
        int purpleColor = ContextCompat.getColor(getContext(), R.color.purple_500);

        // Reset the background color of each option button
        optionA.setBackgroundColor(purpleColor);
        optionB.setBackgroundColor(purpleColor);
        optionC.setBackgroundColor(purpleColor);
        optionD.setBackgroundColor(purpleColor);

        // Ensure text color is consistent (optional, if needed)
        optionA.setTextColor(Color.WHITE);
        optionB.setTextColor(Color.WHITE);
        optionC.setTextColor(Color.WHITE);
        optionD.setTextColor(Color.WHITE);
    }

    private void disableOptions() {
        optionA.setEnabled(false);
        optionB.setEnabled(false);
        optionC.setEnabled(false);
        optionD.setEnabled(false);
    }
}
