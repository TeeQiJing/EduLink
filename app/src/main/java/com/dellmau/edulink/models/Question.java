package com.dellmau.edulink.models;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Question {

    private String title;           // Question title
    private List<String> options;   // List of options (as strings)
    private String answer;          // Correct answer (stored as option text)
    private String image;           // Optional image (URL or path)
    private int score;              // Score for this question
    private String Id;
    private DocumentReference ref; // Reference to parent quiz

    // Static method to create a Question object from Firestore snapshot
    public static Question fromSnapshot(DocumentSnapshot snapshot) {
        Question question = new Question();

        // Populate fields
        question.setTitle(snapshot.getString("title"));
        question.setOptions((List<String>) snapshot.get("options"));
        question.setAnswer(snapshot.getString("answer"));
        question.setImage(snapshot.getString("image"));
        question.setId(snapshot.getId()); // Convert Long to int
        question.setScore(snapshot.getLong("score").intValue()); // Convert Long to int
        question.setRef(snapshot.getDocumentReference("ref"));

        return question;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public DocumentReference getRef() {
        return ref;
    }

    public void setRef(DocumentReference ref) {
        this.ref = ref;
    }
    public String getId() {
        return Id;
    }
    public void setId(String Id) {
        this.Id = Id;
    }

    public String getImageUrl() {
        try {
            JSONArray jsonArray = new JSONArray(image); // Assuming `image` is a JSON string
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if ("image".equals(jsonObject.getString("type"))) {
                    return jsonObject.getString("value");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null; // Return null if no valid URL is found
    }
}
