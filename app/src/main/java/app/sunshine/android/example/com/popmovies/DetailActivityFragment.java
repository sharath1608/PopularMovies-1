package app.sunshine.android.example.com.popmovies;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private String movieId;
    private Toast noConnectToast;
    private ImageView imageView;
    private TextView movieTitleView;
    private Target loadTarget;
    private ProgressBar progressBar;

    public static DetailActivityFragment newInstance(String id) {
        DetailActivityFragment fragment = new DetailActivityFragment();
        Bundle args = new Bundle();
        args.putString(Intent.EXTRA_TEXT, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        requestMovieDetails();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (noConnectToast != null)
            noConnectToast.cancel();
    }

    public void requestMovieDetails() {

        final String LOG_TAG = getClass().getSimpleName();
        final String API_REQ_STRING = "api_key";
        String apiKey = getString(R.string.api_key);
        String DETAIL_BASE_URI = getString(R.string.details_base_path);
        progressBar = (ProgressBar)getActivity().findViewById(R.id.progressBar_detail);
        RequestQueue mRequestQueue = Volley.newRequestQueue(getActivity());
        Uri builtUri = Uri.parse(DETAIL_BASE_URI).buildUpon().appendPath(movieId)
                .appendQueryParameter(API_REQ_STRING, apiKey).build();

        String stringUri = builtUri.toString();
        JsonObjectRequest primaryJsonReq = new JsonObjectRequest(Request.Method.GET, stringUri, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {
                    String posterSize = getString(R.string.detail_poster_size);
                    DetailMovieData movieData = new DetailMovieData();
                    setFragmentViews(parseDetailsFromJson(response.toString(), movieData, posterSize));
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    Log.e(LOG_TAG,"Error reading the response");
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG,"Error getting response for the JSON request");
            }
        });

        mRequestQueue.add(primaryJsonReq);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        movieId = getArguments().getString(Intent.EXTRA_TEXT);
        return view;
    }

    public DetailMovieData parseDetailsFromJson(String detailsJsonString, DetailMovieData detailMovieData, String posterSize) throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String POSTER_TAG = "poster_path";
        final String BASE_POSTER_PATH = getString(R.string.poster_base_path);
        final String TITLE_TAG = "title";
        final String OVERVIEW_TAG = "overview";
        final String RUNTIME_TAG = "runtime";
        final String VOTE_AVERAGE_TAG = "vote_average";
        final String RELEASE_DATE_TAG = "release_date";
        final String BACKDROP_TAG = "backdrop_path";

        JSONObject detailsJson = new JSONObject(detailsJsonString);

        String imageUrl = BASE_POSTER_PATH + posterSize + detailsJson.getString(POSTER_TAG);
        String backdropUrl = BASE_POSTER_PATH + "/w1920" + detailsJson.getString(BACKDROP_TAG);
        String date = detailsJson.getString(RELEASE_DATE_TAG);
        String[] parsedDate = date.split("-");

        // Set the detailMovieData object
        detailMovieData.setImageUrl(imageUrl);
        detailMovieData.setDescription(detailsJson.getString(OVERVIEW_TAG));
        detailMovieData.setDuration(detailsJson.getString(RUNTIME_TAG));
        detailMovieData.setRating(detailsJson.getString(VOTE_AVERAGE_TAG));
        detailMovieData.setYear(parsedDate[0]);
        detailMovieData.setTitle(detailsJson.getString(TITLE_TAG));
        detailMovieData.setBackdropUrl(backdropUrl);
        return detailMovieData;
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
            Picasso.with(getActivity()).load(backdropUrl).into(loadTarget);
        }
    }

    public void setFragmentViews(final DetailMovieData detailMovieData) {
        final int layoutWidth = 342;
        final int layoutHeight = 513;
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

            Picasso
                    .with(getActivity())
                    .load(detailMovieData.getImageUrl())
                    .fit()
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            loadBackgroundImage(detailMovieData.getBackdropUrl());
                            //progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {

                        }
                    });

            // Some foreign movies display duration = 0 and description as "null".
            movieRatingView.setText(detailMovieData.getRating());
            movieDateView.setText(detailMovieData.getYear());

            if (Integer.parseInt(detailMovieData.getDuration()) == 0) {
                movieDurationView.setText("N/A");
            } else {
                movieDurationView.setText(detailMovieData.getDuration() + getString(R.string.minutes));
            }

            if (detailMovieData.getDescription().equals("null")) {
                movieDescriptionView.setText("No overview available");
            } else {
                movieDescriptionView.setText(detailMovieData.getDescription());
            }
            movieTitleView.setText(detailMovieData.getTitle());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in displaying movie details");
        }
    }
}