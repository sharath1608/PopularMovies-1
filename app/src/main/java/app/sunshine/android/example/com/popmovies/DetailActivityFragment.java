package app.sunshine.android.example.com.popmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private String movieId;

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
        displayMovieDetails();
    }

    public void displayMovieDetails(){
        FetchMovieDetails fetchMovieDetails = new FetchMovieDetails();
        fetchMovieDetails.execute(movieId);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        movieId = getArguments().getString(Intent.EXTRA_TEXT);
        return view;
    }

    public class FetchMovieDetails extends AsyncTask<String,Void,DetailMovieData> {

        @Override
        protected DetailMovieData doInBackground(String... params){
            DetailMovieData detailMovieData = new DetailMovieData();

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String apiKey = getString(R.string.api_key);
            String detailsJsonString = null;
            String DETAIL_BASE_URI = getString(R.string.details_base_path);
            String posterSize = getString(R.string.detail_poster_size);
            final String API_REQ_STRING = "api_key";

            String LOG_TAG = FetchMovieDetails.class.getSimpleName();

            try {
                // URL example :  new URL("http://api.themoviedb.org/3/movie/movieId?api_key=[YOUR API KEY]");

                Uri builtUri = Uri.parse(DETAIL_BASE_URI).buildUpon().appendPath(movieId)
                        .appendQueryParameter(API_REQ_STRING, apiKey).build();


                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream streamReader = urlConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();
                if (streamReader == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(streamReader));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuffer.append(line + "\n");
                }
                if (stringBuffer.length() == 0) {
                    return null;
                }
                detailsJsonString = stringBuffer.toString();
                try {
                    detailMovieData = parseDetailsFromJson(detailsJsonString, detailMovieData,posterSize);
                } catch (JSONException e) {
                    Log.e("Movie details parser", "error", e);
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream");
                        return null;
                    }
                }
            }
            return detailMovieData;
        }

        public DetailMovieData parseDetailsFromJson (String detailsJsonString, DetailMovieData detailMovieData,String posterSize)throws JSONException{

            // These are the names of the JSON objects that need to be extracted.
            final String POSTER_TAG = "poster_path";
            final String BASE_POSTER_PATH = getString(R.string.poster_base_path);
            final String TITLE_TAG = "title";
            final String OVERVIEW_TAG = "overview";
            final String RUNTIME_TAG = "runtime";
            final String VOTE_AVERAGE_TAG = "vote_average";
            final String RELEASE_DATE_TAG = "release_date";

            JSONObject detailsJson = new JSONObject(detailsJsonString);

            String imageUrl = BASE_POSTER_PATH + posterSize + detailsJson.getString(POSTER_TAG);
            String date = detailsJson.getString(RELEASE_DATE_TAG);
            String[] parsedDate = date.split("-");

            // Set the detailMovieData object
            detailMovieData.setImageUrl(imageUrl);
            detailMovieData.setDescription(detailsJson.getString(OVERVIEW_TAG));
            detailMovieData.setDuration(detailsJson.getString(RUNTIME_TAG));
            detailMovieData.setRating(detailsJson.getString(VOTE_AVERAGE_TAG));
            detailMovieData.setYear(parsedDate[0]);
            detailMovieData.setTitle(detailsJson.getString(TITLE_TAG));
            return detailMovieData;
        }

        @Override
        protected void onPostExecute(DetailMovieData result){
            super.onPostExecute(result);
            if(result!=null){
                setFragmentViews(result);
            }

        }
        public void setFragmentViews(DetailMovieData detailMovieData){
            int layoutWidth = 342;
            int layoutHeight = 513;
            final String RATING_DEN = "/10";

            TextView movieTitleView = (TextView) getActivity().findViewById(R.id.movie_title_detail);
            TextView movieRatingView = (TextView) getActivity().findViewById(R.id.move_rating_detail);
            TextView movieDateView = (TextView) getActivity().findViewById(R.id.movie_date_detail);
            TextView movieDurationView = (TextView) getActivity().findViewById(R.id.movie_time_detail);
            TextView movieDescriptionView = (TextView)getActivity().findViewById(R.id.movie_description_detail);

            ImageView imageView = (ImageView)getActivity().findViewById(R.id.movie_image_detail);
            android.view.ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            layoutParams.width = layoutWidth;
            layoutParams.height = layoutHeight;
            imageView.setLayoutParams(layoutParams);

            Picasso
                    .with(getActivity())
                    .load(detailMovieData.getImageUrl())
                    .fit()
                    .into(imageView);

            // Some foreign movies display duration = 0 and description as "null".
            movieRatingView.setText(detailMovieData.getRating() + RATING_DEN);
            movieDateView.setText(detailMovieData.getYear());

            if(Integer.parseInt(detailMovieData.getDuration()) == 0){
                movieDurationView.setText("N/A");
            }else {
                movieDurationView.setText(detailMovieData.getDuration() + getString(R.string.minutes));
            }

            if(detailMovieData.getDescription().equals("null")){
                movieDescriptionView.setText(getString(R.string.no_overview));
            }else {
                movieDescriptionView.setText(detailMovieData.getDescription());
            }
            movieTitleView.setText(detailMovieData.getTitle());
        }
    }
}