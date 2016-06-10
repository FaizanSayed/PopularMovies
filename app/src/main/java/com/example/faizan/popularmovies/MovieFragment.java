package com.example.faizan.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

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
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment {

    private final String LOG_TAG = MovieFragment.class.getSimpleName();
    private ImageAdapter mMovieAdapter;

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mMovieAdapter =
                new ImageAdapter(getActivity(),
                        R.layout.list_item_movies,
                        R.id.list_item_movies_imageview,
                        new ArrayList<MovieInfo>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the GridView, and attach the adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(mMovieAdapter);

        return rootView;
    }

    private void updateMovieList() {
        FetchMovieListTask movieListTask = new FetchMovieListTask();
        movieListTask.execute();

    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovieList();
    }

    public class FetchMovieListTask extends AsyncTask<String, Void, MovieInfo[]> {
        private final String LOG_TAG = FetchMovieListTask.class.getSimpleName();

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
            String TMDB_PAGE = "page";
            String TMDB_RESULTS = "results";
            String TMDB_POSTER_PATH = "poster_path";
            String TMDB_ADULT = "false";
            String TMDB_OVERVIEW = "overview";
            String TMDB_RELEASE_DATE = "release_date";
            String TMDB_GENRE_IDS = "genre_ids";
            String TMDB_ID = "id";
            String TMDB_ORIGINAL_TITLE = "original_title";
            String TMDB_ORIGINAL_LANGUAGE = "original_language";
            String TMDB_TITLE = "title";
            String TMDB_BACKDROP_PATH = "backdrop_path";
            String TMDB_POPULARITY = "popularity";
            String TMDB_VOTE_COUNT = "vote_count";
            String TMDB_VIDEO = "video";
            String TMDB_VOTE_AVERAGE = "vote_average";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieList = movieJson.getJSONArray(TMDB_RESULTS);

            //String[] resultStrs = new String[numItems];
            MovieInfo[] resultMovieInfoItems = new MovieInfo[movieList.length()];

            for (int i = 0; i < movieList.length(); i++) {
                String poster_path;
                String title;
                String overview;
                double popularity;
                double vote_count;
                MovieInfo movieInfo;

                // Get the JSON object representing the movie list item.
                JSONObject movie_list_item = movieList.getJSONObject(i);
                poster_path = movie_list_item.getString(TMDB_POSTER_PATH);
                title = movie_list_item.getString(TMDB_ORIGINAL_TITLE);
                overview = movie_list_item.getString(TMDB_OVERVIEW);
                popularity = movie_list_item.getDouble(TMDB_POPULARITY);
                vote_count = movie_list_item.getDouble(TMDB_VOTE_COUNT);
                movieInfo = new MovieInfo(poster_path, title, overview, popularity, vote_count);
                resultMovieInfoItems[i] = movieInfo;
            }
            return resultMovieInfoItems;
        }

        @Override
        protected MovieInfo[] doInBackground(String... params) {

//            if (params.length == 0) {
//                return null;
//            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;


            try {
                // Construct the URL for the TheMovieDatabase query
                final String MOVIE_BASE_URL =
                        "https://api.themoviedb.org/3/discover/movie?";
                final String SORT_BY_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, "popularity.desc")
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // create the request to themoviedb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
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
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point
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
                mMovieAdapter.clear();
                for(MovieInfo movieInfoItem: result) {
                    mMovieAdapter.add(movieInfoItem);
                }
                // New data is back from the server. Hooray!
            }
        }
    }
}
