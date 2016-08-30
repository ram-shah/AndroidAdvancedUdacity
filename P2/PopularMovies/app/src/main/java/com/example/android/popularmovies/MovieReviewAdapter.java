package com.example.android.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ramshah on 8/27/16.
 */
public class MovieReviewAdapter extends ArrayAdapter<Review> {
    public MovieReviewAdapter(Context context, List<Review> reviews) {
        super(context, 0, reviews);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listViewItem = convertView;
        ViewHolder holder;

        if (listViewItem == null) {
            listViewItem = LayoutInflater.from(getContext()).inflate(
                    R.layout.movie_review_list_item, parent, false);
            holder = new ViewHolder();
            holder.reviewAuthor = (TextView) listViewItem.findViewById(R.id.review_author);
            holder.reviewContent = (TextView) listViewItem.findViewById(R.id.review_content);
            listViewItem.setTag(holder);
        } else {
            holder = (ViewHolder) listViewItem.getTag();
        }

        Review currentReview = getItem(position);
        holder.reviewAuthor.setText(currentReview.getAuthor());
        holder.reviewContent.setText(currentReview.getReview());

        return listViewItem;
    }

    static class ViewHolder {
        private TextView reviewAuthor;
        private TextView reviewContent;
    }
}