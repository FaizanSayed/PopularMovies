package com.example.faizan.popularmovies;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends ArrayAdapter<MovieInfo> {
    private final String LOG_TAG = ImageAdapter.class.getSimpleName();

    public ImageAdapter(Context context, int resource, int textViewResourceId, List<MovieInfo> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = (ImageView) convertView;
        if (imageView == null) {
            imageView = new ImageView(getContext());
        }
        String url = "http://image.tmdb.org/t/p/w185" + getItem(position).posterPath;
        Picasso.with(getContext()).load(url).into(imageView);
        return imageView;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
