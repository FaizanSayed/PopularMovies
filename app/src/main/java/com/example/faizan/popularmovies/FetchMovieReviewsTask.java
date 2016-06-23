package com.example.faizan.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ExpandableListView;

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

public class FetchMovieReviewsTask extends AsyncTask<String, Void, MovieReviewInfo[]> {
    private final String LOG_TAG = FetchMovieReviewsTask.class.getSimpleName();
    private final Context mContext;

    private MovieReviewAdapter mMovieReviewAdapter;
    private List<String> authorList;
    private HashMap<String, List<MovieReviewInfo>> reviewAuthorMap;
    private ExpandableListView expandableListView;

    public FetchMovieReviewsTask(Context context, ExpandableListView listView) {
        mContext = context;
        expandableListView = listView;
        authorList = new ArrayList();
        reviewAuthorMap = new HashMap();
    }

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
                List<MovieReviewInfo> content = new ArrayList();
                content.add(movieReviewItem);
                reviewAuthorMap.put(movieReviewItem.author, content);
                Log.e(LOG_TAG, movieReviewItem.author);
            }
            mMovieReviewAdapter = new MovieReviewAdapter(mContext, authorList, reviewAuthorMap);
            expandableListView.setAdapter(mMovieReviewAdapter);
//                setListViewHeightBasedOnChildren(expandableListView);

        }
    }
}
