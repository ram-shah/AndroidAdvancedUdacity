package com.example.android.popularmovies;

/**
 * Created by ramshah on 8/11/16.
 */
public class Movie {
    private String mMoviePosterImage;
    private long mMovieId;
    private String mTitle;
    private String mPlot;
    private String mUserRating;
    private String mReleaseDate;

    private static final String BASE_URL_POSTER_IMAGE = "http://image.tmdb.org/t/p/w185";

    public Movie(String moviePosterImage, long movieId) {
        this.mMoviePosterImage = moviePosterImage;
        this.mMovieId = movieId;
    }

    public Movie(String moviePosterImage, long movieId, String title, String plot,
                 String userRating, String releaseDate) {
        this.mMoviePosterImage = moviePosterImage;
        this.mMovieId = movieId;
        this.mTitle = title;
        this.mPlot = plot;
        this.mUserRating = userRating;
        this.mReleaseDate = releaseDate;
    }

    public String getMoviePosterImage() {
        return BASE_URL_POSTER_IMAGE + mMoviePosterImage;
    }

    public long getMovieId() {
        return mMovieId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getPlot() {
        return mPlot;
    }

    public String getUserRating() {
        return mUserRating;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }
}
