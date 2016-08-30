package com.example.android.popularmovies;

/**
 * Created by ramshah on 8/27/16.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper methods
 */
public final class Utility {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();
    private static boolean mIsTwoPane;

    /**
     * Create a private constructor because no one should ever create a {@link Utility} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name Utility (and an object instance of Utility is not needed).
     */
    private Utility() {
    }

    public static String getSortPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String sortOrder = prefs.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_most_popular));

        return sortOrder;
    }

    public static String formatUserRating(String userRating) {
        return userRating + "/10";
    }

    /**
     * Return the formatted date string (i.e. "Mar 3, 1984") from a Date object.
     */
    public static String formatReleaseDate(String releaseDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date dateObject;
        try {
            dateObject = format.parse(releaseDate);
        } catch (ParseException pe) {
            Log.e(LOG_TAG, "Error while parsing release date");
            return releaseDate;
        }
        format = new SimpleDateFormat("LLL dd, yyyy");

        return format.format(dateObject);
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    // convert from bitmap to byte array
    public static byte[] getBlobFromBitmap(Bitmap image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getBitmapFromBlob(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static boolean isTwoPane() {
        return mIsTwoPane;
    }

    public static void setTwoPane(boolean isTwoPane) {
        Utility.mIsTwoPane = isTwoPane;
    }
}
