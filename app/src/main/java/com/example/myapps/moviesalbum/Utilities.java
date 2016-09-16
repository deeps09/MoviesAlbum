package com.example.myapps.moviesalbum;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.example.myapps.moviesalbum.sqliteDB.MoviesContract;
import com.example.myapps.moviesalbum.sqliteDB.MoviesDBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by Deepesh_Gupta1 on 08/20/2016.
 */
public class Utilities {
    private static String LOG_TAG = Utilities.class.getSimpleName();
    private static String imageBaseUrl = "http://image.tmdb.org/t/p/w185";
    private static String webURL = "https://api.themoviedb.org/3/discover/movie?primary_release_date.gte=2016-01-01&primary_release_date.lte=2016-10-22&moviesort_by=release_date&api_key=69b589af19cead810bc805ab8f5363f6&page=1";

    public static ArrayList<Movies> listOfMovies = new ArrayList<>();
    public static ArrayList<Movies> tempListOfMoview = new ArrayList<>();
    public static ArrayList<String> reviews = new ArrayList<>();
    public static String videoKey = null;

    public static final String FLAG_VIDEO = "video";
    public static final String FLAG_REVIEW = "review";
    public static final String FLAG_HEADER = "header";

    public static Context mContext;

    public static void FetchDataFromNet(String customUrl, String flag, Context context) {
        URL url;
        Uri uri = Uri.parse(customUrl).buildUpon().build();
        //.appendQueryParameter("page", String.valueOf(page)).build();

        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        String JsonData;

        try {
            url = new URL(uri.toString());
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            JsonData = ReadDataFromStream(inputStream);
            if (flag == FLAG_HEADER)
                extractDataFromJson(JsonData);
            else if (flag == FLAG_REVIEW)
                ReadReviewsOrVideoKeyFromJson(JsonData, flag);
            else if (flag == FLAG_VIDEO)
                ReadReviewsOrVideoKeyFromJson(JsonData, flag);

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, " MalformedURLException Occured ", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, " IOException Occured ", e);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (httpURLConnection != null)
                    httpURLConnection.disconnect();
            } catch (IOException e) {
            }
        }
        //return listOfMovies;
    }

    private static String ReadDataFromStream(InputStream Istream) {
        InputStreamReader inputStreamReader = new InputStreamReader(Istream, Charset.forName("UTF-8"));
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder jsonString = new StringBuilder();

        try {
            String line = bufferedReader.readLine();
            while (line != null) {
                jsonString = jsonString.append(line);
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, " IOException Occured ", e);
        }

        return jsonString.toString();
    }

    private static void ReadReviewsOrVideoKeyFromJson(String JsonString, String Flag) {
        //JsonString = "https://api.themoviedb.org/3/movie/244786/reviews?api_key=69b589af19cead810bc805ab8f5363f6";
        try {
            JSONObject rootObj = new JSONObject(JsonString);
            JSONArray resultsArray = rootObj.getJSONArray("results");

            int i = 0;

            if (Flag == FLAG_REVIEW) {
                reviews.clear();
                while (i < resultsArray.length()) {
                    JSONObject dataObj = resultsArray.getJSONObject(i);
                    reviews.add(dataObj.getString("content"));
                    Log.v(LOG_TAG + " Review ", reviews.get(i));
                    i++;
                }
            } else if (Flag == FLAG_VIDEO) {
                JSONObject dataObj = resultsArray.getJSONObject(i);
                videoKey = dataObj.getString("key");
                Log.v(LOG_TAG + " Video ", videoKey);
            }/* else if (Flag == FLAG_REVIEW && resultsArray.length() == 0){
                reviews.clear();
            }*/

        } catch (JSONException e) {
            Log.e(LOG_TAG, " JSONException Occurred ", e);
            videoKey = null;
        }
        //return reviews;
    }


    private static void extractDataFromJson(String JsonString) {

        try {
            //listOfMovies.clear();
            JSONObject rootObj = new JSONObject(JsonString);
            JSONArray resultsArray = rootObj.getJSONArray("results");
            int i = 0;
            while (i < resultsArray.length()) {
                JSONObject dataObj = resultsArray.getJSONObject(i);
                Bitmap posterImage = null;

                String imagePath = dataObj.getString("poster_path");
                String movieDesc = dataObj.getString("overview");
                String relDate = dataObj.getString("release_date");
                String movieId = dataObj.getString("id");
                String movieTitle = dataObj.getString("original_title");
                String rating = dataObj.getString("vote_average");
                Log.v(LOG_TAG + "imagePath", imagePath );

                if (imagePath != "null")
                    posterImage = DownloadImageFromInternet(imageBaseUrl + imagePath);

                Log.v(LOG_TAG + " JSON ", "\n Poster: " + imageBaseUrl + imagePath +
                        "\n Desc: " + movieDesc +
                        "\n Release Date: " + relDate +
                        "\n Movie ID: " + movieId +
                        "\n Title: " + movieTitle +
                        "\n Rating: " + rating +
                        "\n ---------------------");

                listOfMovies.add(new Movies(movieTitle, relDate, rating, movieId, movieDesc, posterImage));

                byte[] posterBytes = null;
                if (posterImage != null) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    posterImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    posterBytes = outputStream.toByteArray();
                }

                ContentValues contentValues = new ContentValues();
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movieId);
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE, movieTitle);
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_RELEASE_DATE, relDate);
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_RATING, rating);
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_FAVORITE, "N");
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_POSTER, posterBytes);
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_SYNOPSIS, movieDesc);
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TRAILER_URL, "");
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_REVIEWS, "");

                MoviesDBHelper.onInsert(MainActivity.CONTEXT_MAIN_ACTIVITY, contentValues);
                i++;
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, " JSONException Occurred ", e);
        }

        for (Movies movies : listOfMovies) {
            Log.v(LOG_TAG, movies.getMovieTitle());
        }
        Log.v(LOG_TAG, "===================================");

        tempListOfMoview.addAll(listOfMovies);
    }

    public static Bitmap DownloadImageFromInternet(String urlPath) {
        URL url = null;
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        Bitmap bitmap = null;

        try {
            url = new URL(urlPath);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, " MalformedURLException ", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, " IOException ", e);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (httpURLConnection != null)
                    httpURLConnection.disconnect();
            } catch (IOException e) {
                Log.e(LOG_TAG, " IOException ", e);
            }

        }
        return bitmap;
    }

    public static boolean internetState(Context context) {
        boolean netState = false;

        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();

        if (networkInfo != null)
            return networkInfo.isConnectedOrConnecting();

        return netState;
    }

    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }

    }
}
