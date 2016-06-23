package com.example.faizan.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.example.faizan.popularmovies.app";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_VIDEO = "video";
    public static final String PATH_REVIEW = "review";

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;


        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final String COLUMN_POSTER_PATH = "poster_path";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_OVERVIEW = "overview";

        public static final String COLUMN_POPULARITY = "popularity";

        public static final String COLUMN_VOTE_AVERAGE = "average";

        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class VideoEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIDEO).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEO;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEO;

        public static final String TABLE_NAME = "video";

        public static final String COLUMN_MOVIE_VIDEO_ID = "movie_video_id";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final String COLUMN_KEY = "key";

        public static final String COLUMN_NAME   = "name";

        public static final String COLUMN_SITE = "site";

        public static final String COLUMN_TYPE = "type";

        public static Uri buildMovieVideoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieVideoUribyMovie(String movie_id) {
            return CONTENT_URI.buildUpon().appendPath(movie_id).build();
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class ReviewEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        public static final String TABLE_NAME = "review";

        public static final String COLUMN_MOVIE_REVIEW_ID = "movie_review_id";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final String COLUMN_AUTHOR = "author";

        public static final String COLUMN_CONTENT = "content";

        public static final String COLUMN_URL = "url";

        public static Uri buildMovieReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieReviewUribyMovie(String movie_id) {
            return CONTENT_URI.buildUpon().appendPath(movie_id).build();
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }
}
