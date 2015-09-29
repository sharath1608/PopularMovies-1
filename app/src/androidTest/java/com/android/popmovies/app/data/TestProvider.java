package com.android.popmovies.app.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by Asus1 on 9/18/2015.
 */
public class TestProvider extends AndroidTestCase{

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }


    public void deleteAllRecordsFromDB(){
        MoviesDBHelper dbHelper = new MoviesDBHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(MoviesContract.MoviesEntry.TABLE_NAME,
                null,
                null);
        db.delete(MoviesContract.FavoriteEntry.TABLE_NAME,
                null,
                null);

        db.close();

    }


    public void testInsertReadProvider() {
        ContentValues movieValues = TestUtilities.createMovieRecord(mContext);

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.TestContentObserver.getTestContentObserver();
        getContext().getContentResolver().registerContentObserver(MoviesContract.MoviesEntry.CONTENT_URI, true, tco);
        Uri movieURI = getContext().getContentResolver().insert(MoviesContract.MoviesEntry.CONTENT_URI, movieValues);

        // Did our content observer get called?  Students:  If this fails, your insert location
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        getContext().getContentResolver().unregisterContentObserver(tco);

        long movieRowID = ContentUris.parseId(movieURI);

        // Verify we got a row back.
        assertTrue(movieRowID != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor MovieCursor = getContext().getContentResolver().query(
                MoviesContract.MoviesEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating LocationEntry.",
                MovieCursor, movieValues);

        ContentValues favContentValues = TestUtilities.createFavsRecord();
        // The TestContentObserver is a one-shot class

        tco = TestUtilities.TestContentObserver.getTestContentObserver();

        getContext().getContentResolver().registerContentObserver(MoviesContract.FavoriteEntry.CONTENT_URI, true, tco);

        Uri FavInsertUri = getContext().getContentResolver()
                .insert(MoviesContract.FavoriteEntry.CONTENT_URI, favContentValues);
        assertTrue(FavInsertUri != null);

        // Did our content observer get called?  Students:  If this fails, your insert weather
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        getContext().getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor favCursor = getContext().getContentResolver().query(
                MoviesContract.FavoriteEntry.CONTENT_URI, // Table to Query
                new String[]{MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE,
                        MoviesContract.MoviesEntry.COLUMN_POSTER}, // leaving "columns" null just returns all the columns
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry insert.",
                favCursor, TestUtilities.favoriteMovieValue());

        // Get the joined Weather and Location data with a start date
        Cursor favDetailCursor = getContext().getContentResolver().query(
                MoviesContract.FavoriteEntry.buildFavDetailWithID(TestUtilities.MOVIE_ID),
                new String[]{MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE,
                        MoviesContract.MoviesEntry.COLUMN_RATING,
                        MoviesContract.MoviesEntry.COLUMN_RELEASE_YEAR,
                        MoviesContract.MoviesEntry.COLUMN_RUNTIME,
                        MoviesContract.MoviesEntry.COLUMN_OVERVIEW,
                        MoviesContract.MoviesEntry.COLUMN_POSTER,
                        MoviesContract.MoviesEntry.COLUMN_BACKDROP}, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry insert.",
                favDetailCursor, TestUtilities.favoriteDetailValue());

        ContentValues[] trailerValues = TestUtilities.createTrailerRecord(mContext);

        int res = getContext().getContentResolver()
                .bulkInsert(MoviesContract.TrailerEntry.buildTrailerswithID(TestUtilities.MOVIE_ID), trailerValues);

        assertTrue("Error inserting trailers",res>0);

        Cursor trailerCursor = getContext().getContentResolver().query(
                MoviesContract.TrailerEntry.buildTrailerswithID(TestUtilities.MOVIE_ID), // Table to Query
                new String[]{MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE,
                        MoviesContract.MoviesEntry.COLUMN_POSTER}, // leaving "columns" null just returns all the columns
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        if(trailerCursor.moveToFirst()){
            int index = 0;
            do{
                TestUtilities.validateCursor("testInsertReadProvider. Error validating TrailerEntry insert.",
                        trailerCursor, trailerValues[index++]);
            }while(trailerCursor.moveToNext());
        }


    }

    public void deleteAllRecords() {
        deleteAllRecordsFromDB();
    }

    public void testProviderRegistry() {
        PackageManager pm = getContext().getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(getContext().getPackageName(),
                MovieProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MovieProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MoviesContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MoviesContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MovieProvider not registered at " + getContext().getPackageName(),
                    false);
        }
    }
}
