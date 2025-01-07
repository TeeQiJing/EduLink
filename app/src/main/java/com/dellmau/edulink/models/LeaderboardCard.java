package com.dellmau.edulink.models;

import android.graphics.Bitmap;

public class LeaderboardCard implements Comparable<LeaderboardCard> {
    String testing_name;
    String testing_mark;
    Bitmap testing_avatar;

    public LeaderboardCard(String testing_name, String testing_mark, Bitmap testing_avatar) {
        this.testing_name = testing_name;
        this.testing_mark = testing_mark;
        this.testing_avatar=testing_avatar;
    }

    public String getTesting_name() {
        return testing_name;
    }

    public String getTesting_mark() {
        return testing_mark;
    }

    public Bitmap getTesting_avatar() { return testing_avatar; }

    @Override
    public int compareTo(LeaderboardCard other) {
        // For sorting by mark (assuming it's a numeric value)
        int thisMark = Integer.parseInt(this.testing_mark);
        int otherMark = Integer.parseInt(other.testing_mark);

        // For descending order, return otherMark - thisMark
        return Integer.compare(otherMark, thisMark);  // Use this if you want descending order
    }
}
