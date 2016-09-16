package com.example.myapps.moviesalbum;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.myapps.moviesalbum.CustomLoaders.MoviesLoader;
import com.example.myapps.moviesalbum.sqliteDB.AndroidDatabaseManager;
import com.example.myapps.moviesalbum.sqliteDB.MoviesContract;
import com.example.myapps.moviesalbum.sqliteDB.MoviesDBHelper;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Movies>> {
    public static GridView moviesList;
    MoviesAdapter moviesAdapter = null;
    LoaderManager loader;
    ProgressBar progressBar;
    String WEB_URL;
    static String LOADING_KEY = "load_listview";
    boolean isPageLoadRequired = true;
    boolean isLoaderCompleted = false;
    final static String LOG_TAG = MainActivity.class.getSimpleName();
    int page = 1;
    int pageOnPause = 0;
    String searchText = null;
    boolean isSearchTriggered = false;

    AsyncTaskForMovies downloadMovies;
    AsyncTaskForMovies asyncForSearches;

    public static Context CONTEXT_MAIN_ACTIVITY;
    public static String MOVIE_ID = "movie_id";
    public static String MOVIE_DESC_KEY = "desc";
    public static String MOVIE_TITLE_KEY = "title";
    public static String AVG_RATING_KEY = "rating";
    public static String RELEASE_DATE_KEY = "release_date";
    public static String POSTER_KEY = "poster";
    public static String PAGE_NO_KEY = "page";
    public static String REVIEWS_KEY = "reviews";
    public static String TRAILER_URL_KEY = "trailer_url";

    private static String MOVIES_BASE_URL = "https://api.themoviedb.org/3/discover/movie?" +
            "primary_release_date.gte=2016-01-01&" +
            "primary_release_date.lte=2016-10-22&moviesort_by=release_date&" +
            "api_key=69b589af19cead810bc805ab8f5363f6";

    private static String SEARCH_BASE_URL = "http://api.themoviedb.org/3/search/movie?" +
            "&api_key=69b589af19cead810bc805ab8f5363f6&query=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(LOG_TAG + " Activity Callbacks ", "onCreate() Called");

        // Created for Utilities class
        CONTEXT_MAIN_ACTIVITY = MainActivity.this;

        handleIntent(getIntent());

        final LoaderManager.LoaderCallbacks<ArrayList<Movies>> loaderCallbacks = this;
        progressBar = (ProgressBar) findViewById(R.id.progress_bar_main);

        if (savedInstanceState != null) {
            page = savedInstanceState.getInt(PAGE_NO_KEY);
            isPageLoadRequired = savedInstanceState.getBoolean(LOADING_KEY);
        }

        moviesList = (GridView) findViewById(R.id.movies_gridview);
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.image_placeholder);

        ArrayList<Movies> arrayofMovies = new ArrayList<>();

        if (Utilities.internetState(this)) {

            MoviesDBHelper.onDelete(this, "DELETE FROM " + MoviesContract.MoviesEntry.TABLE_NAME +
                    " WHERE " + MoviesContract.MoviesEntry.COLUMN_MOVIE_FAVORITE + " = 'N'");

            constructUrl();

            //loader = getLoaderManager();
            //loadLoader();

            downloadMovies = new AsyncTaskForMovies();
            downloadMovies.execute(WEB_URL);

            // setting adapter with placeholder arrayofMovies
            moviesAdapter = new MoviesAdapter(this, 0, arrayofMovies);
            moviesList.setAdapter(moviesAdapter);

            moviesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Movies movies = moviesAdapter.getItem(position);
                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);

                    Bitmap bmp = movies.getImagePoster();

                    if (bmp == null) {
                        Toast.makeText(getApplicationContext(), "Invalid Content", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    intent.putExtra(MOVIE_ID, movies.getMovieID());
                    intent.putExtra(POSTER_KEY, byteArray);
                    intent.putExtra(MOVIE_DESC_KEY, movies.getDesc());
                    intent.putExtra(MOVIE_TITLE_KEY, movies.getMovieTitle());
                    intent.putExtra(AVG_RATING_KEY, movies.getRating());
                    intent.putExtra(RELEASE_DATE_KEY, movies.getReleaseDate());
                    Log.v(LOG_TAG, movies.getMovieID());
                    startActivity(intent);
                }
            });

            moviesList.setOnScrollListener(new AbsListView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if ((firstVisibleItem + visibleItemCount >= totalItemCount - 6) && totalItemCount != 0 &&
                            isLoaderCompleted == true && (totalItemCount%20) == 0) {
                        isLoaderCompleted = false;

                        if (Utilities.internetState(getApplicationContext()) == true) {
                            page++;
                            constructUrl();
                            progressBar.setVisibility(View.VISIBLE);
                            //loader.restartLoader(1, null, loaderCallbacks);
                            AsyncTaskForMovies asyncTaskForMovies = new AsyncTaskForMovies();
                            asyncTaskForMovies.execute(WEB_URL);
                        } else
                            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            loadFromSqlite();
        }
    }

    private void loadFromSqlite() {
        MoviesDBHelper dbHelper = new MoviesDBHelper(this);
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        final Cursor myCursorData = database.query(MoviesContract.MoviesEntry.TABLE_NAME, null, MoviesContract.MoviesEntry.COLUMN_MOVIE_FAVORITE + " = ?",
                new String[]{"N"}, null, null, null);
        MoviesCursorAdapter cursorAdapter = new MoviesCursorAdapter(this, myCursorData, false, View.GONE);
        moviesList.setAdapter(cursorAdapter);

        moviesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int initialPos = myCursorData.getPosition();
                myCursorData.moveToPosition(position);

                Intent detailsIntent = new Intent(getApplicationContext(), DetailActivity.class);
                detailsIntent.putExtra(MainActivity.MOVIE_ID, String.valueOf(myCursorData.getInt(FavouritesActivity.COL_INDEX_MOVIE_ID)));
                detailsIntent.putExtra(MainActivity.POSTER_KEY, myCursorData.getBlob(FavouritesActivity.COL_INDEX_MOVIE_POSTER));
                detailsIntent.putExtra(MainActivity.MOVIE_DESC_KEY, myCursorData.getString(FavouritesActivity.COL_INDEX_MOVIE_SYNOPSIS));
                detailsIntent.putExtra(MainActivity.MOVIE_TITLE_KEY, myCursorData.getString(FavouritesActivity.COL_INDEX_MOVIE_TITLE));
                detailsIntent.putExtra(MainActivity.AVG_RATING_KEY, myCursorData.getString(FavouritesActivity.COL_INDEX_MOVIE_RATING));
                detailsIntent.putExtra(MainActivity.RELEASE_DATE_KEY, myCursorData.getString(FavouritesActivity.COL_INDEX_MOVIE_RELEASE_DATE));
                detailsIntent.putExtra(MainActivity.TRAILER_URL_KEY, "#");
                //detailsIntent.putStringArrayListExtra(MainActivity.REVIEWS_KEY,
                //       MoviesDBHelper.convertJsonToReviews(myCursorData.getString(FavouritesActivity.COL_INDEX_MOVIE_REVIEWS)));
                detailsIntent.putExtra(FavouritesActivity.FLAG_INTERNET_KEY, false);
                detailsIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                //Log.v("GridView", myCursorData.getString(COL_INDEX_MOVIE_TITLE));
                startActivity(detailsIntent);

                myCursorData.moveToPosition(initialPos);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(LOG_TAG + " Activity Callbacks ", "onPause() Called");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(LOG_TAG + " Activity Callbacks ", "onStart() Called");

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(LOG_TAG + " Activity Callbacks ", "onRestart() Called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG + " Activity Callbacks ", "onResume() Called");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v(LOG_TAG + " Activity Callbacks ", "onSaveInstance() Called");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.v(LOG_TAG + " Activity Callbacks ", "onRestoreInstanceState() Called");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private String handleIntent(Intent intent) {

        searchText = null;
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            isLoaderCompleted = false;
            searchText = intent.getStringExtra(SearchManager.QUERY);

            page = 1;
            isSearchTriggered = true;
            moviesList.setVisibility(View.GONE);
            downloadMovies.onCancelled();
            moviesAdapter.clear();
            Utilities.listOfMovies.clear();
            progressBar.setVisibility(View.INVISIBLE);

            constructUrl();

            // cancelling asynctask if its already running
            if (asyncForSearches != null)
                asyncForSearches.onCancelled();

            asyncForSearches = new AsyncTaskForMovies();
            asyncForSearches.execute(WEB_URL);
            progressBar.setVisibility(View.VISIBLE);

            Toast.makeText(this, searchText, Toast.LENGTH_SHORT).show();

            /*String SEARCH_BASE_URL = "http://api.themoviedb.org/3/search/movie?&api_key=69b589af19cead810bc805ab8f5363f6&query=";
            Uri searchUri = Uri.parse(SEARCH_BASE_URL).buildUpon()
                    .appendQueryParameter("query", searchText).build();

            AsyncTaskForMovies asyncForSearches = new AsyncTaskForMovies();
            asyncForSearches.execute(searchUri.toString());*/

        }

        return searchText;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        //return super.onCreateOptionsMenu(menu);

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                //Toast.makeText(getApplicationContext(), "Keyup Clicked", Toast.LENGTH_SHORT).show();
                asyncForSearches.onCancelled();
                moviesAdapter.clear();
                Utilities.listOfMovies.clear();
                searchText = null;

                page = 1;
                constructUrl();
                downloadMovies = new AsyncTaskForMovies();
                downloadMovies.execute(WEB_URL);
                progressBar.setVisibility(View.VISIBLE);

            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_item_setting) {
            Intent intent = new Intent(this, SettingsActivityMain.class);
            intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivityMain.GeneralPreferenceFragment.class.getName());
            intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
            startActivity(intent);
        }

        if (item.getItemId() == R.id.menu_sqlite_viewer) {
            Intent intent = new Intent(this, AndroidDatabaseManager.class);
            startActivity(intent);
        }

        if (item.getItemId() == R.id.menu_favorites) {
            Intent intent = new Intent(this, FavouritesActivity.class);
            startActivity(intent);
        }

        if (item.getItemId() == R.id.search) {

        }

        return super.onOptionsItemSelected(item);
    }

