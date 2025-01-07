package com.dellmau.edulink.models;

public class Lesson {
    private String id;
    private String title;
    private double rating;
    private String pattern;
    private String level;
    private String image;

    public Lesson() {}

    public Lesson(String id, String title, double rating, String pattern, String level, String image) {
        this.id = id;
        this.title = title;
        this.rating = rating;
        this.pattern = pattern;
        this.level = level;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public double getRating() {
        return rating;
    }

    public String getPattern() {
        return pattern;
    }

    public String getLevel() {
        return level;
    }

    public String getImage() {
        return image;
    }
}
