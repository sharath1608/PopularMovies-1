package com.android.popmovies.app.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by Asus1 on 9/19/2015.
 */
public class TestUriMatcher extends AndroidTestCase {
    private static final String MOVIE_ID = "42";

    // content://com.example.android.sunshine.app/weather"
    private static final Uri TEST_FAVORITE_URI = MoviesContract.FavoriteEntry.CONTENT_URI;
    private static final Uri TEST_TRAILER_URI = MoviesContract.TrailerEntry.buildTrailerswithID(MOVIE_ID);
    private static final Uri TEST_MOVIE_URI = MoviesContract.MoviesEntry.CONTENT_URI;

    /*
        Students: This function tests that your UriMatcher returns the correct integer value
        for each of the Uri types that our ContentProvider can handle.  Uncomment this when you are
        ready to test your UriMatcher.
     */
    public void testUriMatcher() {
        UriMatcher testMatcher = MovieProvider.buildUriMatcher();

        assertEquals("Error: The Movie URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_URI), MovieProvider.MOVIES);
        assertEquals("Error: The Favorites URI was matched incorrectly.",
                testMatcher.match(TEST_FAVORITE_URI), MovieProvider.FAVORITES);
        assertEquals("Error: The Trailers URI was matched incorrectly.",
                testMatcher.match(TEST_TRAILER_URI), MovieProvider.TRAILERS);
    }
}
