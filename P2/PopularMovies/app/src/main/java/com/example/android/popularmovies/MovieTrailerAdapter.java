package com.example.android.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ramshah on 8/26/16.
 */
public class MovieTrailerAdapter extends ArrayAdapter<Trailer> {
    public MovieTrailerAdapter(Context context, List<Trailer> trailers) {
        super(context, 0, trailers);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listViewItem = convertView;
        ViewHolder holder;

        if (listViewItem == null) {
            listViewItem = LayoutInflater.from(getContext()).inflate(
                    R.layout.movie_trailer_list_item, parent, false);
            holder = new ViewHolder();
            holder.trailerName = (TextView) listViewItem.findViewById(R.id.trailer_textview);
            listViewItem.setTag(holder);
        } else {
            holder = (ViewHolder) listViewItem.getTag();
        }

        Trailer currentTrailer = getItem(position);
        holder.trailerName.setText(currentTrailer.getName());

        return listViewItem;
    }

    static class ViewHolder {
        private TextView trailerName;
    }
}
