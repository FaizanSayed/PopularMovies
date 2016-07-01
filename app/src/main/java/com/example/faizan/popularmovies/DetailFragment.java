package com.example.faizan.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.example.faizan.popularmovies.data.MovieContract.MovieEntry;
import com.example.faizan.popularmovies.data.MovieContract.VideoEntry;
import com.example.faizan.popularmovies.data.MovieContract.ReviewEntry;

public class DetailFragment extends Fragment {
    private final String LOG_TAG = DetailFragment.class.getSimpleName();
    private MovieVideoAdapter mMovieVideoAdapter;
    private MovieReviewAdapter mMovieReviewAdapter;
    private ExpandableListView expandableListView;
    ListView movieVideoListView;
    private View rootView;
    private String movie_id;
    private String movie_title;
    private String movie_poster_path;
    private String movie_overview;
    private String movie_popularity;
    private String movie_vote_average;
    private String movie_release_date;
    boolean isDataInDb = false;
    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra("posterPath")){
                movie_poster_path = intent.getStringExtra("posterPath");
                String url = "http://image.tmdb.org/t/p/w185" + movie_poster_path;
                Picasso.with(getContext()).load(url).into((ImageView)rootView.findViewById(R.id.detail_imageview));
            }
            if (intent.hasExtra("title")) {
                movie_title = intent.getStringExtra("title");
                ((TextView)rootView.findViewById(R.id.detail_title_textview))
                        .setText(movie_title);
            }

            if (intent.hasExtra("overview")) {
                movie_overview = intent.getStringExtra("overview");
                ((TextView)rootView.findViewById(R.id.detail_overview_textview))
                        .setText(movie_overview);
            }

            if (intent.hasExtra("popularity")) {
                movie_popularity = Double.toString(intent.getDoubleExtra("popularity", 0));
                ((TextView)rootView.findViewById(R.id.detail_popularity_textview))
                        .setText(movie_popularity);
            }

            if (intent.hasExtra("voteAverage")) {
                movie_vote_average = Double.toString(intent.getDoubleExtra("voteAverage", 0));
                ((TextView)rootView.findViewById(R.id.detail_vote_average_textview))
                        .setText(movie_vote_average + "/10");
            }

            if (intent.hasExtra("releaseDate")) {
                movie_release_date = intent.getStringExtra("releaseDate");
                ((TextView)rootView.findViewById(R.id.detail_release_date_textview))
                        .setText(movie_release_date);
            }

            if (intent.hasExtra("id")) {
                movie_id = intent.getStringExtra("id");
            }

        }



        mMovieVideoAdapter =
                new MovieVideoAdapter(getActivity(),
                        R.layout.list_item_movie_videos,
                        R.id.list_item_movie_videos_textview,
                        new ArrayList<MovieVideoInfo>());


//        movieVideoListView = (ListView) rootView.findViewById(R.id.list_view_movie_videos);
//        movieVideoListView.setAdapter(mMovieVideoAdapter);
//
//        movieVideoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String url = "https://www.youtube.com/watch?v=" + mMovieVideoAdapter.getItem(position).key;
//                Uri uri = Uri.parse(url);
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
//                    startActivity(intent);
//                }
//            }
//        });

        final LinearLayout movieVideoLinearLayout = (LinearLayout) rootView.findViewById(R.id.movie_video_detail_linear_layout);
        final TextView trailerTitleTextView = (TextView) rootView.findViewById(R.id.trailer_title_textview);


        mMovieVideoAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
//                super.onChanged();
                movieVideoLinearLayout.removeAllViews();
                movieVideoLinearLayout.addView(trailerTitleTextView);
                int adapterCount = mMovieVideoAdapter.getCount();
                for (int i = 0; i < adapterCount; i++) {
                    final View item =  mMovieVideoAdapter.getView(i, null, null);
                    item.setId(i);
                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String url = "https://www.youtube.com/watch?v=" + mMovieVideoAdapter.getItem(item.getId()).key;
                            Uri uri = Uri.parse(url);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivity(intent);
                            }
                        }
                    });

                    //Log.e(LOG_TAG,"View text: " + item.getText().toString());
                    movieVideoLinearLayout.addView(item);
                }

            }
        });

        mMovieReviewAdapter = new MovieReviewAdapter(getContext(), new ArrayList<String>(), new HashMap<String, List<MovieReviewInfo>>());

//        expandableListView = (ExpandableListView) rootView.findViewById(R.id.list_view_movie_reviews);
//        expandableListView.setAdapter(mMovieReviewAdapter);

        final LinearLayout movieReviewLinearLayout = (LinearLayout) rootView.findViewById(R.id.movie_review_detail_linear_layout);
        final TextView reviewTitleTextView = (TextView) rootView.findViewById(R.id.review_title_textview);

        mMovieReviewAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
