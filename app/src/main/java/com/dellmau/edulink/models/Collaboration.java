package com.dellmau.edulink.models;

import com.google.firebase.firestore.DocumentReference;

public class Collaboration {
    DocumentReference company;
    DocumentReference university;

    public DocumentReference getCompany() {
        return company;
    }

    public void setCompany(DocumentReference company) {
        this.company = company;
    }

    public DocumentReference getUniversity() {
        return university;
    }

    public void setUniversity(DocumentReference university) {
        this.university = university;
    }

    public Collaboration(DocumentReference company, DocumentReference university) {
        this.company = company;
        this.university = university;
    }
}
