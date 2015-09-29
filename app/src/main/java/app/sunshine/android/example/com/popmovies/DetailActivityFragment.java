package app.sunshine.android.example.com.popmovies;


import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.popmovies.app.data.MoviesContract;
import com.android.popmovies.app.data.MoviesContract.FavoriteEntry;
import com.android.popmovies.app.data.MoviesContract.MoviesEntry;
import com.android.popmovies.app.data.MoviesContract.ReviewsEntry;
import com.android.popmovies.app.data.MoviesContract.TrailerEntry;
import com.android.popmovies.app.data.MoviesContract.CastEntry;
import com.android.popmovies.app.data.MoviesDBHelper;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.melnykov.fab.FloatingActionButton;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private String movieId;
    private Toast noConnectToast;
    private TextView movieTitleView;
    private Target loadTarget;
    private ProgressBar progressBar;
    private final String API_REQ_STRING = "api_key";
    private final String APPEND_TO_RESPONSE = "append_to_response";
    private List<String> fullReviewList;
    private boolean isReviewInDB;
    private boolean isTrailerInDB;
    private CastViewAdapter castAdapter;
    private ArrayList<CastViewObject> castObjectArray;
    private RequestQueue mRequestQueue;
    private ShareActionProvider mShareActionProvider;
    private DetailMovieData thisMovieData;
    private MenuItem shareMenuItem;
    private String LOG_TAG;
    private boolean isFavorite;
    private boolean isMovieInDB;
    private boolean isCastInDB;
    private FloatingActionButton fab;
    private Toast favToast;
    private Cursor movieCursor;
    private Cursor reviewCursor;
    private Cursor trailerCursor;
    private Cursor castCursor;

    public static DetailActivityFragment newInstance(String id) {
        DetailActivityFragment fragment = new DetailActivityFragment();
        Bundle args = new Bundle();
        args.putString(Intent.EXTRA_TEXT, id);
        fragment.setArguments(args);
        return fragment;
    }

    // For testing purposes only!
    public void deleteAllRecordsFromDB() {
        MoviesDBHelper dbHelper = new MoviesDBHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(MoviesContract.MoviesEntry.TABLE_NAME,
                null,
                null);
        db.delete(MoviesContract.FavoriteEntry.TABLE_NAME,
                null,
                null);

        db.close();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        //deleteAllRecordsFromDB();
        isTrailerInDB = false;
        isCastInDB = false;
        isReviewInDB = false;
        isMovieInDB = false;
        mRequestQueue = Volley.newRequestQueue(getActivity());
        thisMovieData = new DetailMovieData();
        movieId = getArguments().getString(Intent.EXTRA_TEXT);
        castAdapter = new CastViewAdapter(new ArrayList<CastViewObject>());
        fab = (FloatingActionButton) view.findViewById(R.id.movie_fav_fab);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar_detail);
        movieCursor = checkMovieInDB(movieId);
        checkMovieFavorited(movieId);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.cast_list_view);

        if (isFavorite) {
            fab.setBackgroundColor(getResources().getColor(R.color.fab_ripple));
        }

        //  set click listener for floating action bar.
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertFavorite(thisMovieData);
            }
        });

        LinearLayoutManager castLayoutManager = new LinearLayoutManager(getActivity());
        castLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        castLayoutManager.requestSimpleAnimationsInNextLayout();
        recyclerView.setLayoutManager(castLayoutManager);

        setHasOptionsMenu(true);
        castAdapter.setOnItemClickListener(new CastViewAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View v, int position) {
                CastViewObject chosenCast = thisMovieData.getCasts().get(position);
                mRequestQueue.add(getPersonRequest(chosenCast.getCastId()));
            }
        });
        recyclerView.setAdapter(castAdapter);
        return view;
    }

    public void saveMovieDetails() {
        LOG_TAG = getClass().getSimpleName();
        if (!isMovieInDB) {
            ContentValues movieValues = new ContentValues();
            ContentValues favValues = new ContentValues();
            movieValues.put(MoviesEntry.COLUMN_MOVIE_ID, thisMovieData.getMovieID());
            movieValues.put(MoviesEntry.COLUMN_MOVIE_TITLE, thisMovieData.getTitle());
            movieValues.put(MoviesEntry.COLUMN_RUNTIME, thisMovieData.getDuration());
            movieValues.put(MoviesEntry.COLUMN_BACKDROP, thisMovieData.getBackdropUrl());
            movieValues.put(MoviesEntry.COLUMN_POSTER, thisMovieData.getImageUrl());
            movieValues.put(MoviesEntry.COLUMN_RELEASE_YEAR, thisMovieData.getYear());
            movieValues.put(MoviesEntry.COLUMN_OVERVIEW, thisMovieData.getDescription());
            movieValues.put(MoviesEntry.COLUMN_RATING, thisMovieData.getRating());
            favValues.put(FavoriteEntry.COLUMN_MOVIE_ID, movieId);

            Uri movieUri = getActivity().getContentResolver().insert(MoviesEntry.CONTENT_URI, movieValues);
            long movieRowID = ContentUris.parseId(movieUri);
            if (!(movieRowID > 0)) {
                Log.e(LOG_TAG, "Error inserting favorite to database");
            } else {
                isMovieInDB = true;
            }
        }

        if (!isTrailerInDB) {
            ContentValues[] trailerValues = new ContentValues[thisMovieData.getTrailers().length];
            int index = 0;
            for (Trailer trailer : thisMovieData.getTrailers()) {
                ContentValues value = new ContentValues();
                value.put(TrailerEntry.COLUMN_MOVIE_ID, movieId);
                value.put(TrailerEntry.COLUMN_TRAILER_TITLE, trailer.getTrailerName());
                value.put(TrailerEntry.COLUMN_TRAILER_URI, trailer.getSource());
                value.put(TrailerEntry.COLUMN_TRAILER_ID, trailer.getTrailerId());
                trailerValues[index++] = value;
            }
            getActivity().getContentResolver().bulkInsert(TrailerEntry.buildTrailerswithID(movieId), trailerValues);
        }

        if (!isCastInDB) {
            List<CastViewObject> casts = thisMovieData.getCasts();
            ContentValues[] castValues = new ContentValues[casts.size()];
            int index = 0;
            for (CastViewObject cast : casts) {
                ContentValues value = new ContentValues();
                value.put(CastEntry.COLUMN_CAST_ID, cast.getCastId());
                value.put(CastEntry.COLUMN_CAST_NAME, cast.getCastName());
                value.put(CastEntry.COLUMN_CAST_URI, cast.getCastImageUrl());
                value.put(CastEntry.COLUMN_MOVIE_ID, thisMovieData.getMovieID());
                castValues[index++] = value;
            }
            getActivity().getContentResolver().bulkInsert(CastEntry.buildCastWithID(movieId), castValues);
        }
    }


