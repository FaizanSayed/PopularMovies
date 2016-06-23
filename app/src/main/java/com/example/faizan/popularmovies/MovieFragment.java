package com.example.faizan.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;


import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment {

    private final String LOG_TAG = MovieFragment.class.getSimpleName();
    private MovieInfoAdapter mMovieAdapter;
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
                new MovieInfoAdapter(getActivity(),
                        R.layout.list_item_movies,
                        R.id.list_item_movies_imageview,
                        new ArrayList<MovieInfo>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the GridView, and attach the adapter to it.
        gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
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
        FetchMovieListTask movieListTask = new FetchMovieListTask(getActivity(), mMovieAdapter);
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
                new FetchMovieListTask(getActivity(), mMovieAdapter).execute(Integer.toString(currentPage + 1));
                loading = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }
}
