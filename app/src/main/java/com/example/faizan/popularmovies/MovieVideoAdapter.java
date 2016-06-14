package com.example.faizan.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class MovieVideoAdapter extends ArrayAdapter<MovieVideoInfo> {
    private final String LOG_TAG = MovieVideoAdapter.class.getSimpleName();

    public MovieVideoAdapter(Context context, int resource, int textViewResourceId, List<MovieVideoInfo> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) convertView;
        if (textView == null) {
            textView = new TextView(getContext());
        }
        textView.setText(getItem(position).name);
        return textView;
    }
}
