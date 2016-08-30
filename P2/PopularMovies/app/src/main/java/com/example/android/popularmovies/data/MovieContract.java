package com.example.android.popularmovies.data;

/**
 * Created by ramshah on 8/24/16.
 */

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the movie database.
 */
public class MovieContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.example.android.popularmovies";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /* Inner class that defines the table contents of the movie table */
    public static final class MovieEntry implements BaseColumns {

        //table name
        public static final String TABLE_NAME = "movie";

        //columns
        // movie id as returned by API, to identify the movie
        public static final String COLUMN_MOVIE_ID = "movie_id";
        // title of the movie
        public static final String COLUMN_TITLE = "title";
        // poster of the movie
        public static final String COLUMN_POSTER = "poster";
        // poster image of the movie
        public static final String COLUMN_POSTER_IMAGE = "poster_image";
        // plot of the movie
        public static final String COLUMN_PLOT = "plot";
        // user rating of the movie
        public static final String COLUMN_USER_RATING = "user_rating";
        // release date of the movie
        public static final String COLUMN_RELEASE_DATE = "release_date";

        // create content uri
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        // create cursor of base type directory for multiple entries
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;
        // create cursor of base type item for single entry
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;

        // for building URIs on insertion
        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieWithIdUri(String movieId) {
            return CONTENT_URI.buildUpon().appendPath(movieId).build();
        }
    }
}
