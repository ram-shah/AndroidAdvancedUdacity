package com.example.android.popularmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MovieTrailerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_trailer_list);

        if (savedInstanceState == null) {
            // Create the trailer fragment and add it to the activity
            // using a fragment transaction.
            long movieId = getIntent().getExtras().getLong("movieId");
            Bundle arguments = new Bundle();
            arguments.putLong(MovieTrailerFragment.DETAIL_MOVIE_ID, movieId);

            MovieTrailerFragment trailerFragment = new MovieTrailerFragment();
            trailerFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_trailer_container, trailerFragment)
                    .commit();
        }
    }
}
