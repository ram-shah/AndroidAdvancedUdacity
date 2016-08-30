package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by ramshah on 8/26/16.
 */
public class MovieTrailerFragment extends Fragment {
    /** Tag for the log messages */
    private static final String LOG_TAG = MovieTrailerFragment.class.getSimpleName();

    static final String DETAIL_MOVIE_ID = "MOVIE_ID";

    private static final String MOVIES_SHARE_HASHTAG = " #PopularMoviesApp";

    private ShareActionProvider mShareActionProvider;
    private String mShareMovieTrailerURL;

    private long mMovieId;

    private ListView mListView;

    /** Adapter for the list of movie trailers */
    private MovieTrailerAdapter mAdapter;

    public MovieTrailerFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovieId = arguments.getLong(MovieTrailerFragment.DETAIL_MOVIE_ID);
        }

        View rootView = inflater.inflate(R.layout.fragment_movie_trailer_list, container, false);

        mListView = (ListView) rootView.findViewById(R.id.movie_trailer_listview);

        // Create a new {@link MovieTrailerAdapter} of trailers
        mAdapter = new MovieTrailerAdapter( getActivity(), new ArrayList<Trailer>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String trailerKey = ((Trailer)adapterView.getItemAtPosition(position)).getKey();
                String trailerURL = "https://www.youtube.com/watch?v=" + trailerKey;
                Uri trailerUri = Uri.parse(trailerURL);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(trailerUri);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Couldn't call " + trailerUri.toString() + ", no receiving apps installed!");
                }
            }
        });

        getTrailers();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.moviedetailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onPostExecute happens before this, we can go ahead and set the share intent now.
        if (mAdapter != null) {
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        }
    }

    private Intent createShareTrailerIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareMovieTrailerURL + MOVIES_SHARE_HASHTAG);
        return shareIntent;
    }

    private void getTrailers() {
        if (Utility.isNetworkConnected(getActivity()) == true) {
            // Kick off an {@link AsyncTask} to perform the network request
            MovieTrailerAsyncTask task = new MovieTrailerAsyncTask();
            task.execute();
        }
    }

    /**
     * Update the screen to display information of all {@link Trailer} objects.
     */
    private void updateUi(ArrayList<Trailer> trailers) {

        // Clear the adapter of previous trailers data
        mAdapter.clear();

        // If there is a valid list of {@link Trailer}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (trailers != null && !trailers.isEmpty()) {
            mAdapter.addAll(trailers);

            // We still need this for the share intent
            mShareMovieTrailerURL = "https://www.youtube.com/watch?v=" + trailers.get(0).getKey();

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareTrailerIntent());
            }
        }
    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread
     */
    private class MovieTrailerAsyncTask extends AsyncTask<Void, Void, ArrayList<Trailer>> {

        @Override
        protected ArrayList<Trailer> doInBackground(Void... params) {
            // Create URL object
            URL url = QueryUtils.createUrl(String.valueOf(mMovieId), "videos");

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = QueryUtils.makeHttpRequest(url);
                Log.d(LOG_TAG, jsonResponse);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error while retrieving trailer information");
            }

            // Extract relevant fields from the JSON response and create
            // a list of {@link Trailer} objects
            ArrayList<Trailer> trailers = QueryUtils.extractTrailers(jsonResponse);

            // Return the {@link Trailer} object as the result fo the {@link MovieTrailerAsyncTask}
            return trailers;
        }

        /**
         * Update the screen with the given trailers (which was the result of the
         * {@link MovieTrailerAsyncTask}).
         */
        @Override
        protected void onPostExecute(ArrayList<Trailer> trailers) {
            updateUi(trailers);
        }
    }
}
