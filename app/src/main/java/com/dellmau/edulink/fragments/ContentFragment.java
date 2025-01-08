package com.dellmau.edulink.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dellmau.edulink.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ContentFragment extends Fragment {
    private static final String TAG = "ContentFragment";
    private FirebaseFirestore db;
    private LinearLayout contentContainer;
    private String chapterId;
    private DocumentReference userIdRef;  // Firebase reference for user ID
    private DocumentReference chapterIdRef;  // Firebase reference for chapter ID
    private boolean progressRecorded = false;
    SharedPreferences sharedPreferences;
    String user_role;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(com.dellmau.edulink.R.layout.fragment_content, container, false);
        contentContainer = view.findViewById(R.id.contentContainer);
        sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        user_role = sharedPreferences.getString("user_role", "");

        db = FirebaseFirestore.getInstance();

        chapterId = getArguments() != null ? getArguments().getString("chapterId") : null;
        String chapterTitle = getArguments() != null ? getArguments().getString("chapterTitle") : null;

        // Get the current user's reference from Firebase Authentication (assuming the user is authenticated)
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userIdRef = db.collection(user_role.toLowerCase()).document(mAuth.getCurrentUser().getUid()); // Use real current user ID

        if (chapterId != null && !chapterId.isEmpty()) {
            chapterIdRef = db.collection("chapters").document(chapterId);  // Set the chapter reference
            MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);
            String titleWithoutChapter = chapterTitle.replaceAll("^Chapter\\s\\d+\\s*-\\s*", "");
            topAppBar.setTitle(titleWithoutChapter);
            topAppBar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

            ScrollView scrollView = view.findViewById(R.id.scrollView);
            scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
                if (!progressRecorded && isScrollViewAtBottom(scrollView)) {
                    checkAndRecordChapterProgress();
                    progressRecorded = true;
                }
            });

            loadChapterContent(chapterId);
        } else {
            Toast.makeText(getContext(), "Chapter content is empty", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private boolean isScrollViewAtBottom(ScrollView scrollView) {
        View childView = scrollView.getChildAt(0);
        return childView != null && scrollView.getScrollY() >= (childView.getHeight() - scrollView.getHeight());
    }

    private void checkAndRecordChapterProgress() {
        // Check if progress already exists for the user and chapter
        db.collection("chapter_progress")
                .whereEqualTo("userIdRef", userIdRef)
                .whereEqualTo("chapterIdRef", chapterIdRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        // No existing progress, record the new progress
                        recordChapterProgress();
                    } else {
                        Log.d(TAG, "Progress already recorded for this chapter");
                    }
                });
    }

    private void recordChapterProgress() {
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("userIdRef", userIdRef);  // Use userIdRef as reference
        progressData.put("chapterIdRef", chapterIdRef);  // Use chapterIdRef as reference
        progressData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("chapter_progress").add(progressData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Progress recorded successfully");
            } else {
                Log.e(TAG, "Failed to record progress", task.getException());
            }
        });
    }

    private void loadChapterContent(String chapterId) {
        db.collection("chapters").document(chapterId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                String jsonContent = document.getString("content");

                if (jsonContent != null) {
                    try {
                        JSONArray contentArray = new JSONArray(jsonContent);
                        displayChapterContent(contentArray);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON content", e);
                        Toast.makeText(getContext(), "Failed to load content", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "No content available.");
                }
            } else {
                Log.e(TAG, "Error fetching chapter content", task.getException());
            }
        });
    }

    private void displayChapterContent(JSONArray contentArray) throws JSONException {
        for (int i = 0; i < contentArray.length(); i++) {
            JSONObject contentObject = contentArray.getJSONObject(i);
            String type = contentObject.getString("type");
            String value = contentObject.getString("value");
            String subtype = contentObject.optString("subtype", null);

            switch (type) {
                case "text":
                    addTextView(value, subtype, contentObject);
                    break;
                case "list":
                    JSONArray listItems = contentObject.getJSONArray("value");
                    List<String> items = new ArrayList<>();
                    for (int j = 0; j < listItems.length(); j++) {
                        items.add(listItems.getString(j));
                    }
                    addListView(items);
                    break;
                case "image":
                    addImageView(value);
                    break;
                case "video":
                    addVideoView(value);
                    break;
                case "code":
                    addCodeView(value);
                    break;
                default:
                    Log.e(TAG, "Unknown content type: " + type);
            }
        }

        // Add an empty container at the end to help trigger the bottom detection
        View spacerView = new View(getContext());
        spacerView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                200 // Spacer height to ensure smooth scrolling
        ));
        contentContainer.addView(spacerView);
    }

    private void addTextView(String text, String subtype, JSONObject contentObject) {
        TextView textView = new TextView(getContext());

        if ("heading".equals(subtype)) {
            int size = contentObject.optInt("size", 24); // Default size 24 for headings
            textView.setTextSize(size);
            textView.setPadding(16, 64, 16, 16); // Added extra padding before headings
            textView.setTextColor(getResources().getColor(android.R.color.black));
        } else if ("paragraph".equals(subtype)) {
            textView.setTextSize(16);
            textView.setPadding(16, 16, 16, 16);
            textView.setTextColor(getResources().getColor(android.R.color.black));
        }

        textView.setText(text);
        contentContainer.addView(textView);
    }

    private void addListView(List<String> items) {
        LinearLayout listLayout = new LinearLayout(getContext());
        listLayout.setOrientation(LinearLayout.VERTICAL);

        for (String item : items) {
            TextView itemView = new TextView(getContext());
            itemView.setText("‣ " + item); // Simple bullet point
            itemView.setPadding(16, 8, 16, 8);
            itemView.setTextSize(16);
            itemView.setTextColor(getResources().getColor(android.R.color.black));
            listLayout.addView(itemView);
        }
        contentContainer.addView(listLayout);
    }

    private void addImageView(String imageUrl) {
        ImageView imageView = new ImageView(getContext());
        Glide.with(getContext()).load(imageUrl).into(imageView);
        imageView.setPadding(16, 16, 16, 16);
        contentContainer.addView(imageView);
    }

    private void addVideoView(String videoUrl) {
        WebView webView = new WebView(getContext());

        // Enable JavaScript and other settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        // Extract the YouTube video ID from the URL
        String videoId = extractYouTubeVideoId(videoUrl);

        // Construct the YouTube embed URL
        if (videoId != null) {
            String embedUrl = "https://www.youtube.com/embed/" + videoId + "?autoplay=0&modestbranding=1&rel=0";

            // Load the embed URL in an iframe format
            String iframeHtml = "<html><body style='margin:0; padding:0;'>"
                    + "<iframe width='100%' height='100%' src='" + embedUrl + "' frameborder='0' "
                    + "allowfullscreen></iframe>"
                    + "</body></html>";
            webView.loadData(iframeHtml, "text/html", "utf-8");
        } else {
            Toast.makeText(getContext(), "Invalid video URL", Toast.LENGTH_SHORT).show();
        }

        // Set layout parameters for the WebView
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                600 // Set a fixed height for the video frame
        );
        webView.setLayoutParams(params);

        contentContainer.addView(webView);
    }

    private String extractYouTubeVideoId(String url) {
        String pattern = "^(?:https?:\\/\\/)?(?:www\\.|m\\.)?(?:youtube\\.com\\/.*v=|youtu\\.be\\/)([\\w-]{11})(?:\\S+)?$";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1); // Return the video ID
        }
        return null; // Return null if no match is found
    }

    private void addCodeView(String codeSnippet) {
        View codeView = LayoutInflater.from(getContext()).inflate(R.layout.item_code, contentContainer, false);
        TextView codeTextView = codeView.findViewById(R.id.codeTextView);

        codeTextView.setText(codeSnippet);
        codeTextView.setSelected(true);
        codeTextView.setHorizontallyScrolling(true);

        contentContainer.addView(codeView);
    }
}
