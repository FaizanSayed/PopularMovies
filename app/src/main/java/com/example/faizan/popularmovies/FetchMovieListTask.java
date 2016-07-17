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



public class FetchMovieListTask extends AsyncTask<String, Void, MovieInfo[]> {
    private final String LOG_TAG = FetchMovieListTask.class.getSimpleName();
    private MovieInfoAdapter mMovieAdapter;
    private final Context mContext;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
    };

    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_TITLE = 1;
    static final int COL_MOVIE_POSTER_PATH = 2;
    static final int COL_MOVIE_OVERVIEW = 3;
    static final int COL_MOVIE_RELEASE_DATE = 4;
    static final int COL_MOVIE_POPULARITY = 5;
    static final int COL_MOVIE_VOTE_AVERAGE = 6;

    public FetchMovieListTask(Context context, MovieInfoAdapter adapter) {
        mMovieAdapter = adapter;
        mContext = context;
    }


    /**
     * Take the String representing the complete movies information in  JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy: constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private MovieInfo[] getMovieDataFromJson(String movieJsonStr)
            throws JSONException {
        // These are the names of the JSON objects that need to be extracted.
        String TMDB_RESULTS = "results";
        String TMDB_POSTER_PATH = "poster_path";
        String TMDB_OVERVIEW = "overview";
        String TMDB_RELEASE_DATE = "release_date";
        String TMDB_ID = "id";
        String TMDB_ORIGINAL_TITLE = "original_title";
        String TMDB_POPULARITY = "popularity";
        String TMDB_VOTE_AVERAGE = "vote_average";

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray movieList = movieJson.getJSONArray(TMDB_RESULTS);

        MovieInfo[] resultMovieInfoItems = new MovieInfo[movieList.length()];

        for (int i = 0; i < movieList.length(); i++) {
            String poster_path;
            String title;
            String overview;
            double popularity;
            double vote_average;
            String release_date;
            String id;
            MovieInfo movieInfo;

            // Get the JSON object representing the movie list item.
            JSONObject movie_list_item = movieList.getJSONObject(i);
            poster_path = movie_list_item.getString(TMDB_POSTER_PATH);
            title = movie_list_item.getString(TMDB_ORIGINAL_TITLE);
            overview = movie_list_item.getString(TMDB_OVERVIEW);
            popularity = movie_list_item.getDouble(TMDB_POPULARITY);
            vote_average = movie_list_item.getDouble(TMDB_VOTE_AVERAGE);
            release_date = movie_list_item.getString(TMDB_RELEASE_DATE);
            id = movie_list_item.getString(TMDB_ID);
            movieInfo = new MovieInfo(id, poster_path, title, overview, popularity, vote_average, release_date);
            resultMovieInfoItems[i] = movieInfo;
        }
        return resultMovieInfoItems;
    }

    @Override
    protected MovieInfo[] doInBackground(String... params) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        Uri builtUri;


        try {

            // Construct the URL for the TheMovieDatabase query
            final String MOVIE_BASE_URL =
                    "https://api.themoviedb.org/3";
            final String SORT_BY_PARAM = "sort_by";
            final String API_KEY_PARAM = "api_key";
            final String PAGE_PARAM = "page";

            SharedPreferences sharedPrefs =
                    PreferenceManager.getDefaultSharedPreferences(mContext);
            String sortOrder = sharedPrefs.getString(
                    mContext.getString(R.string.pref_sort_order_key),
                    mContext.getString(R.string.pref_sort_order_most_popular));
            Log.e(LOG_TAG, "Sort order: " + sortOrder);
            if (sortOrder.equals(mContext.getString(R.string.pref_sort_order_favourites))) {
                Cursor movieCursor = mContext.getContentResolver().query(
                        MovieContract.MovieEntry.CONTENT_URI,
                        MOVIE_COLUMNS,
                        null, null, null
                );
                if (movieCursor != null) {
                    MovieInfo[] resultMovieInfoItems = new MovieInfo[movieCursor.getCount()];
                    int i = 0;
                    Log.e(LOG_TAG, "Cursor count: " + movieCursor.getCount());
                    while (movieCursor.moveToNext()) {
                        Log.e(LOG_TAG, movieCursor.getString(COL_MOVIE_TITLE));
                        resultMovieInfoItems[i++] = new MovieInfo(movieCursor.getString(COL_MOVIE_ID),
                                movieCursor.getString(COL_MOVIE_POSTER_PATH),
                                movieCursor.getString(COL_MOVIE_TITLE),
                                movieCursor.getString(COL_MOVIE_OVERVIEW),
                                movieCursor.getDouble(COL_MOVIE_POPULARITY),
                                movieCursor.getDouble(COL_MOVIE_VOTE_AVERAGE),
                                movieCursor.getString(COL_MOVIE_RELEASE_DATE));
                    }
                    movieCursor.close();
                    return resultMovieInfoItems;
                }
            }

            String sort_by = "popularity.desc";
            if (sortOrder.equals(mContext.getString(R.string.pref_sort_order_top_rated))) {
                builtUri = Uri.parse(MOVIE_BASE_URL + "/movie/top_rated?").buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .appendQueryParameter(PAGE_PARAM, params[0])
                        .build();
            } else {

                builtUri = Uri.parse(MOVIE_BASE_URL + "/discover/movie?").buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, sort_by)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .appendQueryParameter(PAGE_PARAM, params[0])
                        .build();
            }

            URL url = new URL(builtUri.toString());

            // create the request to themoviedb, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line.concat("\n"));
            }

            if (buffer.length() == 0) {
                // Stream was empty. No point in parsing.
                return null;
            }
            movieJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the data, there's no point
            // to parse it.
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
            return getMovieDataFromJson(movieJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the movie list.
        return null;
    }

    @Override
    protected void onPostExecute(MovieInfo[] result) {
        if (result != null) {
            for(MovieInfo movieInfoItem: result) {
                mMovieAdapter.add(movieInfoItem);
            }
            // New data is back from the server. Hooray!
        }
    }
}