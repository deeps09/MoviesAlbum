package com.example.myapps.moviesalbum;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

import com.example.myapps.moviesalbum.CustomLoaders.FavouritesLoader;
import com.example.myapps.moviesalbum.sqliteDB.MoviesDBHelper;

public class FavouritesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static GridView moviesList_new;
    public static ProgressBar progressBar;
    ToggleButton fav_toggle_favorites;
    static Cursor myCursorData;
    static final String LOG_TAG = FavouritesActivity.class.getSimpleName();
    int cursorSize = 0;

    public static int COL_INDEX_MOVIE_ID = 0;
    public static int COL_INDEX_MOVIE_TITLE = 1;
    public static int COL_INDEX_MOVIE_RELEASE_DATE = 2;
    public static int COL_INDEX_MOVIE_RATING = 3;
    public static int COL_INDEX_FAV_YN = 4;
    public static int COL_INDEX_MOVIE_POSTER = 5;
    public static int COL_INDEX_MOVIE_SYNOPSIS = 6;
    public static int COL_INDEX_MOVIE_TRAILER_URL = 7;
    public static int COL_INDEX_MOVIE_REVIEWS = 8;
    public static String FLAG_INTERNET_KEY = "no_internet";
    int gridViewCurrentPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        Log.d(LOG_TAG, "onCreate");
/*
        if (savedInstanceState != null)
            gridViewCurrentPos = savedInstanceState.getInt("current_position");*/

        moviesList_new = (GridView) findViewById(R.id.movies_gridview_new);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar_main);

        moviesList_new.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int initialPos = myCursorData.getPosition();
                myCursorData.moveToPosition(position);
                gridViewCurrentPos = position;
                Log.v("Cursor Dumped", DatabaseUtils.dumpCursorToString(myCursorData));

                Intent detailsIntent = new Intent(getApplicationContext(), DetailActivity.class);
                detailsIntent.putExtra(MainActivity.MOVIE_ID, String.valueOf(myCursorData.getInt(COL_INDEX_MOVIE_ID)));
                detailsIntent.putExtra(MainActivity.POSTER_KEY, myCursorData.getBlob(COL_INDEX_MOVIE_POSTER));
                detailsIntent.putExtra(MainActivity.MOVIE_DESC_KEY, myCursorData.getString(COL_INDEX_MOVIE_SYNOPSIS));
                detailsIntent.putExtra(MainActivity.MOVIE_TITLE_KEY, myCursorData.getString(COL_INDEX_MOVIE_TITLE));
                detailsIntent.putExtra(MainActivity.AVG_RATING_KEY, myCursorData.getString(COL_INDEX_MOVIE_RATING));
                detailsIntent.putExtra(MainActivity.RELEASE_DATE_KEY, myCursorData.getString(COL_INDEX_MOVIE_RELEASE_DATE));
                detailsIntent.putExtra(MainActivity.TRAILER_URL_KEY, myCursorData.getString(COL_INDEX_MOVIE_TRAILER_URL));
                detailsIntent.putStringArrayListExtra(MainActivity.REVIEWS_KEY,
                        MoviesDBHelper.convertJsonToReviews(myCursorData.getString(COL_INDEX_MOVIE_REVIEWS)));
                detailsIntent.putExtra(FLAG_INTERNET_KEY, false);
                detailsIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                Log.v("GridView", myCursorData.getString(COL_INDEX_MOVIE_TITLE));
                startActivity(detailsIntent);

                myCursorData.moveToPosition(initialPos);
            }
        });
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(4, null, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // To handle the current position of fridview
        if (gridViewCurrentPos == 0)
            gridViewCurrentPos = moviesList_new.getFirstVisiblePosition();
        else
            gridViewCurrentPos = gridViewCurrentPos;
        cursorSize = myCursorData.getCount();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new FavouritesLoader(this);
    }

    @Override
    public void onLoadFinished(Loader loader, final Cursor data) {
        Log.d(LOG_TAG, "onLoadFinished");
        myCursorData = data;
        progressBar.setVisibility(View.GONE);

        // To avoid populating listview again on pause and restart
        if (cursorSize != data.getCount()) {
            MoviesCursorAdapter cursorAdapter = new MoviesCursorAdapter(this, data);
            moviesList_new.setAdapter(cursorAdapter);
            moviesList_new.setSelection(gridViewCurrentPos);
            Log.d(LOG_TAG, String.valueOf(gridViewCurrentPos));
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        myCursorData = null;
    }

/*    public ArrayList<String> convertJsonToReviews(String JsonString) {
        ArrayList<String> reviews = new ArrayList<>();

        try {
            JSONObject rootObj = new JSONObject(JsonString);
            JSONArray reviewsArray = rootObj.getJSONArray(DetailActivity.REVIEWS_KEY_FOR_JSON);

            int i = 0;
            while (i < reviewsArray.length()) {
                reviews.add(reviewsArray.getString(i));
                i++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reviews;
    }*/
}
