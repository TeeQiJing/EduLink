package com.dellmau.edulink.models;

import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

public class Employer {
    private String username;
    private String email;
    private String password;
    private String registration_date;
    private String login_date;
    private String avatar = "";
    private String user_bio;
    private DocumentReference organization;


    // Default constructor required by Firestore for deserialization
    public Employer() {
    }

    // Constructor with parameters
    public Employer(String username, String email, String password, String registration_date, String login_date, String avatar, String user_bio, DocumentReference  organization) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.registration_date = registration_date;
        this.login_date = login_date;
        this.avatar = avatar;
        this.user_bio = user_bio;
        this.organization = organization;}

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRegistration_date() {
        return registration_date;
    }

    public void setRegistration_date(String registration_date) {
        this.registration_date = registration_date;
    }

    public String getLogin_date() {
        return login_date;
    }

    public void setLogin_date(String login_date) {
        this.login_date = login_date;
    }


    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar != null ? avatar : ""; // Prevent null assignment
    }

    public String getUser_bio() {
        return user_bio;
    }

    public void setUser_bio(String user_bio) {
        this.user_bio = user_bio;
    }

    public DocumentReference getOrganization() {
        return organization;
    }

    public void setOrganization(DocumentReference organization) {
        this.organization = organization;
    }

}
