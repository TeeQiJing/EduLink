package com.dellmau.edulink.models;

import com.google.firebase.firestore.DocumentReference;

import org.w3c.dom.Document;

public class CurrentLessonCard {
    private DocumentReference lessonId;  // Store as DocumentReference
    private String progress;
    private DocumentReference userId;  // Store as DocumentReference
    private DocumentReference company;

    public DocumentReference getCompany() {
        return company;
    }

    public void setCompany(DocumentReference company) {
        this.company = company;
    }

    // Getters and Setters
    public DocumentReference getLessonId() {
        return lessonId;
    }

    public void setLessonId(DocumentReference lessonId) {
        this.lessonId = lessonId;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public DocumentReference getUserId() {
        return userId;
    }

    public void setUserId(DocumentReference userId) {
        this.userId = userId;
    }
}