//                super.onChanged();
                movieReviewLinearLayout.removeAllViews();
                movieReviewLinearLayout.addView(reviewTitleTextView);
                int adapterGroupCount = mMovieReviewAdapter.getGroupCount();
//                Log.e(LOG_TAG,"Group Adapter Count: " + adapterGroupCount);
                for (int i = 0; i < adapterGroupCount; i++) {
                    final TextView item =  (TextView) mMovieReviewAdapter.getGroupView(i, false, null, null);
                    LinearLayout item_parent = (LinearLayout) item.getParent();
                    item.setId(i);
                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MovieReviewInfo reviewItem = (MovieReviewInfo) mMovieReviewAdapter.getChild(item.getId(), 0);
                            if (item.getText().equals(reviewItem.author)){
                                item.setText(reviewItem.author + "\n" + reviewItem.content);
                            } else {
                                item.setText(reviewItem.author);
                            }
                        }
                    });

                    movieReviewLinearLayout.addView(item_parent);
                }
            }
        });



        final ToggleButton movieFavouriteButton = (ToggleButton)rootView.findViewById(R.id.mark_favourite_button);
        isDataInDb = isMovieDataInDb(movie_id);
//        Log.e(LOG_TAG, "Data for " + movie_title + " " + movie_id + " is in db: " + isDataInDb);
        movieFavouriteButton.setChecked(isDataInDb);
        movieFavouriteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
//                    Log.e(LOG_TAG, "Movie to be marked as favorite: " + movie_title);
                    long movieDbId = addMovie(movie_id, movie_poster_path, movie_title, movie_overview, movie_popularity, movie_vote_average, movie_release_date);
//                    Log.e(LOG_TAG, "Movie info " + movie_title + " successfully added to local database with db id: " + movieDbId);
//                    Log.e(LOG_TAG, "No. of movie trailers: " + mMovieVideoAdapter.getCount());
                    Vector<ContentValues> cVVideoVector = new Vector(mMovieVideoAdapter.getCount());
                    for (int i = 0; i < mMovieVideoAdapter.getCount(); i++) {
                      //Log.e(LOG_TAG, "video: " + mMovieVideoAdapter.getItem(i).name);
                        MovieVideoInfo movieVideoInfoItem = mMovieVideoAdapter.getItem(i);
                        ContentValues movieVideoValues = new ContentValues();
                        movieVideoValues.put(VideoEntry.COLUMN_MOVIE_VIDEO_ID, movieVideoInfoItem.id);
                        movieVideoValues.put(VideoEntry.COLUMN_NAME, movieVideoInfoItem.name);
                        movieVideoValues.put(VideoEntry.COLUMN_TYPE, movieVideoInfoItem.type);
                        movieVideoValues.put(VideoEntry.COLUMN_SITE, movieVideoInfoItem.site);
                        movieVideoValues.put(VideoEntry.COLUMN_KEY, movieVideoInfoItem.key);
                        movieVideoValues.put(VideoEntry.COLUMN_MOVIE_ID, movie_id);

                        cVVideoVector.add(movieVideoValues);
                    }

                    int movie_video_inserted = 0;

                    if (cVVideoVector.size() > 0) {
                        ContentValues[] cvVideoArray = new ContentValues[cVVideoVector.size()];
                        cVVideoVector.toArray(cvVideoArray);
                        movie_video_inserted = getContext().getContentResolver().bulkInsert(VideoEntry.CONTENT_URI, cvVideoArray);

                    }

//                    Log.e(LOG_TAG, "Movie Video information for " + movie_video_inserted + " items inserted.");
                    Vector<ContentValues> cVReviewVector = new Vector(mMovieReviewAdapter.getGroupCount());
                    for (int i = 0; i < mMovieReviewAdapter.getGroupCount(); i++) {
                        //Log.e(LOG_TAG, "the review: " + reviewAdapter.getChild(i,0).toString());
                        //Log.e(LOG_TAG, "review author: " + reviewAdapter.getGroup(i).toString());
                        MovieReviewInfo movieReviewInfoItem = (MovieReviewInfo) mMovieReviewAdapter.getChild(i, 0);
                        ContentValues movieReviewValues = new ContentValues();
                        movieReviewValues.put(ReviewEntry.COLUMN_MOVIE_REVIEW_ID, movieReviewInfoItem.id);
                        movieReviewValues.put(ReviewEntry.COLUMN_AUTHOR, movieReviewInfoItem.author);
                        movieReviewValues.put(ReviewEntry.COLUMN_CONTENT, movieReviewInfoItem.content);
                        movieReviewValues.put(ReviewEntry.COLUMN_URL, movieReviewInfoItem.url);
                        movieReviewValues.put(ReviewEntry.COLUMN_MOVIE_ID, movie_id);

                        cVReviewVector.add(movieReviewValues);
                    }

                    int movie_review_inserted = 0;

                    if (cVReviewVector.size() > 0) {
                        ContentValues[] cvReviewArray = new ContentValues[cVReviewVector.size()];
                        cVReviewVector.toArray(cvReviewArray);
                        movie_review_inserted = getContext().getContentResolver().bulkInsert(ReviewEntry.CONTENT_URI, cvReviewArray);

                    }

