package com.android.popmovies.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;
import com.android.popmovies.app.utils.PollingCheck;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Set;

import app.sunshine.android.example.com.popmovies.R;

/**
 * Created by Asus1 on 9/12/2015.
 */
public class TestUtilities extends AndroidTestCase{

    private static  String overViewTestString_1 = "This is an overview";
    private static String yearTestString_1 = "2015";
    private static String ratingTestString_1 = "4.5";
    private static String titleTestString_1 = "fake title";
    private static String timeTestString_1 = "145";
    private static String posterID_1 = "t90Y3G8UGQp0f0DrP60wRu9gfrH";
    private static String backdropID_1 = "tbhdm8UJAb4ViCTsulYFL3lxMCd";
    final static String MOVIE_ID = "42";

    static ContentValues createMovieRecord(Context mContext) {

        //Movie #1
        ContentValues movieValues = new ContentValues();
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,MOVIE_ID);
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW,overViewTestString_1);
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE,titleTestString_1);
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_YEAR,yearTestString_1);
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_RATING,ratingTestString_1);
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_RUNTIME,timeTestString_1);
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER, posterID_1);
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_BACKDROP,backdropID_1);

        return movieValues;
    }

    static ContentValues[] createTrailerRecord(Context mContext){

        ContentValues[] trailerValues = new ContentValues[2];
        ContentValues value1 = new ContentValues();
        value1.put(MoviesContract.TrailerEntry.COLUMN_TRAILER_ID,"this_id");
        value1.put(MoviesContract.TrailerEntry.COLUMN_TRAILER_TITLE,"this_title");
        value1.put(MoviesContract.TrailerEntry.COLUMN_TRAILER_URI,"this_URI");
        value1.put(MoviesContract.TrailerEntry.COLUMN_MOVIE_ID,MOVIE_ID);

        ContentValues values2 = new ContentValues();
        values2.put(MoviesContract.TrailerEntry.COLUMN_TRAILER_ID,"this_id_2");
        values2.put(MoviesContract.TrailerEntry.COLUMN_TRAILER_TITLE,"this_title_2");
        values2.put(MoviesContract.TrailerEntry.COLUMN_TRAILER_URI,"this_URI_2");
        values2.put(MoviesContract.TrailerEntry.COLUMN_MOVIE_ID,MOVIE_ID);
        trailerValues[0] = value1;
        trailerValues[1] = values2;
        return trailerValues;
    }

    static ContentValues createFavsRecord(){
        ContentValues favsValues = new ContentValues();
        favsValues.put(MoviesContract.FavoriteEntry.COLUMN_MOVIE_ID,42);
        return favsValues;
    }

    static ContentValues favoriteDetailValue(){

            ContentValues movieValues = new ContentValues();
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW,overViewTestString_1);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE,titleTestString_1);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_YEAR,yearTestString_1);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_RATING,ratingTestString_1);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_RUNTIME,timeTestString_1);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER, posterID_1);
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_BACKDROP,backdropID_1);

            return movieValues;
    }


    static ContentValues favoriteMovieValue(){
        ContentValues cv = new ContentValues();
        cv.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE,titleTestString_1);
        cv.put(MoviesContract.MoviesEntry.COLUMN_POSTER, posterID_1);
        return cv;
    }

    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

}
