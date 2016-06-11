package com.example.faizan.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {
        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                if (intent.hasExtra("posterPath")){
                    String url = "http://image.tmdb.org/t/p/w185" + intent.getStringExtra("posterPath");
                    Picasso.with(getContext()).load(url).into((ImageView)rootView.findViewById(R.id.detail_imageview));
                }
                if (intent.hasExtra("title")) {
                    ((TextView)rootView.findViewById(R.id.detail_title_textview))
                            .setText(intent.getStringExtra("title"));
                }

                if (intent.hasExtra("overview")) {
                    ((TextView)rootView.findViewById(R.id.detail_overview_textview))
                            .setText(intent.getStringExtra("overview"));
                }

                if (intent.hasExtra("popularity")) {
                    ((TextView)rootView.findViewById(R.id.detail_popularity_textview))
                            .setText("Popularity: " + Double.toString(intent.getDoubleExtra("popularity", 0)));
                }

                if (intent.hasExtra("voteAverage")) {
                    ((TextView)rootView.findViewById(R.id.detail_vote_average_textview))
                            .setText("Vote Average: " + intent.getDoubleExtra("voteAverage", 0));
                }

            }



            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.moviefragment, menu);
        }
    }
}
