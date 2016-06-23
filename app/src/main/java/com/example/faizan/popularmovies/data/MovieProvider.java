package com.example.faizan.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MovieProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    static final int MOVIE = 100;
    static final int MOVIE_VIDEO = 200;
    static final int MOVIE_VIDEO_BY_MOVIE = 201;
    static final int MOVIE_REVIEW = 300;
    static final int MOVIE_REVIEW_BY_MOVIE = 301;

    private static final SQLiteQueryBuilder sMovieVideoByMovieQueryBuilder;

    static {
        sMovieVideoByMovieQueryBuilder = new SQLiteQueryBuilder();

        sMovieVideoByMovieQueryBuilder.setTables(
                MovieContract.VideoEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.MovieEntry.TABLE_NAME +
                        " ON " + MovieContract.VideoEntry.TABLE_NAME +
                        "." + MovieContract.VideoEntry.COLUMN_MOVIE_ID +
                        " = " + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID);
    }

    private static final SQLiteQueryBuilder sMovieReviewByMovieQueryBuilder;

    static {
        sMovieReviewByMovieQueryBuilder = new SQLiteQueryBuilder();

        sMovieReviewByMovieQueryBuilder.setTables(
                MovieContract.ReviewEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.MovieEntry.TABLE_NAME +
                        " ON " + MovieContract.ReviewEntry.TABLE_NAME +
                        "." + MovieContract.ReviewEntry.COLUMN_MOVIE_ID +
                        " = " + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID);
    }

    private static final String sMovieIdSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";

    private Cursor getMovieVideoByMovieId(Uri uri, String[] projection, String sortOrder) {
        String movieId = MovieContract.VideoEntry.getMovieIdFromUri(uri);
        String[] selectionArgs = new String[]{movieId};
        String selection = sMovieIdSelection;

        return sMovieVideoByMovieQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMovieReviewByMovieId(Uri uri, String[] projection, String sortOrder) {
        String movieId = MovieContract.ReviewEntry.getMovieIdFromUri(uri);
        String[] selectionArgs = new String[]{movieId};
        String selection = sMovieIdSelection;

        return sMovieReviewByMovieQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }
    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_VIDEO, MOVIE_VIDEO);
        matcher.addURI(authority, MovieContract.PATH_VIDEO + "/*", MOVIE_VIDEO_BY_MOVIE);

        matcher.addURI(authority,MovieContract.PATH_REVIEW, MOVIE_REVIEW);
        matcher.addURI(authority, MovieContract.PATH_REVIEW + "/*", MOVIE_REVIEW_BY_MOVIE);

        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE_VIDEO_BY_MOVIE:
                return MovieContract.VideoEntry.CONTENT_TYPE;
            case MOVIE_REVIEW_BY_MOVIE:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            case MOVIE_VIDEO:
                return MovieContract.VideoEntry.CONTENT_TYPE;
            case MOVIE_REVIEW:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch(sUriMatcher.match(uri)) {
            case MOVIE_VIDEO_BY_MOVIE: {
                retCursor = getMovieVideoByMovieId(uri, projection, sortOrder);
                break;
            }

            case MOVIE_REVIEW_BY_MOVIE: {
                retCursor = getMovieReviewByMovieId(uri, projection, sortOrder);
                break;
            }

            case MOVIE_VIDEO: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.VideoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case MOVIE_REVIEW: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.ReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case MOVIE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MOVIE_VIDEO: {
                long _id = db.insert(MovieContract.VideoEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.VideoEntry.buildMovieVideoUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            case  MOVIE_REVIEW: {
                long _id = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.ReviewEntry.buildMovieReviewUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (null == selection) selection = "1";
        switch (match){
            case MOVIE_VIDEO:
                rowsDeleted = db.delete(
                        MovieContract.VideoEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case MOVIE_REVIEW:
                rowsDeleted = db.delete(
                        MovieContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case MOVIE:
                rowsDeleted = db.delete(
                        MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        if (null == selection) selection = "1";
        switch (match){
            case MOVIE_VIDEO:
                rowsUpdated = db.update(
                        MovieContract.VideoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case MOVIE_REVIEW:
                rowsUpdated = db.update(
                        MovieContract.ReviewEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case MOVIE:
                rowsUpdated = db.update(
                        MovieContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount;
        switch (match) {
            case MOVIE_VIDEO:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value: values) {
                        long _id = db.insert(MovieContract.VideoEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case MOVIE_REVIEW:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value: values) {
                        long _id = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
