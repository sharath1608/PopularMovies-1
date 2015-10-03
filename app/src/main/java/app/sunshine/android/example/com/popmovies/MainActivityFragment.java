package app.sunshine.android.example.com.popmovies;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import com.android.popmovies.app.data.MoviesContract.MoviesEntry;
import com.android.popmovies.app.data.MoviesContract.FavoriteEntry;
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
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayAdapter<GridViewObject> movieGridAdapter;
    private static List<String> movieIds;
    private CursorAdapter favCursorAdapter;
    private static List<GridViewObject> gridViewObjects;
    private String sortByValue;
    private String movieKey = "movieKey";
    private int position;
    private GridView gridView;
    private Toast commonToast;
    private static int pagenum;
    private boolean rogueFirstTime;
    private boolean justChangedToLand;
    private final String spinPosKey = "sortPos";
    private final String spinStringKey = "sortString";
    private boolean nextPageCalling;
    private String favSortString;
    private String showSortString;
    private String grossSortString;
    private String popSortString;
    private SharedPreferences prefs;
    private ProgressDialog progDialog;
    private String[] cursorSelection = {MoviesEntry.TABLE_NAME + "." + MoviesEntry._ID, MoviesEntry.COLUMN_MOVIE_ID, MoviesEntry.COLUMN_MOVIE_TITLE, MoviesEntry.COLUMN_POSTER};

    public MainActivityFragment() {

    }

    @Override
    public void onStart() {
        super.onStart();

        if (gridViewObjects.isEmpty())
            fetchMovies();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        gridView = (GridView) view.findViewById(R.id.fragment_main_gridView);
        favSortString = getString(R.string.favorites);
        grossSortString = getString(R.string.highest_grossing);
        popSortString = getString(R.string.most_popular);
        showSortString = getString(R.string.now_showing);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (savedInstanceState != null) {
            gridViewObjects = (List<GridViewObject>) savedInstanceState.get(movieKey);
            position = savedInstanceState.getInt("position");
            justChangedToLand = true;
        } else {
            gridViewObjects = new ArrayList<>();
        }

        favCursorAdapter = new CustomFavViewAdapter(getActivity(), null, 0);
        movieGridAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_layout, gridViewObjects);

        // Set grid view columns here.
        if(getResources().getBoolean(R.bool.isTablet)) {
            Configuration config = getResources().getConfiguration();
            if(config.orientation == config.ORIENTATION_LANDSCAPE) {
                gridView.setNumColumns(2);
            }else if(config.orientation == config.ORIENTATION_PORTRAIT){
                gridView.setNumColumns(1);
            }
        }
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String movieId = movieIds.get(position);
                ((Callback) getActivity())
                        .onItemSelected(movieId);
            }
        });

        gridView.setSelection(position);
        // Read scroll position to set page number in the request.


        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                // If reached the end of page, fetch the next page from the request.
                if ((firstVisibleItem + visibleItemCount >= totalItemCount) && nextPageCalling) {
                    nextPageCalling = false;
                    pagenum++;
                    fetchMovies();
                }
            }
        });
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (commonToast != null)
            commonToast.cancel();
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(String id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(movieKey, (ArrayList) gridViewObjects);
        outState.putInt("position", gridView.getFirstVisiblePosition());
        super.onSaveInstanceState(outState);
    }

    // Define the spinner to display the a drop-down menu to list the sort options.    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        String LOG_TAG = this.getClass().getSimpleName();
        super.onCreateOptionsMenu(menu, inflater);

        getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);

        Log.v(LOG_TAG, "Entering onCreateOptionsMenu");
        String savedSpinnerPos = prefs.getString(spinPosKey, "");
        final Spinner sortingSpinner = (Spinner) menu.findItem(R.id.sort_spinner).getActionView();
        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(getActivity().getApplication(), R.array.sort_option, android.R.layout.simple_spinner_dropdown_item);
        rogueFirstTime = true;
        sortingSpinner.setAdapter(spinnerAdapter);
        if (savedSpinnerPos !=null && !savedSpinnerPos.isEmpty()) {
            sortingSpinner.setSelection(Integer.parseInt(savedSpinnerPos));
        }
        sortingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String itemSelected = parent.getItemAtPosition(position).toString();
                SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.putString(spinPosKey, String.valueOf(position)).apply();
                if (commonToast != null) {
                    commonToast.cancel();
                }

                // Sorting criteria
                // Sort by popularity
                if (itemSelected.equals(popSortString)) {
                    sortByValue = getString(R.string.popularity_sort);
                }
                // Sort by "Now Showing"
                else if (itemSelected.equals(showSortString)) {
                    sortByValue = getString(R.string.now_showing_sort);
                }
                // Sort by highest grossing
                else if (itemSelected.equals(grossSortString)) {
                    sortByValue = getString(R.string.earnings_sort);
                }
                // Sort by favorites
                else if (itemSelected.equals(favSortString)) {
                    sortByValue = getString(R.string.favorites);
                }

                // Depending on the sort criteria, set appropriate adapter for the view. If favorites, set favCursorAdapter, else set movieListAdapter
                prefEditor.putString(spinStringKey, sortByValue).apply();
                chooseAdapter();

                // Initialize and display progress dialog.
                if(NetworkUtils.isNetworkAvailable(getActivity())) {
                    progDialog = new ProgressDialog(getActivity());
                    progDialog.setMessage(getString(R.string.progress_text_main));
                    progDialog.setIndeterminate(true);
                    progDialog.show();
                }


                if (sortByValue.equals(favSortString)) {
                    viewFavorites();
                }

                // fetch the list of movies with the updated sort value
                // onItemSelected is called when orientation is changed and when spinner is created.
                if (!justChangedToLand) {
                    gridViewObjects = new ArrayList<>();

                    // If entering the first time (initialization of view), or if selected favorites as sort, then skip and do not fetch movies.
                    if (!rogueFirstTime && !sortByValue.equals(favSortString)) {
                        pagenum = 1;
                        movieIds = new ArrayList<>();
                        fetchMovies();
                    } else {
                        progDialog.dismiss();
                        rogueFirstTime = false;
                    }
                } else {
                    justChangedToLand = false;
                    rogueFirstTime = false;
                    progDialog.dismiss();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    public void chooseAdapter() {

        if (sortByValue.equals(getString(R.string.favorites))) {
            gridView.setAdapter(favCursorAdapter);
        } else {
            gridView.setAdapter(movieGridAdapter);
        }
    }

    public void viewFavorites() {
        gridView.setAdapter(favCursorAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public CursorLoader onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                FavoriteEntry.CONTENT_URI,
                cursorSelection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        movieIds = new ArrayList<>();
        if (data.moveToFirst()) {
            do {
                movieIds.add(data.getString(data.getColumnIndex(MoviesEntry.COLUMN_MOVIE_ID)));
            } while (data.moveToNext());
            favCursorAdapter.swapCursor(data);
            progDialog.dismiss();
        } else {
            displayToastMessage(getString(R.string.no_fav_text));
        }
    }

    // This method disables any progress dialogue and displays the toast.
    public void displayToastMessage(String message){
        if(progDialog!=null && progDialog.isShowing()){
            progDialog.dismiss();
        }
        commonToast = Toast.makeText(getActivity(),
                message,
                Toast.LENGTH_SHORT);
        commonToast.setGravity(Gravity.CENTER, 0, 0);
        commonToast.show();
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    public void fetchMovies() {
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        if (!NetworkUtils.isNetworkAvailable(getActivity())) {
            displayToastMessage(getString(R.string.no_connect_string));
        } else
            fetchMoviesTask.execute();
    }

    public class FetchMoviesTask extends AsyncTask<Void, Void, List<GridViewObject>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<GridViewObject> doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            int connectionTimeout = 3000;
            String apiKey = getString(R.string.api_key);
            String moviesJsonString;
            String LOG_TAG = FetchMoviesTask.class.getSimpleName();
            String MOVIES_BASE_URI = getString(R.string.tmdb_base_uri);
            String posterSize = getString(R.string.grid_poster_size);
            String API_REQ_STRING = "api_key";
            String SORT_BY_REQ = "sort_by";
            String PAGE_NUM = "page";


            try {
                sortByValue = prefs.getString(spinStringKey, "");
                if (sortByValue == null || sortByValue.isEmpty()) {
                    sortByValue = getString(R.string.now_showing_sort);
                }

                // URL example :  new URL("http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=[YOUR API KEY]");
                Uri builtUri = Uri.parse(MOVIES_BASE_URI).buildUpon().build();

                if (sortByValue.equals(getString(R.string.now_showing_sort))) {
                    builtUri = builtUri.buildUpon()
                            .appendPath(getString(R.string.movie_tag))
                            .appendPath(getString(R.string.now_showing_sort))
                            .build();
                } else if (sortByValue.equals(getString(R.string.favorites))) {
                    return null;
                } else {
                    builtUri = builtUri.buildUpon()
                            .appendPath(getString(R.string.discover_tag))
                            .appendPath(getString(R.string.movie_tag))
                            .appendQueryParameter(SORT_BY_REQ, sortByValue).build();
                }

                pagenum = pagenum <= 1 ? 1 : pagenum;
                nextPageCalling = true;
                builtUri = builtUri.buildUpon().appendQueryParameter(PAGE_NUM, String.valueOf(pagenum)).appendQueryParameter(API_REQ_STRING, apiKey).build();

                try {
                    URL url = new URL(builtUri.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(connectionTimeout); // in milliseconds
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                } catch (ConnectException e) {
                    Log.e(LOG_TAG, "Error", e);
                }

                InputStream streamReader = urlConnection.getInputStream();
                StringBuilder stringBuffer = new StringBuilder();

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
                    gridViewObjects.addAll(getMoviesFromJson(moviesJsonString, posterSize));
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
            final String BASE_POSTER_PATH = getString(R.string.poster_base_path);
            final String IDS_TAG = "id";
            final String TITLE_TAG = "title";
            List<GridViewObject> movieGridObjects;
            JSONObject moviesJSON = new JSONObject(moviesJsonString);
            // This check is for scrolling multiple pages. The previous movies need to be retained when fetching the next page of movies.
            if (movieIds == null)
                movieIds = new ArrayList<>();

            movieGridObjects = new ArrayList<>();
            JSONArray resultsArray = moviesJSON.getJSONArray(RESULTS_TAG);
            for (int i = 0; i < resultsArray.length(); i++) {
                GridViewObject gridViewObject = new GridViewObject();
                JSONObject resultObject = resultsArray.getJSONObject(i);
                gridViewObject.setMovieUrl(BASE_POSTER_PATH + posterSize + resultObject.getString(POSTER_TAG));
                gridViewObject.setMovieTag(resultObject.getString(TITLE_TAG));
                movieGridObjects.add(gridViewObject);
                movieIds.add(resultObject.getString(IDS_TAG));
            }
            return movieGridObjects;
        }


        //TODO: If  a new arrayList is not used to store the data from res, on movieGridAdapter.clear() wipes out res. Investigate
        @Override
        protected void onPostExecute(List<GridViewObject> res) {
            if (res != null) {
                List<GridViewObject> result = new ArrayList<>(res);
                super.onPostExecute(res);
                movieGridAdapter.clear();
                movieGridAdapter.addAll(result);
                movieGridAdapter.notifyDataSetChanged();
                progDialog.dismiss();
            }
        }
    }
}