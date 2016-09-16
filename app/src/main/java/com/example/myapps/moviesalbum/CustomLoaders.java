package com.example.myapps.moviesalbum;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.myapps.moviesalbum.sqliteDB.MoviesContract;
import com.example.myapps.moviesalbum.sqliteDB.MoviesDBHelper;

import java.util.ArrayList;

/**
 * Created by Deepesh_Gupta1 on 08/26/2016.
 */
public class CustomLoaders {

    public static class MoviesLoader extends AsyncTaskLoader<ArrayList<Movies>> {

        String mUrl, mFlag;

        public MoviesLoader(Context context, String url, String flag) {
            super(context);
            this.mUrl = url;
            this.mFlag = flag;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public ArrayList<Movies> loadInBackground() {
            //ArrayList<Movies> moviesArrayList = new ArrayList<>();
            Utilities.FetchDataFromNet(mUrl, mFlag, getContext());

            // Called function will put date in listofMovies object of Utilities Class
            return Utilities.tempListOfMoview;
        }
    }

    public static class DetailLoader extends AsyncTaskLoader {
        String mUrl;
        String mFlag;

        public DetailLoader(Context context, String url, String flag) {
            super(context);
            this.mUrl = url;
            this.mFlag = flag;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public Object loadInBackground() {
            Utilities.FetchDataFromNet(mUrl, mFlag, getContext());
            return null;
        }
    }

    public static class FavouritesLoader extends AsyncTaskLoader{
        Context myContext;
        static MoviesDBHelper dbHelper;
        static SQLiteDatabase database;
        static Cursor cursor;

        public FavouritesLoader(Context context) {
            super(context);
            myContext = context;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public Cursor loadInBackground() {
            dbHelper = new MoviesDBHelper(myContext);
            database = dbHelper.getReadableDatabase();

            cursor =  database.query(MoviesContract.MoviesEntry.TABLE_NAME, null, MoviesContract.MoviesEntry.COLUMN_MOVIE_FAVORITE + " = ?",
                    new String[] {"Y"}, null, null, null);

            Log.v("Dumped Cursor", DatabaseUtils.dumpCursorToString(cursor));

            return cursor;
        }
    }
}
