package com.dellmau.edulink.models;

public class CertCard{
    private String date;
    private String user_name;
    private String course_name;

    public CertCard(String date, String user_name, String course_name) {
        this.date = date;
        this.user_name = user_name;
        this.course_name = course_name;
    }

    public String getDate() {
        return date;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getCourse_name() {
        return course_name;
    }
}

