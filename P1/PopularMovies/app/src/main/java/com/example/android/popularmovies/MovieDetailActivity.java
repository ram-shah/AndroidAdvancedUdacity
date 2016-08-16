package com.example.android.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MovieDetailActivity extends AppCompatActivity {

    /** Tag for the log messages */
    private static final String LOG_TAG = MovieDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        long movieId = getIntent().getExtras().getLong("movieId");
        getMovieDetails(movieId);
    }

    private void getMovieDetails(long id) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // Kick off an {@link AsyncTask} to perform the network request
            MovieDetailAsyncTask task = new MovieDetailAsyncTask();
            task.execute(id);
        }
    }

    /**
     * Update the screen to display information of {@link Movie} details.
     */
    private void updateUi(Movie movie) {

        ImageView posterThumbnail = (ImageView) findViewById(R.id.movie_poster_thumbnail);
        Picasso.with(this).load(movie.getMoviePosterImage()).into(posterThumbnail);

        TextView title = (TextView) findViewById(R.id.title);
        title.setText(movie.getTitle());

        TextView plot = (TextView) findViewById(R.id.plot);
        plot.setText(movie.getPlot());

        TextView userRating = (TextView) findViewById(R.id.user_rating);
        userRating.setText(formatUserRating(movie.getUserRating()));

        TextView releaseDate = (TextView) findViewById(R.id.release_date);
        releaseDate.setText(formatReleaseDate(movie.getReleaseDate()));
    }

    private String formatUserRating(String userRating) {
        return userRating + "/10";
    }

    /**
     * Return the formatted date string (i.e. "Mar 3, 1984") from a Date object.
     */
    private String formatReleaseDate(String releaseDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date dateObject;
        try {
            dateObject = format.parse(releaseDate);
        }
        catch (ParseException pe) {
            Log.e(LOG_TAG, "Error while retrieving movie information");
            return releaseDate;
        }
        format = new SimpleDateFormat("LLL dd, yyyy");

        return format.format(dateObject);
    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread
     */
    private class MovieDetailAsyncTask extends AsyncTask<Long, Void, Movie> {

        @Override
        protected Movie doInBackground(Long... params) {
            // Create URL object
            String movieId = params[0].toString();
            URL url = createUrl(movieId);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = QueryUtils.makeHttpRequest(url);
                Log.d(LOG_TAG, jsonResponse);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error while retrieving movie information");
            }

            // Extract relevant fields from the JSON response and create a {@link Movie} object
            Movie movie = QueryUtils.extractMovieDetail(jsonResponse);

            // Return the {@link Movie} object as the result fo the {@link MovieDetailAsyncTask}
            return movie;
        }

        /**
         * Update the screen with the given movie (which was the result of the
         * {@link MovieDetailAsyncTask}).
         */
        @Override
        protected void onPostExecute(Movie movie) {
            updateUi(movie);
        }

        /**
         * Returns new URL object
         */
        private URL createUrl(String movieId) {
            URL url = null;
            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(movieId)
                        .appendQueryParameter("api_key", getString(R.string.moviedb_api_key));
                url = new URL(builder.build().toString());
            } catch (Exception exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }
    }
}
