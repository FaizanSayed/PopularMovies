package com.example.faizan.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.faizan.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieInfoAdapter extends ArrayAdapter<MovieInfo> {

    public MovieInfoAdapter(Context context, int resource, int textViewResourceId, List<MovieInfo> objects) {
        super(context, resource, textViewResourceId, objects);
    }

//    public MovieInfoAdapter(Context context, Cursor c, int flags) {
//        super(context, c, flags);
//    }

//    @Override
//    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//        View view = LayoutInflater.from(context).inflate(R.layout.list_item_movies, parent, false);
//
//        return view;
//    }

//    @Override
//    public void bindView(View view, Context context, Cursor cursor) {
//        ImageView imageView = (ImageView) view;
//
//        int idx_poster_path = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
//        String url = "http://image.tmdb.org/t/p/w185" + cursor.getString(idx_poster_path);
//        Picasso.with(context).load(url).into(imageView);
//    }

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
