package com.example.android.popularmovies;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieReviewFragment extends Fragment {
    /** Tag for the log messages */
    private static final String LOG_TAG = MovieReviewFragment.class.getSimpleName();

    static final String DETAIL_MOVIE_ID = "MOVIE_ID";
    private long mMovieId;

    private ListView mListView;

    /** Adapter for the list of movie reviews */
    private MovieReviewAdapter mAdapter;

    public MovieReviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovieId = arguments.getLong(MovieReviewFragment.DETAIL_MOVIE_ID);
        }

        View rootView = inflater.inflate(R.layout.fragment_movie_review_list, container, false);

        mListView = (ListView) rootView.findViewById(R.id.movie_review_listview);

        // Create a new {@link MovieReviewFragment} of reviews
        mAdapter = new MovieReviewAdapter( getActivity(), new ArrayList<Review>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        mListView.setAdapter(mAdapter);

        getReviews();

        return rootView;
    }

    private void getReviews() {
        if (Utility.isNetworkConnected(getActivity()) == true) {
            // Kick off an {@link AsyncTask} to perform the network request
            MovieReviewAsyncTask task = new MovieReviewAsyncTask();
            task.execute();
        }
    }

    /**
     * Update the screen to display information of all {@link Review} objects.
     */
    private void updateUi(ArrayList<Review> reviews) {

        // Clear the adapter of previous reviews data
        mAdapter.clear();

        // If there is a valid list of {@link Review}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (reviews != null && !reviews.isEmpty()) {
            mAdapter.addAll(reviews);
        }
    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread
     */
    private class MovieReviewAsyncTask extends AsyncTask<Void, Void, ArrayList<Review>> {

        @Override
        protected ArrayList<Review> doInBackground(Void... params) {
            // Create URL object
            URL url = QueryUtils.createUrl(String.valueOf(mMovieId), "reviews");

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = QueryUtils.makeHttpRequest(url);
                Log.d(LOG_TAG, jsonResponse);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error while retrieving review information");
            }

            // Extract relevant fields from the JSON response and create
            // a list of {@link Review} objects
            ArrayList<Review> reviews = QueryUtils.extractReviews(jsonResponse);

            // Return the {@link Review} object as the result fo the {@link MovieReviewAsyncTask}
            return reviews;
        }

        /**
         * Update the screen with the given reviews (which was the result of the
         * {@link MovieReviewAsyncTask}).
         */
        @Override
        protected void onPostExecute(ArrayList<Review> reviews) {
            updateUi(reviews);
        }
    }
}
