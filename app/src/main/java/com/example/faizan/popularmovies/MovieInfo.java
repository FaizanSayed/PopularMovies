package com.example.faizan.popularmovies;

public class MovieInfo {
    String posterPath;
    String title;
    String overview;
    double popularity;
    double voteAverage;

    public MovieInfo(String movie_poster_path, String movie_title, String movie_overview, double movie_popularity, double movie_vote_average) {
        posterPath = movie_poster_path;
        title = movie_title;
        overview = movie_overview;
        popularity = movie_popularity;
        voteAverage = movie_vote_average;
    }
}
