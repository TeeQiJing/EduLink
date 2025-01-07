//package com.practical.edumasters.models;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class User {
//    private String username;
//    private String email;
//    private String password;
//    private String registration_date;
//    private String login_date;
//    private int xp;
//    private String avatar;
//    private String user_bio;
//    private List<String> courses;  // New list for courses
//
//    // Default constructor required by Firestore for deserialization
//    public User() {
//        // Initialize courses list to avoid null pointer exceptions
//        this.courses = new ArrayList<>();
//    }
//
//    // Constructor with parameters
//    public User(String username, String email, String password, String registration_date, String login_date, int xp, String avatar, String user_bio) {
//        this.username = username;
//        this.email = email;
//        this.password = password;
//        this.registration_date = registration_date;
//        this.login_date = login_date;
//        this.xp = xp;
//        this.avatar = avatar;
//        this.user_bio = user_bio;
//        this.courses = new ArrayList<>();  // Initialize the list to be empty by default
//    }
//
//    // Getters and setters
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public String getRegistration_date() {
//        return registration_date;
//    }
//
//    public void setRegistration_date(String registration_date) {
//        this.registration_date = registration_date;
//    }
//
//    public String getLogin_date() {
//        return login_date;
//    }
//
//    public void setLogin_date(String login_date) {
//        this.login_date = login_date;
//    }
//
//    public int getXp() {
//        return xp;
//    }
//
//    public void setXp(int xp) {
//        this.xp = xp;
//    }
//
//    public String getAvatar() {
//        return avatar;
//    }
//
//    public void setAvatar(String avatar) {
//        this.avatar = avatar;
//    }
//
//    public String getUser_bio() {
//        return user_bio;
//    }
//
//    public void setUser_bio(String user_bio) {
//        this.user_bio = user_bio;
//    }
//
//    public List<String> getCourses() {
//        return courses;
//    }
//
//    public void setCourses(List<String> courses) {
//        this.courses = courses;
//    }
//}
package com.dellmau.edulink.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String email;
    private String password;
    private String registration_date;
    private String login_date;
    private int xp;
    private String avatar = "";
    private String user_bio;
    private List<String> courses;  // New list for courses

    // Default constructor required by Firestore for deserialization
    public User() {
        // Initialize courses list to avoid null pointer exceptions
        this.courses = new ArrayList<>();
    }

    // Constructor with parameters
    public User(String username, String email, String password, String registration_date, String login_date, int xp, String avatar, String user_bio) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.registration_date = registration_date;
        this.login_date = login_date;
        this.xp = xp;
        this.avatar = avatar;
        this.user_bio = user_bio;
        this.courses = new ArrayList<>();  // Initialize the list to be empty by default
    }

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

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
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

    public List<String> getCourses() {
        return courses;
    }

    public void setCourses(List<String> courses) {
        this.courses = courses;
    }
}
