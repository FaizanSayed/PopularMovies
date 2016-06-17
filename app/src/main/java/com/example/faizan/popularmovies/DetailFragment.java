package com.example.faizan.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DetailFragment extends Fragment {
    private final String LOG_TAG = DetailFragment.class.getSimpleName();
    private MovieVideoAdapter mMovieVideoAdapter;
    private MovieReviewAdapter mMovieReviewAdapter;
    private List<String> authorList;
    private HashMap<String, List<String>> reviewAuthorMap;
    private ExpandableListView expandableListView;
    private View rootView;
    private String movie_id = null;
    private String movie_title;
    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra("posterPath")){
                String url = "http://image.tmdb.org/t/p/w185" + intent.getStringExtra("posterPath");
                Picasso.with(getContext()).load(url).into((ImageView)rootView.findViewById(R.id.detail_imageview));
            }
            if (intent.hasExtra("title")) {
                movie_title = intent.getStringExtra("title");
                ((TextView)rootView.findViewById(R.id.detail_title_textview))
                        .setText(movie_title);
            }

            if (intent.hasExtra("overview")) {
                ((TextView)rootView.findViewById(R.id.detail_overview_textview))
                        .setText(intent.getStringExtra("overview"));
            }

            if (intent.hasExtra("popularity")) {
                ((TextView)rootView.findViewById(R.id.detail_popularity_textview))
                        .setText(Double.toString(intent.getDoubleExtra("popularity", 0)));
            }

            if (intent.hasExtra("voteAverage")) {
                ((TextView)rootView.findViewById(R.id.detail_vote_average_textview))
                        .setText(Double.toString(intent.getDoubleExtra("voteAverage", 0)) + "/10");
            }

            if (intent.hasExtra("releaseDate")) {
                ((TextView)rootView.findViewById(R.id.detail_release_date_textview))
                        .setText(intent.getStringExtra("releaseDate"));
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
        ListView movieVideoListView = (ListView) rootView.findViewById(R.id.list_view_movie_videos);
        movieVideoListView.setAdapter(mMovieVideoAdapter);

        movieVideoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = "https://www.youtube.com/watch?v=" + mMovieVideoAdapter.getItem(position).key;
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        expandableListView = (ExpandableListView) rootView.findViewById(R.id.list_view_movie_reviews);

        Button movieFavouriteButton = (Button)rootView.findViewById(R.id.mark_favourite_button);
        movieFavouriteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e(LOG_TAG, "Movie to be marked as favorite: " + movie_title);

            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.moviefragment, menu);
    }

    private void updateMovieVideoList() {
        if (movie_id != null){
            FetchMovieVideosTask movieVideosTask = new FetchMovieVideosTask();
            movieVideosTask.execute(movie_id);
        }

    }

    private void updateMovieReviewList() {
        if (movie_id != null) {
            authorList = new ArrayList<String>();
            reviewAuthorMap = new HashMap<String, List<String>>();
            FetchMovieReviewTask movieReviewTask = new FetchMovieReviewTask();
            Log.e(LOG_TAG, "Executing fetchreviewtask");
            movieReviewTask.execute(movie_id);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovieVideoList();
        updateMovieReviewList();
    }

    public class FetchMovieVideosTask extends AsyncTask<String, Void, MovieVideoInfo[]> {
        private final String LOG_TAG = FetchMovieVideosTask.class.getSimpleName();

        private MovieVideoInfo[] getMovieVideoDataFromJson(String movieVideoJsonStr)
                throws JSONException {
            String TMDB_MOVIE_VIDEO_ID = "id";
            String TMDB_MOVIE_VIDEO_RESULTS = "results";
            String TMDB_MOVIE_VIDEO_KEY = "key";
            String TMDB_MOVIE_VIDEO_NAME = "name";
            String TMDB_MOVIE_VIDEO_SITE = "site";
            String TMDB_MOVIE_VIDEO_TYPE = "type";

            JSONObject movieVideoJson = new JSONObject(movieVideoJsonStr);
            JSONArray movieVideosList = movieVideoJson.getJSONArray(TMDB_MOVIE_VIDEO_RESULTS);

            MovieVideoInfo[] resultMovieVideoItems = new MovieVideoInfo[movieVideosList.length()];
            for (int i = 0; i < movieVideosList.length(); i++) {
                String key;
                String name;
                String site;
                String type;

                JSONObject movieVideoListItem = movieVideosList.getJSONObject(i);
                key = movieVideoListItem.getString(TMDB_MOVIE_VIDEO_KEY);
                name = movieVideoListItem.getString(TMDB_MOVIE_VIDEO_NAME);
                site = movieVideoListItem.getString(TMDB_MOVIE_VIDEO_SITE);
                type = movieVideoListItem.getString(TMDB_MOVIE_VIDEO_TYPE);
                resultMovieVideoItems[i] = new MovieVideoInfo(key, name, site, type);
            }

            return resultMovieVideoItems;
        }

        @Override
        protected MovieVideoInfo[] doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieVideoJsonStr = null;

            Uri builtUri;

            try {
                final String MOVIE_VIDEO_BASE_URL =
                        "https://api.themoviedb.org/3/movie/" + params[0] + "/videos";
                final String API_KEY_PARAM = "api_key";

                builtUri = Uri.parse(MOVIE_VIDEO_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");;
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                movieVideoJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);

                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch(final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieVideoDataFromJson(movieVideoJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(MovieVideoInfo[] result) {
            if (result != null) {
                mMovieVideoAdapter.clear();
                for(MovieVideoInfo movieVideoItem : result) {
                    mMovieVideoAdapter.add(movieVideoItem);
                }
            }
        }
    }

    public class FetchMovieReviewTask extends AsyncTask<String, Void, MovieReviewInfo[]> {
        private final String LOG_TAG = FetchMovieReviewTask.class.getSimpleName();

        private MovieReviewInfo[] getMovieReviewDataFromJson(String movieReviewJsonStr)
                throws JSONException {
            String TMDB_MOVIE_REVIEW_ID = "id";
            String TMDB_MOVIE_REVIEW_RESULTS = "results";
            String TMDB_MOVIE_REVIEW_AUTHOR = "author";
            String TMDB_MOVIE_REVIEW_CONTENT = "content";
            String TMDB_MOVIE_REVIEW_URL = "url";

            JSONObject movieReviewJson = new JSONObject(movieReviewJsonStr);
            JSONArray movieReviewsList = movieReviewJson.getJSONArray(TMDB_MOVIE_REVIEW_RESULTS);

            MovieReviewInfo[] resultMovieReviewItems = new MovieReviewInfo[movieReviewsList.length()];
            for (int i = 0; i < movieReviewsList.length(); i++) {
                String id;
                String author;
                String content;
                String url;

                JSONObject movieReviewListItem = movieReviewsList.getJSONObject(i);
                id = movieReviewListItem.getString(TMDB_MOVIE_REVIEW_ID);
                author = movieReviewListItem.getString(TMDB_MOVIE_REVIEW_AUTHOR);
                content = movieReviewListItem.getString(TMDB_MOVIE_REVIEW_CONTENT);
                url = movieReviewListItem.getString(TMDB_MOVIE_REVIEW_URL);
                resultMovieReviewItems[i] = new MovieReviewInfo(id, author, content, url);
            }

            return resultMovieReviewItems;
        }

        @Override
        protected MovieReviewInfo[] doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieReviewJsonStr = null;

            Uri builtUri;

            try {
                final String MOVIE_REVIEW_BASE_URL =
                        "https://api.themoviedb.org/3/movie/" + params[0] + "/reviews";
                final String API_KEY_PARAM = "api_key";

                builtUri = Uri.parse(MOVIE_REVIEW_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");;
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                movieReviewJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);

                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch(final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieReviewDataFromJson(movieReviewJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(MovieReviewInfo[] result) {
            if (result != null) {
                for(MovieReviewInfo movieReviewItem : result) {
                    authorList.add(movieReviewItem.author);
                    List<String> content = new ArrayList<String>();
                    content.add(movieReviewItem.content);
                    reviewAuthorMap.put(movieReviewItem.author, content);
                    Log.e(LOG_TAG, movieReviewItem.author);
                }
                mMovieReviewAdapter = new MovieReviewAdapter(getContext(), authorList, reviewAuthorMap);
                expandableListView.setAdapter(mMovieReviewAdapter);
            }
        }
    }
}

