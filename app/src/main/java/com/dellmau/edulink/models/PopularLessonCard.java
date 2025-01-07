package com.dellmau.edulink.models;

public class PopularLessonCard {
    int image;
    String level;
    String title;
    String ratings;

    public PopularLessonCard() {}

    public PopularLessonCard(int image, String level, String title, String ratings) {
        this.image = image;
        this.level = level;
        this.title = title;
        this.ratings = ratings;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRatings() {
        return ratings;
    }

    public void setRatings(String ratings) {
        this.ratings = ratings;
    }
}
