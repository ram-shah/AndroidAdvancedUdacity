package com.example.android.popularmovies;

/**
 * Created by ramshah on 8/26/16.
 */
public class Review {
    private String mAuthor;
    private String mReview;

    public Review(String author, String review) {
        this.mAuthor = author;
        this.mReview = review;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getReview() {
        return mReview;
    }
}
