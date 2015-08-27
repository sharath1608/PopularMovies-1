package app.sunshine.android.example.com.popmovies;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
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

    private ArrayAdapter<GridViewObject> movieGridAdapter;
    private static List<String> movieIds;
    private static List<GridViewObject> gridViewObjects;
    private String sortByValue;
    private String movieKey = "movieKey";
    private int position;
    private GridView gridView;
    private Toast noConnectToast;

    public MainActivityFragment() {

    }

    @Override
    public void onStart() {
        super.onStart();

        if(gridViewObjects.size() == 0)
            fetchMovies();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        gridView = (GridView) view.findViewById(R.id.fragment_main_gridView);
        if(savedInstanceState != null){
            gridViewObjects = (List<GridViewObject>)savedInstanceState.get(movieKey);
            position = savedInstanceState.getInt("position");
        } else{
            gridViewObjects = new ArrayList<>();
        }

        movieGridAdapter = new GridViewAdapter(getActivity(),R.layout.grid_item_layout,gridViewObjects);

        gridView.setAdapter(movieGridAdapter);
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

        gridView.setSelection(position);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(noConnectToast!=null)
            noConnectToast.cancel();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(movieKey, (ArrayList) gridViewObjects);
        outState.putInt("position", gridView.getFirstVisiblePosition());
    }

    // Define the spinner to display the a drop-down menu to list the sort options.
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final String prefKey = "sortOption";

        String LOG_TAG = this.getClass().getSimpleName();
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);

        Log.v(LOG_TAG,"Entering onCreateOptionsMenu");
        String savedSpinnerPos = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(prefKey,"");
        final Spinner sortingSpinner = (Spinner)menu.findItem(R.id.sort_spinner).getActionView();
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
                } else if (itemSelected.equals(R.string.now_showing)) {
                    sortByValue = getString(R.string.now_showing_sort);
                } else if (itemSelected.equals(getString(R.string.highest_grossing))) {
                    sortByValue = getString(R.string.earnings_sort);
                } else if(itemSelected.equals(getString(R.string.now_showing))){
                    sortByValue = getString(R.string.now_showing_sort);
                }

                // Using sharedPreferences to store
                prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.putString(prefKey, String.valueOf(position)).commit();

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
//        if(!NetworkUtils.isNetworkAvailable()) {
//            noConnectToast = Toast.makeText(getActivity(), "No connectivity! Please check your internet connection.", Toast.LENGTH_SHORT);
//            noConnectToast.show();
//        }
//        else
            fetchMoviesTask.execute();
    }

//    public boolean isNetworkAvailable(){
//        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkUtils
//
//    }

    public class FetchMoviesTask extends AsyncTask<Void, Void, List<GridViewObject>> {

        @Override
        protected List<GridViewObject> doInBackground(Void... params) {
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

                sortByValue = sortByValue==null?getString(R.string.now_showing_sort):sortByValue;

                // URL example :  new URL("http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=[YOUR API KEY]");
                Uri builtUri = Uri.parse(MOVIES_BASE_URI).buildUpon().build();

                if(sortByValue.equals(getString(R.string.now_showing_sort))){
                    builtUri = builtUri.buildUpon()
                            .appendPath(getString(R.string.movie_tag))
                            .appendPath(getString(R.string.now_showing_sort))
                            .build();
                }else{
                    builtUri = builtUri.buildUpon()
                            .appendPath(getString(R.string.discover_tag))
                            .appendPath(getString(R.string.movie_tag))
                            .appendQueryParameter(SORT_BY_REQ, sortByValue).build();
                }

                builtUri = builtUri.buildUpon().appendQueryParameter(API_REQ_STRING, apiKey).build();

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
                    gridViewObjects = getMoviesFromJson(moviesJsonString, posterSize);
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
            return gridViewObjects;
        }

        private List<GridViewObject> getMoviesFromJson(String moviesJsonString, String posterSize)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String POSTER_TAG = "poster_path";
            final String RESULTS_TAG = "results";
            final String BACKDROP_PATH = "backdrop_path";
            final String BASE_POSTER_PATH = getString(R.string.poster_base_path);
            final String IDS_TAG = "id";
            final String TITLE_TAG = "title";

            JSONObject moviesJSON = new JSONObject(moviesJsonString);
            movieIds = new ArrayList<>();
            gridViewObjects = new ArrayList<>();

            List<String> posterArray = new ArrayList<>();
            JSONArray resultsArray = moviesJSON.getJSONArray(RESULTS_TAG);
            for (int i = 0; i < resultsArray.length(); i++) {
                GridViewObject gridViewObject = new GridViewObject();
                JSONObject resultObject = resultsArray.getJSONObject(i);
                gridViewObject.setMovieUrl(BASE_POSTER_PATH + posterSize + resultObject.getString(POSTER_TAG));
                gridViewObject.setMovieTag(resultObject.getString(TITLE_TAG));
                gridViewObject.setBackDropURL(resultObject.getString(BACKDROP_PATH));
                gridViewObjects.add(gridViewObject);
                movieIds.add(resultObject.getString(IDS_TAG));
            }

            return gridViewObjects;
        }

        @Override
        protected void onPostExecute(List<GridViewObject> result) {
            super.onPostExecute(result);
            if (result != null) {
                movieGridAdapter.clear();
                movieGridAdapter.addAll(result);
            }

        }

    }
}
