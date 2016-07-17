package com.example.faizan.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.faizan.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

public class FetchMovieReviewsTask extends AsyncTask<String, Void, MovieReviewInfo[]> {
    private final String LOG_TAG = FetchMovieReviewsTask.class.getSimpleName();
    private final Context mContext;

    private MovieReviewAdapter mMovieReviewAdapter;

    private static final String[] MOVIE_REVIEW_COLUMNS = {
            MovieContract.ReviewEntry.COLUMN_MOVIE_ID,
            MovieContract.ReviewEntry.COLUMN_AUTHOR,
            MovieContract.ReviewEntry.COLUMN_CONTENT,
            MovieContract.ReviewEntry.COLUMN_URL,
    };

    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_REVIEW_AUTHOR = 1;
    static final int COL_MOVIE_REVIEW_CONTENT = 2;
    static final int COL_MOVIE_REVIEW_URL = 3;

    public FetchMovieReviewsTask(Context context, MovieReviewAdapter adapter) {
        mContext = context;
        mMovieReviewAdapter = adapter;
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

            SharedPreferences sharedPrefs =
                    PreferenceManager.getDefaultSharedPreferences(mContext);
            String sortOrder = sharedPrefs.getString(
                    mContext.getString(R.string.pref_sort_order_key),
                    mContext.getString(R.string.pref_sort_order_most_popular));
            if (sortOrder.equals(mContext.getString(R.string.pref_sort_order_favourites))) {
                Cursor movieReviewCursor = mContext.getContentResolver().query(
                        MovieContract.ReviewEntry.CONTENT_URI,
                        MOVIE_REVIEW_COLUMNS,
                        MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{params[0]},
                        null
                );
                int i = 0;
                if (movieReviewCursor != null /*&& movieCursor.moveToFirst()*/) {
                    MovieReviewInfo[] resultMovieReviewInfoItems = new MovieReviewInfo[movieReviewCursor.getCount()];
                    while (movieReviewCursor.moveToNext()) {
                        resultMovieReviewInfoItems[i++] = new MovieReviewInfo(movieReviewCursor.getString(COL_MOVIE_ID),
                                movieReviewCursor.getString(COL_MOVIE_REVIEW_AUTHOR),
                                movieReviewCursor.getString(COL_MOVIE_REVIEW_CONTENT),
                                movieReviewCursor.getString(COL_MOVIE_REVIEW_URL));
                    }
                    movieReviewCursor.close();
                    return resultMovieReviewInfoItems;
                }
            }

            builtUri = Uri.parse(MOVIE_REVIEW_BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while((line = reader.readLine()) != null) {
                buffer.append(line.concat("\n"));
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
                mMovieReviewAdapter.add(movieReviewItem);
            }
        }
    }
}
