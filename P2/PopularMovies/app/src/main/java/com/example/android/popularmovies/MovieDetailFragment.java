package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Tag for the log messages */
    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    static final String DETAIL_MOVIE_ID = "MOVIE_ID";

    private long mMovieId;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER,
            MovieContract.MovieEntry.COLUMN_POSTER_IMAGE,
            MovieContract.MovieEntry.COLUMN_PLOT,
            MovieContract.MovieEntry.COLUMN_USER_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_MOVIE_ID = 0;
    public static final int COL_TITLE = 1;
    public static final int COL_POSTER = 2;
    public static final int COL_POSTER_IMAGE = 3;
    public static final int COL_PLOT = 4;
    public static final int COL_USER_RATING = 5;
    public static final int COL_RELEASE_DATE = 6;

    private ImageView mPosterThumbnail;
    private TextView mTitle;
    private TextView mPlot;
    private TextView mUserRating;
    private TextView mReleaseDate;
    private Button mFavorite;
    private Button mTrailers;
    private Button mReviews;

    private Movie mMovie;

    public MovieDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovieId = arguments.getLong(MovieDetailFragment.DETAIL_MOVIE_ID);
        }

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        mPosterThumbnail = (ImageView) rootView.findViewById(R.id.movie_poster_thumbnail);
        mTitle = (TextView) rootView.findViewById(R.id.title);
        mPlot = (TextView) rootView.findViewById(R.id.plot);
        mUserRating = (TextView) rootView.findViewById(R.id.user_rating);
        mReleaseDate = (TextView) rootView.findViewById(R.id.release_date);
        mFavorite = (Button) rootView.findViewById(R.id.favorite);
        mFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start an AsyncTask to write the favorite movie to database
                mPosterThumbnail.buildDrawingCache();
                MovieFavoriteAsyncTask task = new MovieFavoriteAsyncTask();
                task.execute(mPosterThumbnail.getDrawingCache());
            }
        });
        mFavorite.setVisibility(View.INVISIBLE);

        mTrailers = (Button) rootView.findViewById(R.id.trailers);
        mTrailers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utility.isNetworkConnected(getActivity()) == false) {
                    Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Launch MovieTrailerActivity to show trailers
                if (Utility.isTwoPane()) {
                    // In two-pane mode, show the trailer view in this activity by
                    // adding or replacing the trailer fragment using a
                    // fragment transaction.
                    Bundle args = new Bundle();
                    args.putLong(MovieTrailerFragment.DETAIL_MOVIE_ID, mMovieId);

                    MovieTrailerFragment trailerFragment = new MovieTrailerFragment();
                    trailerFragment.setArguments(args);

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_detail_container, trailerFragment)
                            .commit();
                } else {
                    Intent movieTrailersIntent = new Intent(getActivity(), MovieTrailerActivity.class);
                    movieTrailersIntent.putExtra("movieId", mMovieId);
                    startActivity(movieTrailersIntent);
                }
            }
        });
        mTrailers.setVisibility(View.INVISIBLE);

        mReviews = (Button) rootView.findViewById(R.id.reviews);
        mReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utility.isNetworkConnected(getActivity()) == false) {
                    Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Launch MovieReviewActivity to show reviews
                if (Utility.isTwoPane()) {
                    // In two-pane mode, show the review view in this activity by
                    // adding or replacing the review fragment using a
                    // fragment transaction.
                    Bundle args = new Bundle();
                    args.putLong(MovieReviewFragment.DETAIL_MOVIE_ID, mMovieId);

                    MovieReviewFragment reviewFragment = new MovieReviewFragment();
                    reviewFragment.setArguments(args);

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_detail_container, reviewFragment)
                            .commit();
                } else {
                    Intent movieReviewsIntent = new Intent(getActivity(), MovieReviewActivity.class);
                    movieReviewsIntent.putExtra("movieId", mMovieId);
                    startActivity(movieReviewsIntent);
                }
            }
        });
        mReviews.setVisibility(View.INVISIBLE);

        String sortBy = Utility.getSortPreference(getActivity());
        if (sortBy.compareToIgnoreCase(getString(R.string.pref_sort_favorite)) == 0) {
            // Get the movie details from local database
            getFavoriteMovieDetails();
        } else {
            // Get the movie details from the internet
            getMovieDetails(mMovieId);
        }

        return rootView;
    }

    private void getFavoriteMovieDetails() {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    private void getMovieDetails(long id) {

        if (Utility.isNetworkConnected(getActivity()) == true) {
            // Kick off an {@link AsyncTask} to perform the network request
            MovieDetailAsyncTask task = new MovieDetailAsyncTask();
            task.execute(id);
        }
    }

    /**
     * Update the screen to display information of {@link Movie} details.
     */
    private void updateUi() {
        if (mMovie.isFavorite() == true) {
            mPosterThumbnail.setImageBitmap(mMovie.getMoviePosterBitmap());
        } else {
            Picasso.with(getActivity()).load(mMovie.getMoviePosterImage()).into(mPosterThumbnail);
        }
        mTitle.setText(mMovie.getTitle());
        mPlot.setText(mMovie.getPlot());
        mUserRating.setText(Utility.formatUserRating(mMovie.getUserRating()));
        mReleaseDate.setText(Utility.formatReleaseDate(mMovie.getReleaseDate()));

        if (mMovie.isFavorite() == true) {
            // The movie is already marked as favorite.
            // Do not show the favorite button here
            mFavorite.setVisibility(View.INVISIBLE);
        } else {
            mFavorite.setVisibility(View.VISIBLE);
        }
        mTrailers.setVisibility(View.VISIBLE);
        mReviews.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                MovieContract.MovieEntry.buildMovieWithIdUri(String.valueOf(mMovieId)),
                DETAIL_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            String posterPath = data.getString(COL_POSTER);
            Bitmap posterImage = Utility.getBitmapFromBlob(data.getBlob(COL_POSTER_IMAGE));
            long movieId = data.getLong(COL_MOVIE_ID);
            String title = data.getString(COL_TITLE);
            String plot = data.getString(COL_PLOT);
            String userRating = data.getString(COL_USER_RATING);
            String releaseDate = data.getString(COL_RELEASE_DATE);

            mMovie = new Movie(posterImage, movieId, title, plot, userRating, releaseDate, true);
            updateUi();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    /**
     * {@link AsyncTask} to perform the network request on a background thread
     */
    private class MovieDetailAsyncTask extends AsyncTask<Long, Void, Movie> {

        @Override
        protected Movie doInBackground(Long... params) {
            // Create URL object
            String movieId = params[0].toString();
            URL url = QueryUtils.createUrl(movieId);

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
            mMovie = movie;
            updateUi();
        }
    }

    /**
     * {@link AsyncTask} to write favorite movie to database
     */
    private class MovieFavoriteAsyncTask extends AsyncTask<Bitmap, Void, Integer> {

        @Override
        protected Integer doInBackground(Bitmap... images) {
            // Store the favorite movie in db
            ContentValues values = new ContentValues();
            values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, mMovie.getMovieId());
            values.put(MovieContract.MovieEntry.COLUMN_TITLE, mMovie.getTitle());
            values.put(MovieContract.MovieEntry.COLUMN_POSTER, mMovie.getMoviePosterImage());
            values.put(MovieContract.MovieEntry.COLUMN_POSTER_IMAGE, Utility.getBlobFromBitmap(images[0]));
            values.put(MovieContract.MovieEntry.COLUMN_PLOT, mMovie.getPlot());
            values.put(MovieContract.MovieEntry.COLUMN_USER_RATING, mMovie.getUserRating());
            values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDate());

            Integer result;
            try {
                // add to database
                getContext().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, values);
                result = R.string.favorite_insert;
            } catch (SQLException e) {
                // This movie is already present in the favorite database. Update the record with
                // latest movie details
                Log.d(LOG_TAG, e.toString());
                getContext().getContentResolver().update(
                        MovieContract.MovieEntry.buildMovieWithIdUri(String.valueOf(mMovie.getMovieId())),
                        values,
                        null,
                        null);
                result = R.string.favorite_update;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Integer resId) {
            Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT).show();
        }
    }
}
