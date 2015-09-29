package com.android.popmovies.app.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import android.database.SQLException;
import android.widget.ProgressBar;

import com.android.popmovies.app.data.MoviesContract.MoviesEntry;
import com.android.popmovies.app.data.MoviesContract.FavoriteEntry;
import com.android.popmovies.app.data.MoviesContract.ReviewsEntry;
import com.android.popmovies.app.data.MoviesContract.TrailerEntry;
import com.android.popmovies.app.data.MoviesContract.CastEntry;

/**
 * Created by Asus1 on 9/17/2015.
 */

public class MovieProvider extends ContentProvider{

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static MoviesDBHelper mOpenHelper;

    static final int MOVIES = 100;
    static final int FAVORITES = 200;
    static final int REVIEWS = 300;
    static final int TRAILERS = 400;
    static final int CASTS = 500;

    private String LOG_TAG = getClass().getSimpleName();
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
    private static final String sReviewsSelection =
            ReviewsEntry.TABLE_NAME +
                    "." + ReviewsEntry.COLUMN_MOVIE_ID + " = ?";
    private static final String sTrailersSelection =
            TrailerEntry.TABLE_NAME +
                    "." + TrailerEntry.COLUMN_MOVIE_ID + " = ?";
    private static final String sCastSelection =
            CastEntry.TABLE_NAME +
                    "." + CastEntry.COLUMN_MOVIE_ID + " = ?";


    private Cursor getCasts(Uri uri){
        String id = MoviesEntry.getMovieIdFromUri(uri);
        Cursor c = null;
        if(id!=null && id.length()>0) {
            c =  mOpenHelper.getReadableDatabase().query(CastEntry.TABLE_NAME,
                    null,
                    sCastSelection,
                    new String[]{id},
                    null,
                    null,
                    null);
        }else{
            Log.v(LOG_TAG,"Error getting casts");
        }
        return c;
    }

    private Cursor getReviews(Uri uri){
        String id = MoviesEntry.getMovieIdFromUri(uri);
        Cursor c = null;
        if(id!=null && id.length()>0) {
            c =  mOpenHelper.getReadableDatabase().query(ReviewsEntry.TABLE_NAME,
                    null,
                    sReviewsSelection,
                    new String[]{id},
                    null,
                    null, null);
        }else{
            Log.v(LOG_TAG,"Error getting reviews");

        }
        return c;
    }

    private Cursor getTrailers(Uri uri){
        String id = MoviesEntry.getMovieIdFromUri(uri);
        Cursor c = mOpenHelper.getReadableDatabase().query(TrailerEntry.TABLE_NAME,
                    null,
                    sTrailersSelection,
                    new String[]{id},
                    null,
                    null,
                    null);
        return c;
    }

    private Cursor getFavorites(Uri uri, String[] projection){
        // 1. SQlitedb 2.Projection (Which columns to display) 3. Column selection 4. Selection args 5.groupby 6.having 7. SortOrder

        String[] selectionArgs = null;
        String selection = null;
        String favId = MoviesEntry.getMovieIdFromUri(uri);

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
        uriMatcher.addURI(authority, ReviewsEntry.PATH_REVIEWS,REVIEWS);
        uriMatcher.addURI(authority,TrailerEntry.PATH_TRAILERS,TRAILERS);
        uriMatcher.addURI(authority,CastEntry.PATH_CASTS,CASTS);
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
            case REVIEWS:
                cursor = getReviews(uri);
                break;
            case TRAILERS:
                cursor = getTrailers(uri);
                break;
            case CASTS:
                cursor = getCasts(uri);
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
            case TRAILERS:
                return TrailerEntry.CONTENT_ITEM_TYPE;
            case REVIEWS:
                return ReviewsEntry.CONTENT_ITEM_TYPE;
            case CASTS:
                return CastEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI"+uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) throws SQLException {
        final SQLiteDatabase sqLiteDatabase = mOpenHelper.getWritableDatabase();
        int uriMatch= sUriMatcher.match(uri);
        String tableName;
        Uri returnUri;
        switch (uriMatch){

            // Insert in the movies table. Since only favorites are being added for now, add corresponding favorites to favotites table.
            case MOVIES:
                tableName = MoviesEntry.TABLE_NAME;
                break;
            case FAVORITES:
                tableName = FavoriteEntry.TABLE_NAME;
                break;
            case TRAILERS:
                tableName = TrailerEntry.TABLE_NAME;
                break;
            case REVIEWS:
                tableName = ReviewsEntry.TABLE_NAME;
                break;
            case CASTS:
                tableName = CastEntry.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("Insert : Unknown URI"+uri);
            }

        long _id = sqLiteDatabase.insert(tableName,null,values);
        if(_id > 0){
            returnUri = ContentUris.withAppendedId(uri,_id);
        }else{
            throw new SQLException("Insert: Error inserting rows at "+uri);
        }

        getContext().getContentResolver().notifyChange(uri,null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) throws SQLException{
        final SQLiteDatabase sqLiteDatabase = mOpenHelper.getWritableDatabase();
        int uriMatch= sUriMatcher.match(uri);
        int row_count;
        String tableName;
        switch (uriMatch){
            // Delete only the favorites for now
            case FAVORITES:
                tableName = FavoriteEntry.TABLE_NAME;
                break;
            case REVIEWS:
                tableName = ReviewsEntry.TABLE_NAME;
                break;
            case TRAILERS:
                tableName = TrailerEntry.TABLE_NAME;
                break;
            case CASTS:
                tableName = CastEntry.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("delete : Unknown URI"+uri);
        }

        row_count = sqLiteDatabase.delete(tableName,selection,selectionArgs);

        // Notify the listeners of content change.
        if(row_count>0){
            getContext().getContentResolver().notifyChange(uri,null);
        } else {
            throw new SQLException("delete: Error deleting rows at "+uri);
        }
        return row_count;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) throws  SQLException{
        String tableName;
        final SQLiteDatabase sqLiteDatabase = mOpenHelper.getWritableDatabase();
        int uriMatch= sUriMatcher.match(uri);
        int row_count = 0;
        switch (uriMatch) {
            case REVIEWS:
                tableName = ReviewsEntry.TABLE_NAME;
                break;
            case TRAILERS:
                tableName = TrailerEntry.TABLE_NAME;
                break;
            case CASTS:
                tableName = CastEntry.TABLE_NAME;
                break;
            default:
                return super.bulkInsert(uri, values);
        }

        // Lets just assume we are inserting rows that fall in the "int" range.
        for(ContentValues cv : values){
            row_count = (int) sqLiteDatabase.insert(tableName,null,cv);
            if(row_count < 0){
                throw new SQLException("bulkInsert: Error inserting row at" + uri);
            }else{
                row_count++;
            }
        }

        getContext().getContentResolver().notifyChange(uri,null);
        return values.length;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
