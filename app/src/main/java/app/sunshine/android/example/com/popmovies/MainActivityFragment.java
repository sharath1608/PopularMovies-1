package app.sunshine.android.example.com.popmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static List<String> moviesImages;
    private ArrayAdapter<String> movieGridAdapter;
    private static List<String> movieIds;
    private String sortByValue;

    public MainActivityFragment() {

    }

    @Override
    public void onStart() {
        super.onStart();
        fetchMovies();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) view.findViewById(R.id.fragment_main_gridView);
        moviesImages = new ArrayList<>();
        movieGridAdapter = new GridViewAdapter(getActivity(),moviesImages);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                detailIntent.setType("text/plain");
                String movieId = movieIds.get(position).toString();
                detailIntent.putExtra(Intent.EXTRA_TEXT, movieId);
                startActivity(detailIntent);
            }
        });

        setHasOptionsMenu(true);
        gridView.setAdapter(movieGridAdapter);
        return view;
    }

    // Define the spinner to display the a drop-down menu to list the sort options.
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final String prefKey = "sortOption";

        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);

        String savedSpinnerPos = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(prefKey,"");
        Spinner sortingSpinner = (Spinner)menu.findItem(R.id.sort_spinner).getActionView();
        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(getActivity().getApplication(), R.array.sort_option, android.R.layout.simple_spinner_dropdown_item);
        sortingSpinner.setAdapter(spinnerAdapter);
        if(savedSpinnerPos!=""){
            sortingSpinner.setSelection(Integer.parseInt(savedSpinnerPos));
        }
        sortingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String itemSelected = parent.getItemAtPosition(position).toString();
                SharedPreferences prefs;

                // Sorting criteria
                if (itemSelected.equals(getString(R.string.most_popular))) {
                    sortByValue = getString(R.string.popularity_sort);
                } else if (itemSelected.equals(getString(R.string.highest_rated))) {
                    sortByValue = getString(R.string.ratings_sort);
                } else if (itemSelected.equals(getString(R.string.highest_grossing))) {
                    sortByValue = getString(R.string.earnings_sort);
                }

                // Using sharedPreferences to store
                prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.putString(prefKey,String.valueOf(position)).commit();

                // fetch the list of movies with the updated sort value
                fetchMovies();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void fetchMovies(){
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        fetchMoviesTask.execute();
    }

    public class FetchMoviesTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            int connectionTimeout = 3000;
            String apiKey = getString(R.string.api_key);
            String moviesJsonString = null;
            String LOG_TAG = FetchMoviesTask.class.getSimpleName();
            String MOVIES_BASE_URI = getString(R.string.movies_base_uri);
            String posterSize = getString(R.string.grid_poster_size);
            String API_REQ_STRING = "api_key";
            String SORT_BY_REQ = "sort_by";

            try {
                // URL example :  new URL("http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=[YOUR API KEY]");

                Uri builtUri = Uri.parse(MOVIES_BASE_URI).buildUpon()
                        .appendQueryParameter(SORT_BY_REQ, sortByValue)
                        .appendQueryParameter(API_REQ_STRING, apiKey).build();

                try {
                    URL url = new URL(builtUri.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(connectionTimeout); // in milliseconds
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                }catch (ConnectException e){
                    Log.e(LOG_TAG,"Error",e);
                }

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
                moviesJsonString = stringBuffer.toString();
                try {
                    moviesImages = getMoviesFromJson(moviesJsonString, posterSize);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "error", e);
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
            return moviesImages;
        }

        private List<String> getMoviesFromJson(String moviesJsonString, String posterSize)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String POSTER_TAG = "poster_path";
            final String RESULTS_TAG = "results";
            final String BASE_POSTER_PATH = getString(R.string.poster_base_path);
            final String IDS_TAG = "id";

            JSONObject moviesJSON = new JSONObject(moviesJsonString);
            movieIds = new ArrayList<String>();

            List<String> posterArray = new ArrayList<String>();
            JSONArray resultsArray = moviesJSON.getJSONArray(RESULTS_TAG);
            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject resultObject = resultsArray.getJSONObject(i);
                posterArray.add(BASE_POSTER_PATH + posterSize + resultObject.getString(POSTER_TAG));
                movieIds.add(resultObject.getString(IDS_TAG));
            }
            return posterArray;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);
            if (result != null) {
                movieGridAdapter.clear();
                movieGridAdapter.addAll(result);
            }
        }

    }
}
