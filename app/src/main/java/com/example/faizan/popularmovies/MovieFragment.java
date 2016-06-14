package com.example.faizan.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;


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
    private EndlessScrollListener endlessScrollListener;
    private GridView gridView;

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.moviefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovieList();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        //gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        gridView.setAdapter(mMovieAdapter);
        endlessScrollListener = new EndlessScrollListener();
        gridView.setOnScrollListener(endlessScrollListener);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieInfo movie_item = mMovieAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("posterPath", movie_item.posterPath);
                intent.putExtra("title", movie_item.title);
                intent.putExtra("overview", movie_item.overview);
                intent.putExtra("popularity", movie_item.popularity);
                intent.putExtra("voteAverage", movie_item.voteAverage);
                intent.putExtra("releaseDate", movie_item.releaseDate);
                intent.putExtra("id", movie_item.id);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void updateMovieList() {
        FetchMovieListTask movieListTask = new FetchMovieListTask();
        mMovieAdapter.clear();
        endlessScrollListener = new EndlessScrollListener();
        gridView.setOnScrollListener(endlessScrollListener);
        movieListTask.execute(Integer.toString(1));

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

//            if (params.length == 0) {
//                return null;
//            }

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
                        PreferenceManager.getDefaultSharedPreferences(getActivity());
                String sortOrder = sharedPrefs.getString(
                        getString(R.string.pref_sort_order_key),
                        getString(R.string.pref_sort_order_most_popular));

                //Log.e(LOG_TAG, "sort order: " + sortOrder);
                String sort_by = "popularity.desc";
                if (sortOrder.equals(getString(R.string.pref_sort_order_top_rated))) {
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
                //mMovieAdapter.clear();
                for(MovieInfo movieInfoItem: result) {
                    mMovieAdapter.add(movieInfoItem);
                }
                // New data is back from the server. Hooray!
            }
        }
    }

    class EndlessScrollListener implements AbsListView.OnScrollListener {

        private int visibleThreshold = 5;
        private int currentPage = 0;
        private int previousTotal = 0;
        private boolean loading = true;

        public EndlessScrollListener() {
        }
        public EndlessScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                // I load the next page of gigs using a background task,
                // but you can call any function here.
                Log.e(LOG_TAG, "Current Page: " + currentPage);
                new FetchMovieListTask().execute(Integer.toString(currentPage + 1));
                loading = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
//            Log.e(LOG_TAG, "onScrollStateChanged called");
        }
    }
}
