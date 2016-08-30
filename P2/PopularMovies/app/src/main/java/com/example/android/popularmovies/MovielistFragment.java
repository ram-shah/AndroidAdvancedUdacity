package com.example.android.popularmovies;


import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieContract;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovielistFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Tag for the log messages */
    private static final String LOG_TAG = MovielistFragment.class.getSimpleName();

    /** Adapter for the list of movies */
    private MovieAdapter mAdapter;

    private GridView mGridView;

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;

    private ProgressBar mProgressBar;

    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";

    private static final int FAVORITE_MOVIELIST_LOADER = 0;

    // For the gridview we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] MOVIE_GRIDVIEW_COLUMNS = {
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_POSTER_IMAGE
    };

    // These indices are tied to MOVIE_GRIDVIEW_COLUMNS.  If MOVIE_GRIDVIEW_COLUMNS changes, these
    // must change.
    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_POSTER_IMAGE = 1;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * MovieDetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(long movieId);
    }

    public MovielistFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movielist, container, false);

        // Find a reference to the {@link GridView} in the layout
        mGridView = (GridView) rootView.findViewById(R.id.movie_gridview);
        mEmptyStateTextView = (TextView) rootView.findViewById(R.id.empty_view);
        mGridView.setEmptyView(mEmptyStateTextView);

        // Find a reference to the progress bar
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.loading_spinner);

        // Create a new {@link MovieAdapter} of movies
        mAdapter = new MovieAdapter( getActivity(), new ArrayList<Movie>());

        // Set the adapter on the {@link GridView}
        // so the list can be populated in the user interface
        mGridView.setAdapter(mAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie currentMovie = (Movie) adapterView.getItemAtPosition(position);
                ((Callback) getActivity()).onItemSelected(currentMovie.getMovieId());

                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The gridview probably hasn't even been populated yet.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Gridview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    public void onSortOrderChanged( ) {
        //check if favorite is selected...in that case just query the location database
        String sortBy = Utility.getSortPreference(getActivity());
        if (sortBy.compareToIgnoreCase(getString(R.string.pref_sort_favorite)) == 0) {
            // Let's keep this commented out code as this will provide a reference in the
            // future on how to query the contentProvider directly without using loaders
            /*ArrayList<Movie> movies = new ArrayList<>();

            // we'll query our contentProvider to get list of all the movies in DB
            Cursor cursor = getActivity().getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI,
                    MOVIE_GRIDVIEW_COLUMNS,
                    null,
                    null,
                    null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                movies.add(new Movie(cursor.getString(COL_MOVIE_POSTER),
                        cursor.getLong(COL_MOVIE_ID)));
                cursor.moveToNext();
            }
            // make sure to close the cursor
            cursor.close();*/

            getLoaderManager().initLoader(FAVORITE_MOVIELIST_LOADER, null, this);

            getActivity().setTitle(R.string.title_activity_main_favorite);
        } else {
            //Get the movies from the internet
            updateMovies();
        }
    }

    private void updateMovies() {
        String sortBy = Utility.getSortPreference(getActivity());
        String sortOrder;
        if (sortBy.compareToIgnoreCase(getString(R.string.pref_sort_highest_rated)) == 0) {
            getActivity().setTitle(R.string.title_activity_main_top_rated);
            sortOrder = "top_rated";
        } else {
            getActivity().setTitle(R.string.title_activity_main_popular);
            sortOrder = "popular";
        }

        if (Utility.isNetworkConnected(getActivity()) == true) {
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

        if (mPosition != GridView.INVALID_POSITION) {
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                MovieContract.MovieEntry.CONTENT_URI,
                MOVIE_GRIDVIEW_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ArrayList<Movie> movies = new ArrayList<>();
        if (data != null && data.moveToFirst()) {
            while (!data.isAfterLast()) {
                movies.add(new Movie(Utility.getBitmapFromBlob(data.getBlob(COL_MOVIE_POSTER_IMAGE)),
                        data.getLong(COL_MOVIE_ID), true));
                data.moveToNext();
            }
            updateUi(movies);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    /**
     * {@link AsyncTask} to perform the network request on a background thread
     */
    private class MovieAsyncTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
            // Create URL object
            URL url = QueryUtils.createUrl(params[0]);

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
    }
}
