package com.dellmau.edulink.models;

import com.google.firebase.firestore.DocumentReference;

public class CurrentLessonCard {
    private DocumentReference lessonId;  // Store as DocumentReference
    private String progress;
    private DocumentReference userId;  // Store as DocumentReference

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
