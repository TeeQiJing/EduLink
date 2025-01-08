package com.dellmau.edulink.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dellmau.edulink.R;




import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.dellmau.edulink.R;
import com.dellmau.edulink.adapters.ChapterAdapter;
import com.dellmau.edulink.models.Chapter;
import com.dellmau.edulink.models.Quiz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class LessonFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final String TAG = "LessonFragment";

    private TextView tvLessonTitle;
    private TextView tvLessonRatingText;
    private ConstraintLayout CLbtn;
    private ImageView lessonImageView, btnFav;
    private RecyclerView chapterRecyclerView;
    private Button btnEnroll;

    private ChapterAdapter chapterAdapter;
    private List<Object> contentList; // List to hold chapters and quizzes
    private String lessonId;
    private boolean isEnrolled = false; // Flag to check enrollment status
    private boolean isFavorited = false;
    private Map<String, Boolean> completionStateMap; // To track completion state of chapters and quizzes

    public LessonFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        contentList = new ArrayList<>();
        completionStateMap = new HashMap<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lesson, container, false);

        tvLessonTitle = rootView.findViewById(R.id.lessonTitle);
        tvLessonRatingText = rootView.findViewById(R.id.lessonRatingText);
        lessonImageView = rootView.findViewById(R.id.lessonImage);
        chapterRecyclerView = rootView.findViewById(R.id.chapterRecycleView);
        btnEnroll = rootView.findViewById(R.id.btnEnroll);
        btnFav = rootView.findViewById(R.id.btnFav);
        CLbtn = rootView.findViewById(R.id.CLbtn);
        MaterialToolbar toolbar = rootView.findViewById(R.id.materialToolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        btnEnroll.setVisibility(View.GONE);

        chapterRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        chapterAdapter = new ChapterAdapter(getContext(), contentList, completionStateMap, item -> {
            if (!isEnrolled) {
                Toast.makeText(getContext(), "Please enroll in the lesson first.", Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (item instanceof Chapter) {  // Check if the item is a Chapter
                    // Handle chapter click
                    Chapter selectedChapter = (Chapter) item;
                    String chapterId = selectedChapter.getId();  // Get the chapter ID
                    if (chapterId != null && !chapterId.isEmpty()) {
                        ContentFragment contentFragment = new ContentFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("chapterId", chapterId);  // Pass the chapter ID
                        bundle.putString("chapterTitle", selectedChapter.getTitle());  // Pass the chapter Title
                        contentFragment.setArguments(bundle);
                        FragmentManager fragmentManager = getActivity() != null ? getActivity().getSupportFragmentManager() : null;

                        if (fragmentManager != null) {
                            fragmentManager.beginTransaction()
                                    .setCustomAnimations(
                                            R.anim.slide_in_right,  // Animation for fragment entry
                                            R.anim.slide_out_left, // Animation for fragment exit
                                            R.anim.slide_in_left,  // Animation for returning to the fragment
                                            R.anim.slide_out_right // Animation for exiting back
                                    )
                                    .replace(R.id.fragment_container, contentFragment)
                                    .addToBackStack(null)
                                    .commitAllowingStateLoss();
                        }

                    } else {
                        Toast.makeText(getContext(), "Chapter content is empty", Toast.LENGTH_SHORT).show();
                    }
                } else if (item instanceof Quiz) {  // Check if the item is a Quiz
                    // Handle quiz click
                    Quiz selectedQuiz = (Quiz) item;
                    String quizTitle = selectedQuiz.getTitle();  // Get the quiz title
                    if (quizTitle != null && !quizTitle.isEmpty()) {
                        QuizFragment quizFragment = new QuizFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("quizTitle", quizTitle);  // Pass the quiz title
                        bundle.putString("quizId", selectedQuiz.getId());  // Pass the quiz ID (if you need it)
                        quizFragment.setArguments(bundle);
                        FragmentManager fragmentManager = getActivity() != null ? getActivity().getSupportFragmentManager() : null;
                        if (fragmentManager != null) {
                            fragmentManager.beginTransaction()
                                    .setCustomAnimations(
                                            R.anim.slide_in_right,  // Animation for fragment entry
                                            R.anim.slide_out_left, // Animation for fragment exit
                                            R.anim.slide_in_left,  // Animation for returning to the fragment
                                            R.anim.slide_out_right // Animation for exiting back
                                    )
                                    .replace(R.id.fragment_container, quizFragment)
                                    .addToBackStack(null)
                                    .commitAllowingStateLoss();
                        }
                    } else {
                        Toast.makeText(getContext(), "Quiz content is empty", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Unknown item type", Toast.LENGTH_SHORT).show();
                }
            }
        });

        chapterRecyclerView.setAdapter(chapterAdapter);

        lessonId = getArguments().getString("lessonId");
        loadLessonData(lessonId);
        checkAndGrantMilestoneMasterBadge();
        checkAndGrantQuizWhizBadge();

        checkEnrollmentStatus();

        btnEnroll.setOnClickListener(view -> {
            String userId = mAuth.getCurrentUser().getUid();
            enrollInLesson(userId, lessonId);
            checkAndGrantFirstStepBadge();
        });

        btnFav.setOnClickListener(v -> {
            // Load the click animation
            Animation clickAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.scale_click_animation);
            v.startAnimation(clickAnimation);

            // Toggle favorite status
            if (isFavorited) {
                isFavorited = false;
                btnFav.setImageResource(R.drawable.ic_love); // Replace with "not favorite" icon
            } else {
                isFavorited = true;
                btnFav.setImageResource(R.drawable.ic_love_filled); // Replace with "favorite" icon
            }
        });

        return rootView;
    }

    private void checkEnrollmentStatus() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("current_lesson")
                .whereEqualTo("userId", db.collection("users").document(userId))
                .whereEqualTo("lessonId", db.collection("total_lesson").document(lessonId))
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed.", error);
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        isEnrolled = true;
                        btnEnroll.setVisibility(View.GONE);
                        CLbtn.setVisibility(View.GONE);
                    } else {
                        isEnrolled = false;
                        btnEnroll.setVisibility(View.VISIBLE);
                    }
                    // Notify the adapter to refresh UI
                    chapterAdapter.notifyDataSetChanged();
                });
    }
    private void checkAndGrantFirstStepBadge() {
        String userId = mAuth.getCurrentUser().getUid();

        // Get a reference to the badge document by using the badgeId
        DocumentReference badgeRef = db.collection("total_badges").document("5j5PlMVp5CQH4rs0zYZJ"); // Using the direct badge ID

        // Get a reference to the user document
        DocumentReference userRef = db.collection("users").document(userId);

        // Check if the user is enrolled in a course by looking for their record in the current_lesson collection
        db.collection("current_lesson")
                .whereEqualTo("userId", userRef)  // Compare userId with the DocumentReference
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // If the user is enrolled in a course, check the size of the result
                        if (task.getResult().size() == 1) {
                            // Student is enrolling for the first time, so we grant the badge
                            Log.d(TAG, "Student is enrolling for the first time.");
                            // Check if the user has the "First step" badge
                            db.collection("user_badges")
                                    .whereEqualTo("userIdRef", userRef)
                                    .whereEqualTo("badgeIdRef", badgeRef) // Check for existing "First step badge"
                                    .get()
                                    .addOnCompleteListener(badgeTask -> {
                                        if (badgeTask.isSuccessful() && badgeTask.getResult() != null && badgeTask.getResult().isEmpty()) {
                                            // Student doesn't have the "First step badge", grant it
                                            grantFirstStepBadge(userId, badgeRef, userRef);
                                        } else {
                                            // Student already has the "First step badge"
                                            Log.d(TAG, "Student already has the 'First step badge'.");
                                        }
                                    });
                        } else {
                            // Student is already enrolled in multiple lessons or no lessons at all, no action needed
                            Log.d(TAG, "Student is already enrolled in multiple lessons or no lessons at all.");
                        }
                    } else {
                        Log.e(TAG, "Error checking enrollment in lessons: ", task.getException());
                    }
                });
    }

    private void grantFirstStepBadge(String userId, DocumentReference badgeRef, DocumentReference userRef) {
        // Create a new badge document to grant the "First step badge"
        Map<String, Object> badgeData = new HashMap<>();
        badgeData.put("userIdRef", userRef); // Use user reference
        badgeData.put("badgeIdRef", badgeRef); // Use badge reference
        badgeData.put("dateGranted", FieldValue.serverTimestamp());

        db.collection("user_badges")
                .add(badgeData)
                .addOnSuccessListener(documentReference -> {
                    showBadge1Dialog(badgeRef);
                    Log.d(TAG, "First step badge granted to user.");

                    Toast.makeText(getContext(), "You have received the 'First step badge'!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error granting 'First step badge': ", e);
                    Toast.makeText(getContext(), "Failed to grant 'First step badge'.", Toast.LENGTH_SHORT).show();
                });
    }
    private void showBadge1Dialog(DocumentReference badgeRef) {
        // Get the badge details from the total_badges collection using the badgeRef
        badgeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot badgeSnapshot = task.getResult();
                if (badgeSnapshot.exists()) {
                    String badgeName = badgeSnapshot.getString("badges_name");
                    String badgeDesc = badgeSnapshot.getString("badges_desc");

                    // Update the dialog with the badge information
                    final Dialog badgeDialog = new Dialog(getContext());
                    badgeDialog.setContentView(R.layout.dialog_badges);

                    // Set the title and description
                    TextView titleView = badgeDialog.findViewById(R.id.badges_dialog_title);
                    TextView messageView = badgeDialog.findViewById(R.id.badges_dialog_message);
                    ImageView badgeImageView = badgeDialog.findViewById(R.id.badges_image);  // Assume you add an ImageView in XML for icon

                    titleView.setText(badgeName);
                    messageView.setText(badgeDesc);

                    // You can load the appropriate badge icon here (example: set a default icon for now)
                    badgeImageView.setImageResource(R.drawable.ic_badges1); // You can replace this with a dynamic icon if needed

                    // Show the dialog
                    badgeDialog.show();

                    // Handle the collect button click
                    Button collectButton = badgeDialog.findViewById(R.id.collect_badges_button);
                    collectButton.setOnClickListener(v -> badgeDialog.dismiss());  // Close dialog on button click

                } else {
                    Log.d(TAG, "Badge not found in total_badges collection.");
                }
            } else {
                Log.e(TAG, "Error fetching badge details: ", task.getException());
            }
        });
    }




    private void checkAndGrantMilestoneMasterBadge() {
        String userId = mAuth.getCurrentUser().getUid();

        // References to the user and badge documents
        DocumentReference badgeRef = db.collection("total_badges").document("ShBef8enC80FJMKhodvt"); // MilestoneMaster badge ID
        DocumentReference userRef = db.collection("users").document(userId);

        // Step 1: Query the chapter_progress collection to count completed chapters
        db.collection("chapter_progress")
                .whereEqualTo("userIdRef", userRef) // Reference to the user document
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        long completedChaptersCount = task.getResult().size();

                        // Step 2: Check if the user has completed at least 5 chapters
                        if (completedChaptersCount == 5) {
                            // Step 3: Verify if the badge is already granted
                            db.collection("user_badges")
                                    .whereEqualTo("userIdRef", userRef)
                                    .whereEqualTo("badgeIdRef", badgeRef)
                                    .get()
                                    .addOnCompleteListener(badgeTask -> {
                                        if (badgeTask.isSuccessful()) {
                                            if (badgeTask.getResult() != null && badgeTask.getResult().isEmpty()) {
                                                // Badge not granted, proceed to grant it
                                                grantMilestoneMasterBadge(userRef, badgeRef);
                                            } else {
                                                // Badge already granted
                                                Log.d(TAG, "Student already has the 'MilestoneMaster' badge.");
                                            }
                                        } else {
                                            // Handle errors in badge query
                                            Log.e(TAG, "Error checking badge: ", badgeTask.getException());
                                        }
                                    });
                        } else {
                            // Student has not completed enough chapters
                            Log.d(TAG, "Student has not completed enough chapters for 'MilestoneMaster' badge.");
                        }
                    } else {
                        // Handle errors in chapter progress query
                        Log.e(TAG, "Error getting chapter progress: ", task.getException());
                    }
                });
    }

    private void grantMilestoneMasterBadge(DocumentReference userRef, DocumentReference badgeRef) {
        // Create a new badge document to grant the 'MilestoneMaster' badge
        Map<String, Object> badgeData = new HashMap<>();
        badgeData.put("userIdRef", userRef); // Use user reference
        badgeData.put("badgeIdRef", badgeRef); // Use badge reference
        badgeData.put("dateGranted", FieldValue.serverTimestamp()); // Timestamp when the badge is granted

        db.collection("user_badges")
                .add(badgeData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "MilestoneMaster badge granted to user.");
                    // Optionally, display a dialog or notification here
                    showBadge2Dialog(badgeRef);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error granting 'MilestoneMaster' badge: ", e);
                    Toast.makeText(getContext(), "Failed to grant 'MilestoneMaster' badge.", Toast.LENGTH_SHORT).show();
                });
    }
    private void showBadge2Dialog(DocumentReference badgeRef) {
        // Get the badge details from the total_badges collection using the badgeRef
        badgeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot badgeSnapshot = task.getResult();
                if (badgeSnapshot.exists()) {
                    String badgeName = badgeSnapshot.getString("badges_name");
                    String badgeDesc = badgeSnapshot.getString("badges_desc");

                    // Update the dialog with the badge information
                    final Dialog badgeDialog = new Dialog(getContext());
                    badgeDialog.setContentView(R.layout.dialog_badges);

                    // Set the title and description
                    TextView titleView = badgeDialog.findViewById(R.id.badges_dialog_title);
                    TextView messageView = badgeDialog.findViewById(R.id.badges_dialog_message);
                    ImageView badgeImageView = badgeDialog.findViewById(R.id.badges_image);  // Assume you add an ImageView in XML for icon

                    titleView.setText(badgeName);
                    messageView.setText(badgeDesc);

                    // You can load the appropriate badge icon here (example: set a default icon for now)
                    badgeImageView.setImageResource(R.drawable.ic_badges2); // You can replace this with a dynamic icon if needed

                    // Show the dialog
                    badgeDialog.show();

                    // Handle the collect button click
                    Button collectButton = badgeDialog.findViewById(R.id.collect_badges_button);
                    collectButton.setOnClickListener(v -> badgeDialog.dismiss());  // Close dialog on button click

                } else {
                    Log.d(TAG, "Badge not found in total_badges collection.");
                }
            } else {
                Log.e(TAG, "Error fetching badge details: ", task.getException());
            }
        });
    }
    private void checkAndGrantQuizWhizBadge() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.e("QuizFragment", "Student not logged in. Cannot grant badge.");
            return; // Exit early if the user is not logged in
        }

        String userId = currentUser.getUid();

        // References to the user and badge documents
        DocumentReference badgeRef = db.collection("total_badges").document("acNltPeVwqD3gzcX5A3Y"); // MilestoneMaster badge ID
        DocumentReference userRef = db.collection("users").document(userId);

        // Step 1: Query the quiz_progress collection to count completed quiz
        db.collection("quiz_progress")
                .whereEqualTo("userIdRef", userRef) // Reference to the user document
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        long completedQuizCount = task.getResult().size();

                        // Step 2: Check if the user has completed at least 5 quiz
                        if (completedQuizCount == 1) {
                            // Step 3: Verify if the badge is already granted
                            db.collection("user_badges")
                                    .whereEqualTo("userIdRef", userRef)
                                    .whereEqualTo("badgeIdRef", badgeRef)
                                    .get()
                                    .addOnCompleteListener(badgeTask -> {
                                        if (badgeTask.isSuccessful()) {
                                            if (badgeTask.getResult() != null && badgeTask.getResult().isEmpty()) {
                                                // Badge not granted, proceed to grant it
                                                grantQuizWhizBadge(userRef, badgeRef);
                                            } else {
                                                // Badge already granted
                                                Log.d(TAG, "Student already has the 'QuizWhiz badge.");
                                            }
                                        } else {
                                            // Handle errors in badge query
                                            Log.e(TAG, "Error checking badge: ", badgeTask.getException());
                                        }
                                    });
                        } else {
                            // Student has not completed enough quiz
                            Log.d(TAG, "Student has not completed enough chapters for 'QuizWhiz' badge.");
                        }
                    } else {
                        // Handle errors in quiz progress query
                        Log.e(TAG, "Error getting quiz progress: ", task.getException());
                    }
                });
    }

    private void grantQuizWhizBadge(DocumentReference userRef, DocumentReference badgeRef) {
        // Create a new badge document to grant the 'QuizWhiz' badge
        Map<String, Object> badgeData = new HashMap<>();
        badgeData.put("userIdRef", userRef); // Use user reference
        badgeData.put("badgeIdRef", badgeRef); // Use badge reference
        badgeData.put("dateGranted", FieldValue.serverTimestamp()); // Timestamp when the badge is granted

        db.collection("user_badges")
                .add(badgeData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "QuizWhiz badge granted to user.");
                    // Optionally, display a dialog or notification here
                    showBadge3Dialog(badgeRef);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error granting 'QuizWhiz' badge: ", e);
                    Toast.makeText(getContext(), "Failed to grant 'QuizWhiz' badge.", Toast.LENGTH_SHORT).show();
                });
    }
    private void showBadge3Dialog(DocumentReference badgeRef) {
        // Get the badge details from the total_badges collection using the badgeRef
        badgeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot badgeSnapshot = task.getResult();
                if (badgeSnapshot.exists()) {
                    String badgeName = badgeSnapshot.getString("badges_name");
                    String badgeDesc = badgeSnapshot.getString("badges_desc");

                    // Update the dialog with the badge information
                    final Dialog badgeDialog = new Dialog(getContext());
                    badgeDialog.setContentView(R.layout.dialog_badges);

                    // Set the title and description
                    TextView titleView = badgeDialog.findViewById(R.id.badges_dialog_title);
                    TextView messageView = badgeDialog.findViewById(R.id.badges_dialog_message);
                    ImageView badgeImageView = badgeDialog.findViewById(R.id.badges_image);  // Assume you add an ImageView in XML for icon

                    titleView.setText(badgeName);
                    messageView.setText(badgeDesc);

                    // You can load the appropriate badge icon here (example: set a default icon for now)
                    badgeImageView.setImageResource(R.drawable.ic_badges3); // You can replace this with a dynamic icon if needed

                    // Show the dialog
                    badgeDialog.show();

                    // Handle the collect button click
                    Button collectButton = badgeDialog.findViewById(R.id.collect_badges_button);
                    collectButton.setOnClickListener(v -> badgeDialog.dismiss());  // Close dialog on button click

                } else {
                    Log.d(TAG, "Badge not found in total_badges collection.");
                }
            } else {
                Log.e(TAG, "Error fetching badge details: ", task.getException());
            }
        });
    }






    private void loadLessonData(String lessonId) {
        db.collection("total_lesson")
                .document(lessonId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        tvLessonTitle.setText(document.getString("title"));
                        tvLessonRatingText.setText(document.getString("rating"));
                        String pattern = document.getString("pattern");
                        loadLessonImage(Objects.requireNonNull(document.getString("title")));
                        loadContentInPattern(lessonId, pattern);
                    } else {
                        Log.e(TAG, "Error fetching lesson", task.getException());
                        Toast.makeText(getContext(), "Failed to load lesson data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadLessonImage(String title) {
        switch (title) {
            case "GitHub":
                lessonImageView.setImageResource(R.drawable.ic_github);
                break;
            case "Java":
                lessonImageView.setImageResource(R.drawable.ic_java);
                break;
            case "HTML & CSS":
                lessonImageView.setImageResource(R.drawable.ic_html);
                break;
            case "Python":
                lessonImageView.setImageResource(R.drawable.ic_python);
                break;
            case "UI UX Design":
                lessonImageView.setImageResource(R.drawable.ic_uiux);
                break;
            default:
                lessonImageView.setImageResource(R.drawable.ic_avatar);
        }
    }

    private void loadContentInPattern(String lessonId, String pattern) {
        DocumentReference lessonRef = db.collection("total_lesson").document(lessonId);

        List<Chapter> chapters = new ArrayList<>();
        List<Quiz> quizzes = new ArrayList<>();

        // Counter to track completed tasks
        AtomicInteger tasksCompleted = new AtomicInteger(0);
        int totalTasks = 2; // Number of tasks to complete (chapters + quizzes)

        // Define a common callback for when data is ready
        Runnable onDataReady = () -> {
            if (tasksCompleted.incrementAndGet() == totalTasks) {
                arrangeContentInPattern(chapters, quizzes, pattern);
            }
        };

        // Fetch chapters
        db.collection("chapters")
                .whereEqualTo("ref", lessonRef)
                .orderBy("groupId")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            chapters.add(Chapter.fromSnapshot(doc));
                        }
                    } else {
                        Log.e(TAG, "Error fetching chapters", task.getException());
                    }
                    onDataReady.run(); // Notify that chapters are ready
                });

        // Fetch quizzes
        db.collection("quiz")
                .whereEqualTo("ref", lessonRef)
                .orderBy("groupId")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            quizzes.add(Quiz.fromSnapshot(doc));
                        }
                    } else {
                        Log.e(TAG, "Error fetching quizzes", task.getException());
                    }
                    onDataReady.run(); // Notify that quizzes are ready
                });
    }


    private void arrangeContentInPattern(List<Chapter> chapters, List<Quiz> quizzes, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            Log.d("Pattern", "Pattern is null or empty.");
            return;
        }

        // Clear the existing contentList to avoid duplicates
        contentList.clear();

        int chapterIndex = 0, quizIndex = 0;
        int totalItems = pattern.length();  // Total items based on pattern length
        int completedItems = 0;

        // Log size of chapters and quizzes lists
        Log.d("Data", "Chapters size: " + chapters.size() + ", Quizzes size: " + quizzes.size());

        // Track the completion states for all chapters and quizzes
        List<Task<QuerySnapshot>> completionTasks = new ArrayList<>();

        // Loop through pattern and process chapters/quizzes
        for (char type : pattern.toCharArray()) {
            if (type == 'C' && chapterIndex < chapters.size()) {
                Chapter chapter = chapters.get(chapterIndex++);
                checkChapterCompletion(chapter, completionTasks);  // Check if this chapter is completed
                contentList.add(chapter);
            } else if (type == 'Q' && quizIndex < quizzes.size()) {
                Quiz quiz = quizzes.get(quizIndex++);
                checkQuizCompletion(quiz, completionTasks);  // Check if this quiz is completed
                contentList.add(quiz);
            }
        }

        // Use Tasks.whenAll() to wait for all completion tasks to finish
        Tasks.whenAll(completionTasks)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Calculate progress once all tasks are completed
                        calculateProgress(totalItems);
                    } else {
                        Log.e("Error", "Failed to complete one or more tasks");
                    }
                });

        chapterAdapter.notifyDataSetChanged();
    }

    private void checkChapterCompletion(Chapter chapter, List<Task<QuerySnapshot>> completionTasks) {
        String userId = mAuth.getCurrentUser().getUid();
        Task<QuerySnapshot> task = db.collection("chapter_progress")
                .whereEqualTo("chapterIdRef", db.collection("chapters").document(chapter.getId()))
                .whereEqualTo("userIdRef", db.collection("users").document(userId))
                .get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful() && !task1.getResult().isEmpty()) {
                        completionStateMap.put(chapter.getId(), true); // Chapter completed
                    } else {
                        completionStateMap.put(chapter.getId(), false); // Chapter not completed
                    }
                    // Log the completion state
                    Log.d(TAG, "Chapter Completion State: " + chapter.getId() + " " + completionStateMap.get(chapter.getId()));
                });
        completionTasks.add(task); // Add task to the list of tasks to track
    }

    private void checkQuizCompletion(Quiz quiz, List<Task<QuerySnapshot>> completionTasks) {
        String userId = mAuth.getCurrentUser().getUid();
        Task<QuerySnapshot> task = db.collection("quiz_progress")
                .whereEqualTo("quizIdRef", db.collection("quiz").document(quiz.getId()))
                .whereEqualTo("userIdRef", db.collection("users").document(userId))
                .get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful() && !task1.getResult().isEmpty()) {
                        completionStateMap.put(quiz.getId(), true); // Quiz completed
                    } else {
                        completionStateMap.put(quiz.getId(), false); // Quiz not completed
                    }
                    // Log the completion state
                    Log.d(TAG, "Quiz Completion State: " + quiz.getId() + " " + completionStateMap.get(quiz.getId()));
                });
        completionTasks.add(task); // Add task to the list of tasks to track
    }

    private void calculateProgress(int totalItems) {
        // Calculate completed items
        int completedItems = 0;
        for (Boolean state : completionStateMap.values()) {
            if (state) {
                completedItems++;
            }
        }

        // Calculate progress as a percentage
        String progressString = totalItems > 0
                ? String.valueOf(completedItems * 100 / totalItems)  // Save as percentage string, e.g., "50"
                : "0";  // If no items, progress is 0%

        // Log progress string
        Log.d("Progress", "Progress String: " + progressString);

        // Now update the progress in Firestore after checking all completion states
        updateProgressInFirestore(progressString);
    }

    private void updateProgressInFirestore(String progressString) {
        String userId = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "Progress String: " + progressString);
        Log.d(TAG, "UID: " + userId);
        Log.d(TAG, "Lesson Id: " + lessonId);

        // Create references to the user and lesson documents
        DocumentReference userRef = db.collection("users").document(userId);
        DocumentReference lessonRef = db.collection("total_lesson").document(lessonId);

        // Query to find the document where both userId (userRef) and lessonId (lessonRef) match
        db.collection("current_lesson")
                .whereEqualTo("userId", userRef)
                .whereEqualTo("lessonId", lessonRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Assuming only one matching document is found
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        Log.d(TAG, "Current Lesson Document Id: " + document.getId());
                        db.collection("current_lesson")
                                .document(document.getId())  // Get the document ID from the result
                                .update("progress", progressString)  // Update only the progress field
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Progress updated successfully");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating progress", e);
                                });
                    } else {
                        Log.e(TAG, "No matching progress document found for the user and lesson.");
                    }
                });
    }



    private void enrollInLesson(String userId, String lessonId) {
        CollectionReference currentLessonRef = db.collection("current_lesson");

        DocumentReference userIdRef = db.collection("users").document(userId);
        DocumentReference lessonIdRef = db.collection("total_lesson").document(lessonId);
        Map<String, Object> currentLessonData = new HashMap<>();
        currentLessonData.put("userId", userIdRef);
        currentLessonData.put("lessonId", lessonIdRef);
        currentLessonData.put("progress", "0");

        currentLessonRef.add(currentLessonData)
                .addOnSuccessListener(documentReference -> {
                    // Update isEnrolled status immediately
                    isEnrolled = true;
                    btnEnroll.setVisibility(View.GONE);
                    CLbtn.setVisibility(View.GONE);

                    Toast.makeText(getContext(), "Successfully enrolled in the lesson!", Toast.LENGTH_SHORT).show();

                    // Notify the adapter to update
                    chapterAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to enroll in the lesson.", Toast.LENGTH_SHORT).show()
                );
    }


}


