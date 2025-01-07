package com.dellmau.edulink.models;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class Chapter {

    private DocumentReference ref;
    private String title;
    private String id;
    private List<Map<String, Object>> content; // List to hold content blocks

    // Static method to create a Chapter object from Firestore snapshot
    public static Chapter fromSnapshot(DocumentSnapshot snapshot) {
        Chapter chapter = new Chapter();

        // Parse the content field
        String contentJson = snapshot.getString("content");
        if (contentJson != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> contentList = gson.fromJson(contentJson, listType);
            chapter.setContent(contentList);
        }

        // Populate other fields
        chapter.setTitle(snapshot.getString("title"));
        chapter.setId(snapshot.getId());
        chapter.setRef(snapshot.getDocumentReference("ref"));

        return chapter;
    }

    // Getters and setters
    public DocumentReference getRef() {
        return ref;
    }

    public void setRef(DocumentReference ref) {
        this.ref = ref;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Map<String, Object>> getContent() {
        return content;
    }

    public void setContent(List<Map<String, Object>> content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
