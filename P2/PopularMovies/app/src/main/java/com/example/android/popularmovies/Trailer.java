package com.example.android.popularmovies;

/**
 * Created by ramshah on 8/26/16.
 */
public class Trailer {
    private String mName;
    private String mKey;

    public Trailer(String name, String key) {
        this.mName = name;
        this.mKey = key;
    }

    public String getName() {
        return mName;
    }

    public String getKey() {
        return mKey;
    }
}
