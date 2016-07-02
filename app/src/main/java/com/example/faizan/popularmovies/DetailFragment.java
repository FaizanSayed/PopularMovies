package com.example.faizan.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Vector;

import com.example.faizan.popularmovies.data.MovieContract.MovieEntry;
import com.example.faizan.popularmovies.data.MovieContract.VideoEntry;
import com.example.faizan.popularmovies.data.MovieContract.ReviewEntry;

public class DetailFragment extends Fragment {
    private MovieVideoAdapter mMovieVideoAdapter;
    private MovieReviewAdapter mMovieReviewAdapter;
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

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
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
                movie_vote_average = Double.toString(intent.getDoubleExtra("voteAverage", 0)) + "/10";
                ((TextView)rootView.findViewById(R.id.detail_vote_average_textview))
                        .setText(movie_vote_average);
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

        final LinearLayout movieVideoLinearLayout = (LinearLayout) rootView.findViewById(R.id.movie_video_detail_linear_layout);
        final TextView trailerTitleTextView = (TextView) rootView.findViewById(R.id.trailer_title_textview);

        mMovieVideoAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
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
                    movieVideoLinearLayout.addView(item);
                }

            }
        });

        mMovieReviewAdapter = new MovieReviewAdapter(getActivity(),
                                    R.layout.list_item_movie_reviews,
                                    R.id.list_item_movie_reviews_textview,
                                    new ArrayList<MovieReviewInfo>());

        final LinearLayout movieReviewLinearLayout = (LinearLayout) rootView.findViewById(R.id.movie_review_detail_linear_layout);
        final TextView reviewTitleTextView = (TextView) rootView.findViewById(R.id.review_title_textview);

        mMovieReviewAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                movieReviewLinearLayout.removeAllViews();
                movieReviewLinearLayout.addView(reviewTitleTextView);
                int adapterCount = mMovieReviewAdapter.getCount();
                for (int i = 0; i < adapterCount; i++) {
                    final TextView item = (TextView) mMovieReviewAdapter.getView(i, null, null);
                    item.setId(i);
                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MovieReviewInfo reviewItem = mMovieReviewAdapter.getItem(item.getId());
                            if (item.getText().equals(reviewItem.author)){
                                String result = reviewItem.author + "\n" + reviewItem.content;
                                item.setText(result);
                            } else {
                                item.setText(reviewItem.author);
                            }
                        }
                    });

                    movieReviewLinearLayout.addView(item);
                }
            }
        });

        final ToggleButton movieFavouriteButton = (ToggleButton)rootView.findViewById(R.id.mark_favourite_button);
        isDataInDb = isMovieDataInDb(movie_id);
        movieFavouriteButton.setChecked(isDataInDb);
        movieFavouriteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    addMovie(movie_id, movie_poster_path, movie_title, movie_overview, movie_popularity, movie_vote_average, movie_release_date);
                    Vector<ContentValues> cVVideoVector = new Vector<>(mMovieVideoAdapter.getCount());
                    for (int i = 0; i < mMovieVideoAdapter.getCount(); i++) {
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

                    if (cVVideoVector.size() > 0) {
                        ContentValues[] cvVideoArray = new ContentValues[cVVideoVector.size()];
                        cVVideoVector.toArray(cvVideoArray);
                        getContext().getContentResolver().bulkInsert(VideoEntry.CONTENT_URI, cvVideoArray);

                    }

                    Vector<ContentValues> cVReviewVector = new Vector<>(mMovieReviewAdapter.getCount());
                    for (int i = 0; i < mMovieReviewAdapter.getCount(); i++) {
                        MovieReviewInfo movieReviewInfoItem = mMovieReviewAdapter.getItem(i);
                        ContentValues movieReviewValues = new ContentValues();
                        movieReviewValues.put(ReviewEntry.COLUMN_MOVIE_REVIEW_ID, movieReviewInfoItem.id);
                        movieReviewValues.put(ReviewEntry.COLUMN_AUTHOR, movieReviewInfoItem.author);
                        movieReviewValues.put(ReviewEntry.COLUMN_CONTENT, movieReviewInfoItem.content);
                        movieReviewValues.put(ReviewEntry.COLUMN_URL, movieReviewInfoItem.url);
                        movieReviewValues.put(ReviewEntry.COLUMN_MOVIE_ID, movie_id);

                        cVReviewVector.add(movieReviewValues);
                    }

                    if (cVReviewVector.size() > 0) {
                        ContentValues[] cvReviewArray = new ContentValues[cVReviewVector.size()];
                        cVReviewVector.toArray(cvReviewArray);
                        getContext().getContentResolver().bulkInsert(ReviewEntry.CONTENT_URI, cvReviewArray);

                    }
                }


                else {
                    getContext().getContentResolver().delete(MovieEntry.CONTENT_URI,
                            MovieEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[]{movie_id});
                    getContext().getContentResolver().delete(VideoEntry.CONTENT_URI,
                            VideoEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[]{movie_id});
                    getContext().getContentResolver().delete(ReviewEntry.CONTENT_URI,
                            ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[]{movie_id});
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
                new String[]{movieId},
                null);
        if (movieCursor != null && movieCursor.moveToFirst()) {
            result = true;
            movieCursor.close();
        }
        return result;
    }

    long addMovie(String movie_id, String poster_path, String title, String overview, String popularity, String average, String release_date) {

        long movieDbId = -1;
        Cursor movieCursor = getContext().getContentResolver().query(
                MovieEntry.CONTENT_URI,
                new String[]{MovieEntry._ID},
                MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{movie_id},
                null);

        if (movieCursor != null) {
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
        }
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
        }

    }

    private void updateMovieReviewList() {
        if (movie_id != null) {
            FetchMovieReviewsTask movieReviewTask = new FetchMovieReviewsTask(getActivity(), mMovieReviewAdapter);
            movieReviewTask.execute(movie_id);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovieVideoList();
        updateMovieReviewList();
    }
}

