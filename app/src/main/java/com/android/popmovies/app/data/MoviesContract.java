package com.android.popmovies.app.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.graphics.Movie;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Sharath Koday on 9/12/2015.
 */
public class MoviesContract {

    public static final String CONTENT_AUTHORITY = "com.android.popmovies.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final class MoviesEntry implements BaseColumns{

        public static final String PATH_MOVIES = "movies";
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_MOVIE_TITLE = "movie_title";
        public static final String COLUMN_RELEASE_YEAR = "release_year";
        public static final String COLUMN_RATING= "movie_rating";
        public static final String COLUMN_RUNTIME = "movie_runtime";
        public static final String COLUMN_OVERVIEW = "movie_overview";
        public static final String COLUMN_POSTER = "movie_poster";
        public static final String COLUMN_BACKDROP = "movie_backdrop";

    }

    public static final class FavoriteEntry implements  BaseColumns {
        private static String id = null;
        public static final String PATH_FAVORITES = "favorites";
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();
        public static final String TABLE_NAME = "favorites";
        public static final String movieParam = "movie_id";
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;

        // This is a foreign key that is a primary key in the `movies` table. Every movie entry will have a corresponding movieID.
        public static final String COLUMN_MOVIE_ID = "movie_key";

        // Use this URI to fetch all details of the movie that was clicked on. The result must be a join between Movies and Favorites.
        public static Uri buildFavDetailWithID(String movieId) {
            return CONTENT_URI.buildUpon().appendQueryParameter(movieParam, movieId).build();
        }

        public static String getMovieIdFromUri(Uri uri) {
            id = uri.getQueryParameter(MoviesEntry.COLUMN_MOVIE_ID);
            return id;
        }
    }
}