//    public void bulkInsertDetails(Object[] detailArray,String[] cols,Class klass){
//        ContentValues[] detailValues = new ContentValues[detailArray.length];
//        Field[] fields = klass.getDeclaredFields();
//        for(int i=0;i<detailArray.length;i++){
//            ContentValues values = new ContentValues();
//            for()
//            detailValues[i] = values;
//        }
//        getActivity().getContentResolver().bulkInsert(TrailerEntry.buildTrailerswithID(movieId), detailValues);
//    }

    public void saveReviewDetails() {
        if (!isReviewInDB) {
            ContentValues[] reviewValues = new ContentValues[thisMovieData.getReviews().length];
            int index = 0;
            Review[] movieReviews = thisMovieData.getReviews();
            for (Review review : movieReviews) {
                ContentValues values = new ContentValues();
                values.put(ReviewsEntry.COLUMN_REVIEW_AUTHOR, review.getAuthor());
                values.put(ReviewsEntry.COLUMN_MOVIE_ID, movieId);
                values.put(ReviewsEntry.COLUMN_REVIEW_TEXT, review.getReviewText());
                reviewValues[index++] = values;
            }
            getActivity().getContentResolver().bulkInsert(ReviewsEntry.buildReviewsWithID(movieId), reviewValues);
        }
    }

    public void insertFavorite(DetailMovieData movieData) {
        LOG_TAG = getClass().getSimpleName();
        String toastString = "";

        if (isMovieInDB) {

            // insert movie as favorite if not already present.
            if (!isFavorite) {
                ContentValues favValues = new ContentValues();
                favValues.put(FavoriteEntry.COLUMN_MOVIE_ID, movieData.getMovieID());
                Uri favUri = getActivity().getContentResolver().insert(FavoriteEntry.CONTENT_URI, favValues);

                long favRowID = ContentUris.parseId(favUri);
                if (!(favRowID > 0)) {
                    Log.e(LOG_TAG, "Error inserting favorite to database");
                } else {
                    fab.setBackgroundColor(getResources().getColor(R.color.fab_ripple));
                    if (favToast != null) {
                        favToast.cancel();
                    }
                    toastString = getString(R.string.fav_add_success);
                    isFavorite = true;
                }

                // If already marked as favorite, remove from DB.
            } else {

                getActivity().getContentResolver().delete(FavoriteEntry.CONTENT_URI,
                        FavoriteEntry.COLUMN_MOVIE_ID + "= ?",
                        new String[]{movieData.getMovieID()});
                if (favToast != null) {
                    favToast.cancel();
                }
                toastString = getString(R.string.fav_remove_success);
                fab.setBackgroundColor(getResources().getColor(R.color.fab_normal));
                isFavorite = false;
            }
        } else {
            toastString = getString(R.string.fav_add_failure);
        }
        favToast = Toast.makeText(getActivity(), toastString, Toast.LENGTH_SHORT);
        favToast.setGravity(Gravity.BOTTOM, 0, -50);
        favToast.show();
    }

    public void checkMovieFavorited(String id) {

        if (isMovieInDB) {
            Cursor cursor = getActivity().getContentResolver().query(FavoriteEntry.buildFavDetailWithID(id),
                    null,
                    null,
                    null,
                    null);
            isFavorite = cursor.moveToFirst();
        }
    }

    public Cursor checkMovieInDB(String id) {
        Cursor cursor = getActivity().getContentResolver().query(MoviesEntry.CONTENT_URI,
                null,
                MoviesEntry.TABLE_NAME + "." + MoviesEntry.COLUMN_MOVIE_ID + " = ? ",
                new String[]{id},
                null);

        checkReviewInDB();
        checkTrailerInDB();
        checkCastInDB();

        isMovieInDB = cursor.moveToFirst();
        return cursor;
    }

    public void checkReviewInDB() {
        reviewCursor = getActivity().getContentResolver().query(ReviewsEntry.buildReviewsWithID(movieId),
                null,
                null,
                null,
                null);
        ;
        isReviewInDB = reviewCursor.moveToFirst();
    }

    public void checkTrailerInDB() {
        trailerCursor = getActivity().getContentResolver().query(TrailerEntry.buildTrailerswithID(movieId),
                null,
                null,
                null,
                null);
        isTrailerInDB = trailerCursor.moveToFirst();
    }

    public void checkCastInDB() {
        castCursor = getActivity().getContentResolver().query(CastEntry.buildCastWithID(movieId),
                null,
                null,
                null,
                null);

        isCastInDB = castCursor.moveToFirst();
    }

    @Override
    public void onStart() {
        super.onStart();

        // If the movie details are already in memory, just set the views.
        if (thisMovieData.getMovieID() == null) {
            // if movie is in DB, fetch it else make a HTTP request.

            if (isMovieInDB) {
                readDetailsFromCursor(movieCursor);
            } else {
                requestMovieDetails();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (noConnectToast != null)
            noConnectToast.cancel();
    }

    public JsonObjectRequest getBasicInfoRequest() {
        final String apiKey = getString(R.string.api_key);
        final String DETAIL_BASE_URI = getString(R.string.details_base_path);
        String appendAttr = getString(R.string.appendAttr);
        Uri baseRequestUri = Uri.parse(DETAIL_BASE_URI).buildUpon().appendPath(movieId)
                .appendQueryParameter(API_REQ_STRING, apiKey).appendQueryParameter(APPEND_TO_RESPONSE, appendAttr).build();

        final String LOG_TAG = getClass().getSimpleName();
        JsonObjectRequest primaryJsonReq = new JsonObjectRequest(Request.Method.GET, baseRequestUri.toString(), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {
                    String posterSize = getString(R.string.detail_poster_size);
                    parseDetailsFromJson(response.toString(), posterSize);
                    saveMovieDetails();
                    setFragmentViews();
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error reading the response");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "Error getting response for the JSON request");
            }
        });
        return primaryJsonReq;
    }

    public JsonObjectRequest getReviewInfoRequest() {
        final String REVIEW_TAG = "reviews";
        final String apiKey = getString(R.string.api_key);
        final String DETAIL_BASE_URI = getString(R.string.details_base_path);


        Uri reviewRequestUri = Uri.parse(DETAIL_BASE_URI).buildUpon().appendPath(movieId).appendPath(REVIEW_TAG)
                .appendQueryParameter(API_REQ_STRING, apiKey).build();
        final String LOG_TAG = getClass().getSimpleName();


        JsonObjectRequest reviewJasonRequest = new JsonObjectRequest(Request.Method.GET, reviewRequestUri.toString(), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {
                    parseReviewDetails(response);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error reading the response");
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "Error getting response for the JSON request");
            }
        });

        return reviewJasonRequest;
    }


    public void parseReviewDetails(JSONObject JsonResp) throws JSONException {
        final String RESULT_TAG = "results";
        final String CONTENT_TAG = "content";
        final String AUTHOR_TAG = "author";
        int reviewStart = 0;
        int reviewEnd = 150;
        fullReviewList = new ArrayList<>();
        JSONArray resultsArray = JsonResp.getJSONArray(RESULT_TAG);
        Review[] reviewArray = new Review[resultsArray.length()];

        // Parse review content and add to reviewList to be displayed on the detail screen and fullReviewList which will be displayed in a dialog.
        for (int i = 0; i < resultsArray.length(); i++) {
            Review reviewObject = new Review();
            JSONObject result = resultsArray.getJSONObject(i);
            String reviewString = result.getString(CONTENT_TAG);
            String author = result.getString(AUTHOR_TAG);
            reviewEnd = reviewEnd > reviewString.length() ? reviewString.length() : reviewEnd;
            fullReviewList.add("\"" + reviewString + "\"" + " -" + author);
            reviewObject.setReviewText("\"" + reviewString.substring(reviewStart, reviewEnd) + "..." + "\"" + " -" + author);
            reviewObject.setAuthor(author);
            reviewArray[i] = reviewObject;
        }

        if (resultsArray.length() == 0) {
            Review reviewObject = new Review();
            reviewObject.setReviewText(getString(R.string.no_review_text));
            reviewArray = new Review[]{reviewObject};
        }

        // While reviewArray contains data for both scenarios where reviews exist and not, text is saved only when it exists.
        thisMovieData.setReviews(reviewArray);

        if (resultsArray.length() > 0) {
            saveReviewDetails();
        }
    }

    public void setReviewViews() {
        Review[] reviewObjects = thisMovieData.getReviews();
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.review_layout);
        for (int pos = 0; pos < reviewObjects.length; pos++) {
            final int finalPos = pos;
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.review_item_layout, null);
            TextView reviewText = (TextView) v.findViewById(R.id.movie_review_text);

            reviewText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String reviewPos = fullReviewList.get(finalPos);
                    extendedInfoPopUp(reviewPos,getString(R.string.review));
                }
            });

            reviewText.setText(reviewObjects[pos].getReviewText());
            layout.addView(v);
        }
    }

    // Launch another volley request to fetch cast Id in order to get imdb ID subsequently.
    public JsonObjectRequest getPersonRequest(String personId) {
        final String PERSON_TAG = "person";
        final String apiKey = getString(R.string.api_key);
        final String BASE_URI = getString(R.string.tmdb_base_uri);
        Uri reviewRequestUri = Uri.parse(BASE_URI).buildUpon().appendPath(PERSON_TAG).appendPath(personId)
                .appendQueryParameter(API_REQ_STRING, apiKey).build();
        final String LOG_TAG = getClass().getSimpleName();

        final JsonObjectRequest personJsonRequest = new JsonObjectRequest(Request.Method.GET, reviewRequestUri.toString(), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    String imdbId = fetchImdbId(response.toString());
                    if (!imdbId.equals("")) {
                        Uri imdbPage = Uri.parse(getString(R.string.imdb_base_uri) + imdbId);
                        Intent mIntent = new Intent(Intent.ACTION_VIEW, imdbPage);
                        if (mIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(mIntent);
                        }
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.no_detals_text), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error reading the response");
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "Error getting response for the JSON request");
            }
        });
        return personJsonRequest;
    }

    public String fetchImdbId(String JsonRespString) throws JSONException {
        JSONObject jsonObject = new JSONObject(JsonRespString);
        return jsonObject.getString(getString(R.string.imdbId));
    }

    public void requestMovieDetails() {
        mRequestQueue.add(getBasicInfoRequest());
        mRequestQueue.add(getReviewInfoRequest());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        shareMenuItem = menu.findItem(R.id.menu_movie_share);
        shareMenuItem.setVisible(false);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);
        setShareProvider();
    }

    public void parseDetailsFromJson(String detailsJsonString, String posterSize) throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String MOVIE_ID = "id";
        final String POSTER_TAG = "poster_path";
        final String BASE_POSTER_PATH = getString(R.string.poster_base_path);
        final String TITLE_TAG = "title";
        final String OVERVIEW_TAG = "overview";
        final String RUNTIME_TAG = "runtime";
        final String VOTE_AVERAGE_TAG = "vote_average";
        final String RELEASE_DATE_TAG = "release_date";
        final String BACKDROP_TAG = "backdrop_path";
        final String TRAILERS_TAG = "trailers";
        final String YOUTUBE_TAG = "youtube";
        final String TRAILER_NAME_TAG = "name";
        final String TRAILER_SOURCE_TAG = "source";
        final String CREDITS_TAG = "credits";
        final String CAST_TAG = "cast";
        final String CAST_NAME_TAG = "name";
        final String CAST_CHARACTER_TAG = "character";
        final String CAST_PROFILE_TAG = "profile_path";
        final String CAST_ID = "id";


        JSONObject detailsJson = new JSONObject(detailsJsonString);
        String imageUrl = BASE_POSTER_PATH + posterSize + detailsJson.getString(POSTER_TAG);
        String backdropUrl = BASE_POSTER_PATH + "/w1920" + detailsJson.getString(BACKDROP_TAG);
        String date = detailsJson.getString(RELEASE_DATE_TAG);
        String[] parsedDate = date.split("-");

        // Set the detailMovieData object
        thisMovieData.setMovieID(detailsJson.getString(MOVIE_ID));
        thisMovieData.setImageUrl(imageUrl);
        thisMovieData.setDescription(detailsJson.getString(OVERVIEW_TAG));
        thisMovieData.setDuration(detailsJson.getString(RUNTIME_TAG));
        thisMovieData.setRating(detailsJson.getString(VOTE_AVERAGE_TAG));
        thisMovieData.setYear(parsedDate[0]);
        thisMovieData.setTitle(detailsJson.getString(TITLE_TAG));
        thisMovieData.setBackdropUrl(backdropUrl);

        JSONObject trailerObject = detailsJson.getJSONObject(TRAILERS_TAG);
        JSONArray YtTrailerArray = trailerObject.getJSONArray(YOUTUBE_TAG);

        Trailer[] trailers = new Trailer[YtTrailerArray.length()];
        for (int i = 0; i < YtTrailerArray.length(); i++) {
            String trailerName = YtTrailerArray.getJSONObject(i).getString(TRAILER_NAME_TAG);
            String trailerId = YtTrailerArray.getJSONObject(i).getString(TRAILER_SOURCE_TAG);
            String trailerUrl = getString(R.string.youtube_base_uri) + trailerId;
            trailers[i] = new Trailer(trailerName, trailerUrl, trailerId);
            shareMenuItem.setVisible(true);
        }

        JSONObject creditsObject = detailsJson.getJSONObject(CREDITS_TAG);
        JSONArray castsJsonArray = creditsObject.getJSONArray(CAST_TAG);
        castObjectArray = new ArrayList<>();
        for (int i = 0; i < castsJsonArray.length(); i++) {
            String castName = castsJsonArray.getJSONObject(i).getString(CAST_NAME_TAG);
            String castProfile = castsJsonArray.getJSONObject(i).getString(CAST_PROFILE_TAG);
            String castImageUrl = getString(R.string.cast_profile_baseURI) + castProfile;
            String castID = castsJsonArray.getJSONObject(i).getString(CAST_ID);
            castObjectArray.add(new CastViewObject(castName, castImageUrl, castID));
        }
        thisMovieData.setTrailers(trailers);
        thisMovieData.setCasts(castObjectArray);
        setShareProvider();
    }

    public void setShareProvider() {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
    }

    public Intent createShareIntent() {
        if (thisMovieData.getTrailers() != null && thisMovieData.getTrailers().length > 0) {
            shareMenuItem.setVisible(true);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            String shareString = getString(R.string.share_base_string_1)
                    + ": "
                    + thisMovieData.getTrailers()[0].getSource()
                    + " "
                    + getString(R.string.share_base_string_2)
                    + " '"
                    + thisMovieData.getTitle()
                    + "' "
                    + getString(R.string.share_base_string3);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareString);
            return shareIntent;
        }
        return null;
    }

    public void loadBackgroundImage(String backdropUrl) {
        if (loadTarget == null) {
            loadTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.movie_backdrop_layout);
                    layout.setBackground(new BitmapDrawable(bitmap));
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };
            PicassoImageCache.
                    getPicassoInstance(getActivity())
                    .load(backdropUrl)
                    .error(R.drawable.user_placeholder_image)
                    .into(loadTarget);
        }
    }

    public void setFragmentViews() {
        final int layoutWidth = 342;
        final int layoutHeight = 513;
        final ImageView imageView;
        final ProgressBar thisProgressBar = progressBar;
        String LOG_TAG = this.getClass().getSimpleName();
        try {
            movieTitleView = (TextView) getActivity().findViewById(R.id.movie_title_detail);
            TextView movieRatingView = (TextView) getActivity().findViewById(R.id.move_rating_detail);
            TextView movieDateView = (TextView) getActivity().findViewById(R.id.movie_date_detail);
            TextView movieDurationView = (TextView) getActivity().findViewById(R.id.movie_time_detail);
            TextView movieDescriptionView = (TextView) getActivity().findViewById(R.id.movie_description_detail);
            imageView = (ImageView) getActivity().findViewById(R.id.movie_image_detail);
            android.view.ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            layoutParams.width = layoutWidth;
            layoutParams.height = layoutHeight;
            imageView.setLayoutParams(layoutParams);
            ImageView starImageView = (ImageView) getActivity().findViewById(R.id.starImage);
            starImageView.setImageResource(R.drawable.full_star);

            PicassoImageCache
                    .getPicassoInstance(getActivity())
                    .load(thisMovieData.getImageUrl())
                    .error(R.drawable.user_placeholder_image)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            loadBackgroundImage(thisMovieData.getBackdropUrl());
                            thisProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {

                        }
                    });

            // Some foreign movies display duration = 0 and description as "null".
            movieRatingView.setText(thisMovieData.getRating());
            movieDateView.setText(thisMovieData.getYear());

            if (thisMovieData.getDuration().equals("null") || Integer.parseInt(thisMovieData.getDuration()) == 0) {
                movieDurationView.setText("N/A");
            } else {
                movieDurationView.setText(thisMovieData.getDuration() + getString(R.string.minutes));
            }

            if (thisMovieData.getDescription().equals("null") || (thisMovieData.getDescription().length() == 0)) {
                movieDescriptionView.setText(getString(R.string.no_overview));
            } else {
                final String description = thisMovieData.getDescription();
                movieDescriptionView.setText(snipDescription(description));
                movieDescriptionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        extendedInfoPopUp(description,getString(R.string.overview));
                    }
                });
            }

            setCastView();
            movieTitleView.setText(thisMovieData.getTitle());
            setTrailerViews();
            setReviewViews();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in displaying movie details");
        }
    }

    public String snipDescription(String overviewText){
        int start = 0;
        int end = 300;
        return overviewText.length()>end?(overviewText.substring(start,end) + " ..."):overviewText;
    }

    public void extendedInfoPopUp(String extendedInfo, String popupTitle){
        final AlertDialog builder = new AlertDialog.Builder(getActivity()).create();
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.review_dialog_layout, null);
        builder.setView(view);
        TextView textView = (TextView) view.findViewById(R.id.review_dialog_text);
        textView.setText(extendedInfo);
        TextView title = new TextView(getActivity());
        title.setText(popupTitle);
        title.setPadding(10, 25, 10, 20);
        title.setTextSize(21);
        title.setBackgroundColor(getResources().getColor(R.color.movie_title_color));
        title.setGravity(Gravity.CENTER);
        builder.setCustomTitle(title);
        Button dismissButton = (Button) view.findViewById(R.id.dialog_dismiss_button);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
            }
        });

        builder.show();
    }

    public void setCastView() {
        List<CastViewObject> castViewList = thisMovieData.getCasts();
        castAdapter.setCastViewList(castViewList);
        castAdapter.notifyDataSetChanged();
    }

    public void setTrailerViews() {
        Trailer[] trailerData = thisMovieData.getTrailers();
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.trailer_layout);

        if (trailerData.length == 0) {
            View v = inflater.inflate(R.layout.trailer_item_layout, null);
            TextView infoText = (TextView) v.findViewById(R.id.trailer_name);
            ImageView playImage = (ImageView) v.findViewById(R.id.video_play_image);
            playImage.setVisibility(View.INVISIBLE);
            infoText.setText(getString(R.string.no_trailer_text));
            layout.addView(v);
        } else {
            for (int pos = 0; pos < trailerData.length; pos++) {
                View v = inflater.inflate(R.layout.trailer_item_layout, null);
                TextView trailerText = (TextView) v.findViewById(R.id.trailer_name);
                ImageView playImage = (ImageView) v.findViewById(R.id.video_play_image);
                final Trailer currentTrailer = trailerData[pos];
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent ytIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd:youtube:" + currentTrailer.getTrailerId()));
                            startActivity(ytIntent);
                        } catch (ActivityNotFoundException e) {
                            Intent uriIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentTrailer.getSource()));
                            startActivity(uriIntent);
                        }
                    }
                });

                trailerText.setText(trailerData[pos].getTrailerName());
                playImage.setVisibility(View.VISIBLE);
                layout.addView(v);
            }
        }
    }

    // Get movie details from cursor and set views
    public void readDetailsFromCursor(Cursor cursor) {
        thisMovieData.setMovieID(cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_MOVIE_ID)));
        thisMovieData.setImageUrl(cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_POSTER)));
        thisMovieData.setDescription(cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_OVERVIEW)));
        thisMovieData.setDuration(cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_RUNTIME)));
        thisMovieData.setRating(cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_RATING)));
        thisMovieData.setYear(cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_RELEASE_YEAR)));
        thisMovieData.setTitle(cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_MOVIE_TITLE)));
        thisMovieData.setBackdropUrl(cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_BACKDROP)));
        readReviewsFromCursor();
        readTrailersFromCursor();
        readCastFromCursor();
        setFragmentViews();
    }

    public void readCastFromCursor() {
        ArrayList<CastViewObject> castsDB = new ArrayList<CastViewObject>();
        if (castCursor.getCount() > 0) {
            if (castCursor.moveToFirst()) {
                do {
                    String castName = castCursor.getString(castCursor.getColumnIndex(CastEntry.COLUMN_CAST_NAME));
                    String castURI = castCursor.getString(castCursor.getColumnIndex(CastEntry.COLUMN_CAST_URI));
                    String castID = castCursor.getString(castCursor.getColumnIndex(CastEntry.COLUMN_CAST_ID));
                    CastViewObject cast = new CastViewObject(castName,castURI,castID);
                    castsDB.add(cast);
                } while (castCursor.moveToNext());
            }
        }
        thisMovieData.setCasts(castsDB);
    }

    public void readReviewsFromCursor() {
        Review[] reviewsDB;
        if (reviewCursor.getCount() > 0) {
            reviewsDB = new Review[reviewCursor.getCount()];
            int index = 0;
            if (reviewCursor.moveToFirst()) {
                do {
                    String author = reviewCursor.getString(reviewCursor.getColumnIndex(ReviewsEntry.COLUMN_REVIEW_AUTHOR));
                    String reviewText = reviewCursor.getString(reviewCursor.getColumnIndex(ReviewsEntry.COLUMN_REVIEW_TEXT));
                    reviewText = reviewText + " - " + author;
                    Review thisReview = new Review();
                    thisReview.setAuthor(author);
                    thisReview.setReviewText(reviewText);
                    reviewsDB[index++] = thisReview;
                } while (reviewCursor.moveToNext());
            }
        } else {
            Review reviewObject = new Review();
            reviewObject.setReviewText(getString(R.string.no_review_text));
            reviewsDB = new Review[]{reviewObject};
        }
        thisMovieData.setReviews(reviewsDB);
    }

    public void readTrailersFromCursor() {
        if (trailerCursor.getCount() > 0) {
            int index = 0;
            Trailer[] trailersDB = new Trailer[trailerCursor.getCount()];
            if (trailerCursor.moveToFirst()) {
                do {
                    Trailer thisTrailer = new Trailer();
                    thisTrailer.setTrailerId(trailerCursor.getString(trailerCursor.getColumnIndex(TrailerEntry.COLUMN_TRAILER_ID)));
                    thisTrailer.setTrailerName(trailerCursor.getString(trailerCursor.getColumnIndex(TrailerEntry.COLUMN_TRAILER_TITLE)));
                    thisTrailer.setSource(trailerCursor.getString(trailerCursor.getColumnIndex(TrailerEntry.COLUMN_TRAILER_URI)));
                    trailersDB[index++] = thisTrailer;
                } while (trailerCursor.moveToNext());
            }
            thisMovieData.setTrailers(trailersDB);
        }
    }
}
