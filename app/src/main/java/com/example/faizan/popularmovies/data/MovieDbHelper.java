package com.example.faizan.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.faizan.popularmovies.data.MovieContract.MovieEntry;
import com.example.faizan.popularmovies.data.MovieContract.VideoEntry;
import com.example.faizan.popularmovies.data.MovieContract.ReviewEntry;

public class MovieDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "popular_movies.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieEntry.COLUMN_MOVIE_ID + " TEXT UNIQUE NOT NULL, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POPULARITY + " REAL NOT NULL, " +
                MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL " +
                " );";

        final String SQL_CREATE_MOVIE_VIDEO_TABLE = "CREATE TABLE " + VideoEntry.TABLE_NAME + " (" +
                VideoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                VideoEntry.COLUMN_MOVIE_VIDEO_ID + " TEXT UNIQUE NOT NULL, " +
                VideoEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                VideoEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                VideoEntry.COLUMN_SITE + " TEXT NOT NULL, " +
                VideoEntry.COLUMN_KEY + " TEXT NOT NULL, " +
                VideoEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + VideoEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry.COLUMN_MOVIE_ID + "));";

        final String SQL_CREATE_MOVIE_REVIEW_TABLE = "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                ReviewEntry._ID + " INTEGER PRIMARY KEY, " +
                ReviewEntry.COLUMN_MOVIE_REVIEW_ID + " TEXT UNIQUE NOT NULL, " +
                ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_URL + " TEXT NOT NULL, " +

                "FOREIGN KEY  (" + ReviewEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry.COLUMN_MOVIE_ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_VIDEO_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + VideoEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
