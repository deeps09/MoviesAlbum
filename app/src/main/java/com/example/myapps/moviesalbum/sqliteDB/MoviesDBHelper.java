package com.example.myapps.moviesalbum.sqliteDB;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.ToggleButton;

import com.example.myapps.moviesalbum.sqliteDB.MoviesContract.MoviesEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Deepesh_Gupta1 on 08/24/2016.
 */
public class MoviesDBHelper extends SQLiteOpenHelper {

    static String REVIEWS_KEY_FOR_JSON = "reviews_json";
    static final String DATABASE_NAME = "movies.db";
    static final int DATABASE_VERSION = 5;
    static final String LOG_TAG = MoviesDBHelper.class.getSimpleName();

    public MoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesEntry.TABLE_NAME + " (" +
                MoviesEntry.COLUMN_MOVIE_ID + " INTEGER PRIMARY KEY, " +
                MoviesEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_MOVIE_RELEASE_DATE + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_MOVIE_RATING + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_MOVIE_FAVORITE + " CHAR NOT NULL, " +
                MoviesEntry.COLUMN_MOVIE_POSTER + " BLOB, " +
                MoviesEntry.COLUMN_MOVIE_SYNOPSIS + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_MOVIE_TRAILER_URL + " TEXT, " +
                MoviesEntry.COLUMN_MOVIE_REVIEWS + " TEXT " + ") ";

        db.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MoviesEntry.TABLE_NAME);
        onCreate(db);
    }

    public static void onInsert(Context context, ContentValues contentValues) {  //, String MovieId) {

        String movieId = contentValues.getAsString(MoviesEntry.COLUMN_MOVIE_ID);
        String favYn = contentValues.getAsString(MoviesEntry.COLUMN_MOVIE_FAVORITE);
        Cursor cursor = null;

        MoviesDBHelper dbHelper = new MoviesDBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        cursor = db.query(MoviesEntry.TABLE_NAME, new String[]{MoviesEntry.COLUMN_MOVIE_ID},
                MoviesEntry.COLUMN_MOVIE_ID + " = ?", new String[]{movieId}, null, null, null);

        if (favYn == "N") {
            if (cursor.getCount() == 0)
                db.insertOrThrow(MoviesEntry.TABLE_NAME, null, contentValues);
        } else {

            String CursorData = DatabaseUtils.dumpCursorToString(cursor);

            Log.v(LOG_TAG + " Cursor ", CursorData);

            try {
                if (cursor.getCount() == 0) {
                    db.insertOrThrow(MoviesEntry.TABLE_NAME, null, contentValues);
                    Log.v(LOG_TAG + " DB Txn ", " Row Inserted");
                } else {
                    db.update(MoviesEntry.TABLE_NAME, contentValues, MoviesEntry.COLUMN_MOVIE_ID + " = ? ",
                            new String[]{movieId});
                    Log.v(LOG_TAG + " DB Txn ", " Row Updated");
                }
            } catch (SQLException e) {
                Log.e(LOG_TAG, "SQLexception occured while inserting", e);
            }
        }

        if (cursor != null)
            cursor.close();
        db.close();
        dbHelper.close();
    }

    // For saving reviews in DB
    public static String convertReviewsToJson(ArrayList<String> myArrayList) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(REVIEWS_KEY_FOR_JSON, new JSONArray(myArrayList.toArray()));
        } catch (JSONException e) {
            Log.v(LOG_TAG, "JSONException occurred in convertReviewsToJson", e);
        }

        String arrayList = jsonObject.toString();

        return arrayList;
    }

    // For displaying reviews on view
    public static ArrayList<String> convertJsonToReviews(String JsonString) {
        ArrayList<String> reviews = new ArrayList<>();

        try {
            JSONObject rootObj = new JSONObject(JsonString);
            JSONArray reviewsArray = rootObj.getJSONArray(REVIEWS_KEY_FOR_JSON);

            int i = 0;
            while (i < reviewsArray.length()) {
                reviews.add(reviewsArray.getString(i));
                i++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public static void deleteFromFavorites(final Context context, final String movieId, String movieTitle, final ToggleButton toggleButton) {

        new AlertDialog.Builder(context)
                .setTitle("Confirm")
                .setMessage("Confirm to un-favourite " + movieTitle + "?")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        MoviesDBHelper moviesDBHelper = new MoviesDBHelper(context);
                        SQLiteDatabase db = moviesDBHelper.getWritableDatabase();

                        int count = db.delete(MoviesEntry.TABLE_NAME, MoviesEntry.COLUMN_MOVIE_ID + " = ?", new String[]{movieId});

                        if (count == 0)
                            Log.v(LOG_TAG + " DB Txn ", "Error occurred in deletion");
                        else if (count > 0)
                            Log.v(LOG_TAG + " DB Txn ", "Deletion Done!");

                        moviesDBHelper.close();
                        db.close();

                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (toggleButton != null)
                    toggleButton.setChecked(true);
            }
        }).setCancelable(false).show();

    }

    public static void onDelete(Context context, String rawQuery) {

        MoviesDBHelper moviesDBHelper = new MoviesDBHelper(context);
        SQLiteDatabase db = moviesDBHelper.getWritableDatabase();

        db.execSQL(rawQuery);

        db.close();
        moviesDBHelper.close();

      /*  if (count == 0)
            Log.v(LOG_TAG + " DB Txn ", "Error occurred in deletion");
        else if (count > 0)
            Log.v(LOG_TAG + " DB Txn ", "Deletion Done!");*/

    }

    public static boolean getFavourites(Context context, String movieId) {

        MoviesDBHelper moviesDBHelper = new MoviesDBHelper(context);
        SQLiteDatabase readableDB = moviesDBHelper.getReadableDatabase();

        Cursor cursor = readableDB.query(MoviesEntry.TABLE_NAME, new String[]{MoviesEntry.COLUMN_MOVIE_ID},
                MoviesEntry.COLUMN_MOVIE_ID + " = ?" + " AND " + MoviesEntry.COLUMN_MOVIE_FAVORITE + " = ?",
                new String[]{movieId, "Y"}, null, null, null);
        int count = cursor.getCount();

        cursor.close();
        readableDB.close();

        if (count == 0)
            return false;
        else
            return true;

    }

    //Helper functon for sqlite viewer

    public ArrayList<Cursor> getData(String Query) {
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[]{"mesage"};
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2 = new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try {
            String maxQuery = Query;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[]{"Success"});

            alc.set(1, Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0, c);
                c.moveToFirst();

                return alc;
            }
            return alc;
        } catch (SQLException sqlEx) {
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + sqlEx.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        } catch (Exception ex) {

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + ex.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        }


    }
}
