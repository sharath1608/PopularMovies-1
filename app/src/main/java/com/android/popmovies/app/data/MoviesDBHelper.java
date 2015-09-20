package com.android.popmovies.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Asus1 on 9/12/2015.
 */
public class MoviesDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "movies.db";

    public MoviesDBHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE "+MoviesContract.MoviesEntry.TABLE_NAME+" ( " +
                MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " TEXT PRIMARY KEY NOT NULL, "+
                MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, "+
                MoviesContract.MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_RATING + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_RELEASE_YEAR + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_RUNTIME + " TEXT NOT NULL, "+
                MoviesContract.MoviesEntry.COLUMN_BACKDROP + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_POSTER + " TEXT NOT NULL);";

        final String SQL_CREATE_FAV_TABLE = "CREATE TABLE " + MoviesContract.FavoriteEntry.TABLE_NAME+ " (" +
                MoviesContract.FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MoviesContract.FavoriteEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL,"  +
                " FOREIGN KEY (" + MoviesContract.FavoriteEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MoviesContract.MoviesEntry.TABLE_NAME +" ("+ MoviesContract.MoviesEntry.COLUMN_MOVIE_ID+") ON DELETE CASCADE, " +
                "UNIQUE (" + MoviesContract.FavoriteEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

        // Create the tables
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_FAV_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.MoviesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.FavoriteEntry.TABLE_NAME);
        onCreate(db);
    }
}
