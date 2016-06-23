package com.example.faizan.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class MovieReviewAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> listDataHeader;
    private HashMap<String, List<MovieReviewInfo>> listDataChild;

    public MovieReviewAdapter(Context c, List<String> dataHeader,
                              HashMap<String, List<MovieReviewInfo>> childData) {
        context = c;
        listDataHeader = dataHeader;
        listDataChild = childData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = ((MovieReviewInfo) getChild(groupPosition, childPosition)).content;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(
                                        Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_movie_reviews_child, null);
        }

        TextView listChildTextView = (TextView) convertView.findViewById(
                                            R.id.list_item_movie_reviews_child_textview);
        listChildTextView.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listDataChild.get(listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                                        Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_movie_reviews_parent, null);
        }

        TextView listHeaderTextView = (TextView) convertView.findViewById(
                                        R.id.list_item_movie_reviews_parent_textview);
        listHeaderTextView.setText(headerTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
