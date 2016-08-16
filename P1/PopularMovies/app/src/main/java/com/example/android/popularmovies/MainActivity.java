package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    /** Tag for the log messages */
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /** Adapter for the list of movies */
    private MovieAdapter mAdapter;

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find a reference to the {@link GridView} in the layout
        GridView movieGridView = (GridView) findViewById(R.id.movie_gridview);
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        movieGridView.setEmptyView(mEmptyStateTextView);

        // Find a reference to the progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.loading_spinner);

        // Create a new {@link MovieAdapter} of movies
        mAdapter = new MovieAdapter( this, new ArrayList<Movie>());

        // Set the adapter on the {@link GridView}
        // so the list can be populated in the user interface
        movieGridView.setAdapter(mAdapter);

        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent movieDetailIntent = new Intent(MainActivity.this, MovieDetailActivity.class);
                Movie currentMovie = (Movie) adapterView.getItemAtPosition(position);
                movieDetailIntent.putExtra("movieId", currentMovie.getMovieId());
                startActivity(movieDetailIntent);
            }
        });
    }

    @Override
    public void onStart() {
        updateMovies();
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateMovies() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sortBy = prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_most_popular));
        String sortOrder;
        if (sortBy.compareToIgnoreCase(getString(R.string.pref_sort_highest_rated)) == 0) {
            setTitle(R.string.title_activity_main_top_rated);
            sortOrder = "top_rated";
        } else {
            setTitle(R.string.title_activity_main_popular);
            sortOrder = "popular";
        }

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // Kick off an {@link AsyncTask} to perform the network request
            mProgressBar.setVisibility(View.VISIBLE);
            MovieAsyncTask task = new MovieAsyncTask();
            task.execute(sortOrder);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.no_internet);
        }
    }

    /**
     * Update the screen to display information of all {@link Movie} objects.
     */
    private void updateUi(ArrayList<Movie> movies) {
        // Set empty state text to display "No movies found."
        mEmptyStateTextView.setText(R.string.no_movies);

        // Clear the adapter of previous movies data
        mAdapter.clear();

        mProgressBar.setVisibility(View.GONE);

        // If there is a valid list of {@link Movie}s, then add them to the adapter's
        // data set. This will trigger the GridView to update.
        if (movies != null && !movies.isEmpty()) {
            mAdapter.addAll(movies);
        }
    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread
     */
    private class MovieAsyncTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
            // Create URL object
            URL url = createUrl(params[0]);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = QueryUtils.makeHttpRequest(url);
                Log.d(LOG_TAG, jsonResponse);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error while retrieving movie information");
            }

            // Extract relevant fields from the JSON response and create a list of {@link Movie} objects
            ArrayList<Movie> movies = QueryUtils.extractMovies(jsonResponse);

            // Return the {@link Movie} object as the result fo the {@link MovieAsyncTask}
            return movies;
        }

        /**
         * Update the screen with the given movie (which was the result of the
         * {@link MovieAsyncTask}).
         */
        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            updateUi(movies);
        }

        /**
         * Returns new URL object
         */
        private URL createUrl(String sortBy) {
            URL url = null;
            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(sortBy)
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
