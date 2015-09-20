package com.android.popmovies.app.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import com.android.popmovies.app.data.MoviesContract.MoviesEntry;
import com.android.popmovies.app.data.MoviesContract.FavoriteEntry;

/**
 * Created by Asus1 on 9/17/2015.
 */

public class MovieProvider extends ContentProvider{

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDBHelper mOpenHelper;

    static final int MOVIES = 100;
    static final int FAVORITES = 200;
    static final int FAVORITES_WITH_DETAILS = 201;
    private static final SQLiteQueryBuilder sFavoriteMovieDetailsQueryBuilder;
    private static final SQLiteQueryBuilder sFavoriteMoviesQueryBuilder;

    static{
        sFavoriteMovieDetailsQueryBuilder = new SQLiteQueryBuilder();

        sFavoriteMovieDetailsQueryBuilder.setTables(
                MoviesEntry.TABLE_NAME + " INNER JOIN " +
                       FavoriteEntry.TABLE_NAME +
                        " ON " + MoviesEntry.TABLE_NAME +
                        "." + MoviesEntry.COLUMN_MOVIE_ID +
                        "=" + FavoriteEntry.TABLE_NAME +
                        "." + FavoriteEntry.COLUMN_MOVIE_ID);
    }

    static{
        sFavoriteMoviesQueryBuilder = new SQLiteQueryBuilder();

        sFavoriteMoviesQueryBuilder.setTables(
                MoviesEntry.TABLE_NAME + " INNER JOIN " +
                        FavoriteEntry.TABLE_NAME +
                        " ON " + MoviesEntry.TABLE_NAME +
                        "." + MoviesEntry.COLUMN_MOVIE_ID +
                        "=" + FavoriteEntry.TABLE_NAME +
                        "." + FavoriteEntry.COLUMN_MOVIE_ID);
    }

    private static final String sFavoriteMovieSelection =
            MoviesEntry.TABLE_NAME +
                    "." + MoviesEntry.COLUMN_MOVIE_ID + " = ?";


    private Cursor getFavorites(Uri uri, String[] projection){
        // 1. SQlitedb 2.Projection (Which columns to display) 3. Column selection 4. Selection args 5.groupby 6.having 7. SortOrder

        String[] selectionArgs = null;
        String selection = null;
        String favId = FavoriteEntry.getMovieIdFromUri(uri);

        if(favId!=null){
            selectionArgs = new String[]{favId};
            selection = sFavoriteMovieSelection;
        }

        return sFavoriteMovieDetailsQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);
    }

    //TODO: Create tests under androidTest (com.android.popmovies.app.data) to test Uri matching
    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;
        uriMatcher.addURI(authority,MoviesEntry.PATH_MOVIES, MOVIES);
        uriMatcher.addURI(authority, FavoriteEntry.PATH_FAVORITES,FAVORITES);
        return uriMatcher;
    }
    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDBHelper(this.getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (sUriMatcher.match(uri)){
            case MOVIES:
                cursor = mOpenHelper.getReadableDatabase().query(MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case FAVORITES:
                cursor = getFavorites(uri, projection);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI"+uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match){
            case MOVIES:
                return MoviesEntry.CONTENT_TYPE;
            case FAVORITES:
                return FavoriteEntry.CONTENT_TYPE;
            case FAVORITES_WITH_DETAILS:
                return FavoriteEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI"+uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase sqLiteDatabase = mOpenHelper.getWritableDatabase();
        int uriMatch= sUriMatcher.match(uri);
        Uri returnUri = null;
        switch (uriMatch){

            // Insert in the movies table. Since only favorites are being added for now, add corresponding favorites to favotites table.
            case MOVIES:
                long movie_id = sqLiteDatabase.insert(MoviesEntry.TABLE_NAME,null,values);
                if(movie_id > 0){
                    returnUri = ContentUris.withAppendedId(uri,movie_id);
                }else{
                    throw new UnsupportedOperationException("Unknown URI"+uri);
                }
                break;

            case FAVORITES:
                long fav_id = sqLiteDatabase.insert(FavoriteEntry.TABLE_NAME,null,values);
                if(fav_id > 0){
                    returnUri = ContentUris.withAppendedId(uri,fav_id);
                }else{
                    throw new UnsupportedOperationException("Unknown URI"+uri);
                }
            }

        getContext().getContentResolver().notifyChange(uri,null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase = mOpenHelper.getWritableDatabase();
        int uriMatch= sUriMatcher.match(uri);
        int row_count;
        switch (uriMatch){
            // Delete only the favorites for now
            case FAVORITES:
                row_count = sqLiteDatabase.delete(FavoriteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI"+uri);
        }

        // Notify the listeners of content change.
        if(row_count>0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return row_count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
