package com.android.popmovies.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import app.sunshine.android.example.com.popmovies.Trailer;

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
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE "+MoviesContract.MoviesEntry.TABLE_NAME+" (" +
                MoviesContract.MoviesEntry._ID + " INTEGER , "+
                MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, "+
                MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, "+
                MoviesContract.MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_RATING + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_RELEASE_YEAR + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_RUNTIME + " TEXT NOT NULL, "+
                MoviesContract.MoviesEntry.COLUMN_BACKDROP + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_POSTER + " TEXT NOT NULL,"+
                " PRIMARY KEY("+ MoviesContract.MoviesEntry._ID+","+ MoviesContract.MoviesEntry.COLUMN_MOVIE_ID+"));";

        final String SQL_CREATE_FAV_TABLE = "CREATE TABLE " + MoviesContract.FavoriteEntry.TABLE_NAME+ " (" +
                MoviesContract.FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MoviesContract.FavoriteEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL,"  +
                " FOREIGN KEY (" + MoviesContract.FavoriteEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MoviesContract.MoviesEntry.TABLE_NAME +" ("+ MoviesContract.MoviesEntry.COLUMN_MOVIE_ID+") ON DELETE CASCADE, " +
                "UNIQUE (" + MoviesContract.FavoriteEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " + MoviesContract.ReviewsEntry.TABLE_NAME+ " (" +
                MoviesContract.ReviewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MoviesContract.ReviewsEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL," +
                MoviesContract.ReviewsEntry.COLUMN_REVIEW_AUTHOR + " TEXT DEFAULT NULL," +
                MoviesContract.ReviewsEntry.COLUMN_REVIEW_TEXT + " TEXT DEFAULT NULL," +
                " FOREIGN KEY (" + MoviesContract.ReviewsEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MoviesContract.MoviesEntry.TABLE_NAME +" ("+ MoviesContract.MoviesEntry.COLUMN_MOVIE_ID+") ON DELETE CASCADE);";


        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + MoviesContract.TrailerEntry.TABLE_NAME+ " (" +
                MoviesContract.TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MoviesContract.TrailerEntry.COLUMN_TRAILER_ID + " TEXT NOT NULL," +
                MoviesContract.TrailerEntry.COLUMN_TRAILER_TITLE + " TEXT DEFAULT NULL," +
                MoviesContract.TrailerEntry.COLUMN_TRAILER_URI + " TEXT DEFAULT NULL,"+
                MoviesContract.TrailerEntry .COLUMN_MOVIE_ID + " INTEGER NOT NULL,"  +
                " FOREIGN KEY (" + MoviesContract.TrailerEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MoviesContract.MoviesEntry.TABLE_NAME +" ("+ MoviesContract.MoviesEntry.COLUMN_MOVIE_ID+") ON DELETE CASCADE);";

        final String SQL_CREATE_CAST_TABLE = "CREATE TABLE " + MoviesContract.CastEntry.TABLE_NAME+ " (" +
                MoviesContract.CastEntry.COLUMN_CAST_ID + " TEXT NOT NULL," +
                MoviesContract.CastEntry.COLUMN_CAST_NAME + " TEXT DEFAULT NULL," +
                MoviesContract.CastEntry.COLUMN_CAST_URI + " TEXT DEFAULT NULL,"+
                MoviesContract.CastEntry .COLUMN_MOVIE_ID + " INTEGER NOT NULL ,"  +
                " PRIMARY KEY ("+ MoviesContract.CastEntry.COLUMN_CAST_ID+","+ MoviesContract.CastEntry.COLUMN_MOVIE_ID+"),"+
                " FOREIGN KEY (" + MoviesContract.CastEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MoviesContract.MoviesEntry.TABLE_NAME +" ("+ MoviesContract.MoviesEntry.COLUMN_MOVIE_ID+") ON DELETE CASCADE);";


        // Create the tables
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_FAV_TABLE);
        db.execSQL(SQL_CREATE_REVIEW_TABLE);
        db.execSQL(SQL_CREATE_TRAILER_TABLE);
        db.execSQL(SQL_CREATE_CAST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.MoviesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.FavoriteEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.TrailerEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.ReviewsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.CastEntry.TABLE_NAME);
        onCreate(db);
    }
}
