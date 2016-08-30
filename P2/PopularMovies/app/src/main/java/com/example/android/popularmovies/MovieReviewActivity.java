package com.example.android.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by ramshah on 8/29/16.
 */
public class MovieReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_review_list);

        if (savedInstanceState == null) {
            // Create the review fragment and add it to the activity
            // using a fragment transaction.
            long movieId = getIntent().getExtras().getLong("movieId");
            Bundle arguments = new Bundle();
            arguments.putLong(MovieReviewFragment.DETAIL_MOVIE_ID, movieId);

            MovieReviewFragment reviewFragment = new MovieReviewFragment();
            reviewFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_review_container, reviewFragment)
                    .commit();
        }
    }
}

