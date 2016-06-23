package com.example.faizan.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchMovieVideosTask extends AsyncTask<String, Void, MovieVideoInfo[]> {
    private final String LOG_TAG = FetchMovieVideosTask.class.getSimpleName();
    private MovieVideoAdapter mMovieVideoAdapter;
    private final Context mContext;

    public FetchMovieVideosTask(Context context, MovieVideoAdapter adapter) {
        mContext = context;
        mMovieVideoAdapter = adapter;
    }

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
            String id;
            String key;
            String name;
            String site;
            String type;

            JSONObject movieVideoListItem = movieVideosList.getJSONObject(i);
            id = movieVideoListItem.getString(TMDB_MOVIE_VIDEO_ID);
            key = movieVideoListItem.getString(TMDB_MOVIE_VIDEO_KEY);
            name = movieVideoListItem.getString(TMDB_MOVIE_VIDEO_NAME);
            site = movieVideoListItem.getString(TMDB_MOVIE_VIDEO_SITE);
            type = movieVideoListItem.getString(TMDB_MOVIE_VIDEO_TYPE);
            resultMovieVideoItems[i] = new MovieVideoInfo(id, key, name, site, type);
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
            //setListViewHeightBasedOnChildren(movieVideoListView);
        }
    }
}