/*    private void constructUrl() {
        Uri webUri = Uri.parse(MOVIES_BASE_URL).buildUpon().appendQueryParameter(PAGE_NO_KEY, String.valueOf(page)).build();
        WEB_URL = webUri.toString();
        Log.v(LOG_TAG + " WEB_URL", WEB_URL);
        Toast.makeText(this, "Value of: " + searchText, Toast.LENGTH_SHORT).show();
    }*/

    private void constructUrl() {

        Uri webUri = null;

        if (searchText == null) {
            webUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendQueryParameter(PAGE_NO_KEY, String.valueOf(page)).build();

        } else if (searchText != null) {
            webUri = Uri.parse(SEARCH_BASE_URL).buildUpon()
                    .appendQueryParameter("query", searchText)
                    .appendQueryParameter(PAGE_NO_KEY, String.valueOf(page)).build();
        }

        WEB_URL = webUri.toString().replace(" ","%20");
        Log.v(LOG_TAG + " WEB_URL", WEB_URL);
        Toast.makeText(this, "Value of: " + searchText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<ArrayList<Movies>> onCreateLoader(int id, Bundle args) {
        //Log.v(LOG_TAG + " Activity Callbacks ", "Page :" + String.valueOf(page) + " tempPage :" + String.valueOf(tempPageForLoading));
        return new MoviesLoader(this, WEB_URL, Utilities.FLAG_HEADER);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Movies>> loader, ArrayList<Movies> data) {
        moviesAdapter.clear();
        moviesAdapter.addAll(data);
        isLoaderCompleted = true;
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Movies>> loader) {
        moviesAdapter.clear();
    }

    public void loadLoader() {
        isLoaderCompleted = false;
        loader.initLoader(1, null, this);
    }


    public class AsyncTaskForMovies extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            Utilities.FetchDataFromNet(params[0], Utilities.FLAG_HEADER, getApplicationContext());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            moviesAdapter.clear();
            moviesAdapter.addAll(Utilities.listOfMovies);

            if (Utilities.listOfMovies != null)
                isLoaderCompleted = true;

            progressBar.setVisibility(View.GONE);
            moviesList.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onCancelled() {
            Utilities.listOfMovies.clear();
            moviesAdapter.clear();

            this.cancel(true);
        }
    }

}
