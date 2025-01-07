package com.dellmau.edulink.models;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

public class Quiz {

    private String title;
    private String Id;
    private DocumentReference ref; // Reference to parent lesson

    // Static method to create a Quiz object from Firestore snapshot
    public static Quiz fromSnapshot(DocumentSnapshot snapshot) {
        Quiz quiz = new Quiz();

        // Populate fields
        quiz.setTitle(snapshot.getString("title"));
        quiz.setId(snapshot.getId()); // Convert Long to int
        quiz.setRef(snapshot.getDocumentReference("ref")); // Get reference to the parent lesson

        return quiz;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return Id;
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    public DocumentReference getRef() {
        return ref;
    }

    public void setRef(DocumentReference ref) {
        this.ref = ref;
    }

}
