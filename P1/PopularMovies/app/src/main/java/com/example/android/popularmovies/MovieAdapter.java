package com.example.android.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ramshah on 8/11/16.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {

    private Context mContext;

    public MovieAdapter(Context context, List<Movie> movies) {
        super(context, 0, movies);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View gridViewItem = convertView;
        ViewHolder holder;

        if (gridViewItem == null) {
            gridViewItem = LayoutInflater.from(getContext()).inflate(R.layout.movie_list_item, parent, false);
            holder = new ViewHolder();
            holder.posterImageView = (ImageView) gridViewItem.findViewById(R.id.movie_Poster);
            gridViewItem.setTag(holder);
        } else {
            holder = (ViewHolder) gridViewItem.getTag();
        }

        Movie currentMovie = getItem(position);
        Picasso.with(mContext).load(currentMovie.getMoviePosterImage()).into(holder.posterImageView);

        return gridViewItem;
    }

    static class ViewHolder {
        private ImageView posterImageView;
    }
}