//                    Log.e(LOG_TAG, "Movie Review information for " + movie_review_inserted + " items inserted.");
                }


                else {
//                    Log.e(LOG_TAG, "Movie to be unmarked as favorite: " + movie_title);
                    int rowsDeleted;
                    rowsDeleted = getContext().getContentResolver().delete(MovieEntry.CONTENT_URI,
                            MovieEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[]{movie_id});
//                    Log.e(LOG_TAG, "No. of rows from movie table deleted: " + rowsDeleted);
                    rowsDeleted = getContext().getContentResolver().delete(VideoEntry.CONTENT_URI,
                            VideoEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[]{movie_id});
//                    Log.e(LOG_TAG, "No. of rows from video table deleted: " + rowsDeleted);
                    rowsDeleted = getContext().getContentResolver().delete(ReviewEntry.CONTENT_URI,
                            ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[]{movie_id});
//                    Log.e(LOG_TAG, "No. of rows from review table deleted: " + rowsDeleted);
                }

            }
        });

        return rootView;
    }



    private boolean isMovieDataInDb(String movieId) {
        boolean result = false;
        Cursor movieCursor = getContext().getContentResolver().query(
                MovieEntry.CONTENT_URI,
                new String[]{MovieEntry.COLUMN_MOVIE_ID},
                MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{movie_id},
                null);

        if (movieCursor.moveToFirst()) {
            result = true;
        }
        movieCursor.close();
        return result;
    }

    long addMovie(String movie_id, String poster_path, String title, String overview, String popularity, String average, String release_date) {

        long movieDbId;
        Cursor movieCursor = getContext().getContentResolver().query(
                MovieEntry.CONTENT_URI,
                new String[]{MovieEntry._ID},
                MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{movie_id},
                null);

        if (movieCursor.moveToFirst()) {
            int movieIdIndex = movieCursor.getColumnIndex(MovieEntry._ID);
            movieDbId = movieCursor.getLong(movieIdIndex);
        } else {
            ContentValues movieValues = new ContentValues();

            movieValues.put(MovieEntry.COLUMN_MOVIE_ID, movie_id);
            movieValues.put(MovieEntry.COLUMN_POSTER_PATH, poster_path);
            movieValues.put(MovieEntry.COLUMN_TITLE, title);
            movieValues.put(MovieEntry.COLUMN_OVERVIEW, overview);
            movieValues.put(MovieEntry.COLUMN_POPULARITY, popularity);
            movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, average);
            movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, release_date);

            Uri insertedUri = getContext().getContentResolver().insert(
                    MovieEntry.CONTENT_URI,
                    movieValues
            );

            movieDbId = ContentUris.parseId(insertedUri);
        }
        movieCursor.close();

        return movieDbId;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.moviefragment, menu);
    }

    private void updateMovieVideoList() {
        if (movie_id != null){
            FetchMovieVideosTask movieVideosTask = new FetchMovieVideosTask(getActivity(), mMovieVideoAdapter);
            movieVideosTask.execute(movie_id);
//            setListViewHeightBasedOnChildren(movieVideoListView);
        }

    }

    private void updateMovieReviewList() {
        if (movie_id != null) {
            FetchMovieReviewsTask movieReviewTask = new FetchMovieReviewsTask(getActivity(), mMovieReviewAdapter);
//            Log.e(LOG_TAG, "Executing fetchreviewtask");
            movieReviewTask.execute(movie_id);
//            setListViewHeightBasedOnChildren(expandableListView);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovieVideoList();
        updateMovieReviewList();
    }

//    public static void setListViewHeightBasedOnChildren(ListView listView) {
//        Log.e("setListView", "inside setListViewHeightBasedOnChildren");
//        ListAdapter listAdapter = listView.getAdapter();
//        if (listAdapter == null) {
//            // pre-condition
//            return;
//        }
//
//        int totalHeight = 0;
//        for (int i = 0; i < listAdapter.getCount(); i++) {
//            View listItem = listAdapter.getView(i, null, listView);
//            listItem.measure(0, 0);
//            totalHeight += listItem.getMeasuredHeight();
//        }
//
//        ViewGroup.LayoutParams params = listView.getLayoutParams();
//        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
//        Log.e("setListView", "height of listview: " + params.height);
//        listView.setLayoutParams(params);
//        listView.requestLayout();
//    }

}

