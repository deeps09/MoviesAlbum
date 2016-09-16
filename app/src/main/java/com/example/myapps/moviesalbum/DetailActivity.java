package com.example.myapps.moviesalbum;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.myapps.moviesalbum.CustomLoaders.DetailLoader;
import com.example.myapps.moviesalbum.sqliteDB.MoviesContract.MoviesEntry;
import com.example.myapps.moviesalbum.sqliteDB.MoviesDBHelper;

import java.util.ArrayList;


public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks {
    ImageView imageViewDetail;
    TextView review_one;
    TextView view_all;
    TextView review_one_label;
    String mMovieTitle;
    ToggleButton fav_toggle;
    String mMovieId;
    String mRating;
    String mReleaseDate;
    String mMovieSynopsis;
    Uri mVideoUri;
    public static ArrayList<String> mReviews;
    Boolean mInternetFlag; // Used for taking care of showing data from DB and Internet
    byte[] mPosterImageInByte;

    static String BASE_URL_FOR_VIDEO_REVIEWS = "https://api.themoviedb.org/3/movie";
    static String API_KEY_LBL = "api_key";
    static String API_KEY = "69b589af19cead810bc805ab8f5363f6";
    static String YOUTUBE_BASE_URI = "https://www.youtube.com/watch";
    static String REVIEWS_KEY_FOR_JSON = "reviews_json";
    static String LOG_TAG = DetailActivity.class.getSimpleName();
    Uri videoUriForKey, reviewsUri;

    // Database Column Indexes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        review_one = (TextView) findViewById(R.id.review_one_tv);
        view_all = (TextView) findViewById(R.id.view_all_tv);
        review_one_label = (TextView) findViewById(R.id.reviews_label_tv);
        imageViewDetail = (ImageView) findViewById(R.id.image_poster_detail);
        fav_toggle = (ToggleButton) findViewById(R.id.fav_toggle);

        // Receiving intent data
        final Intent intent = getIntent();
        mInternetFlag = intent.getBooleanExtra(FavouritesActivity.FLAG_INTERNET_KEY, true);
        mMovieId = intent.getStringExtra(MainActivity.MOVIE_ID);
        mPosterImageInByte = intent.getByteArrayExtra(MainActivity.POSTER_KEY);
        mMovieTitle = intent.getStringExtra(MainActivity.MOVIE_TITLE_KEY);
        mRating = intent.getStringExtra(MainActivity.AVG_RATING_KEY);
        mReleaseDate = intent.getStringExtra(MainActivity.RELEASE_DATE_KEY);
        mMovieSynopsis = intent.getStringExtra(MainActivity.MOVIE_DESC_KEY);

        if (mInternetFlag == false) {
            mReviews = intent.getStringArrayListExtra(MainActivity.REVIEWS_KEY);
            mVideoUri = Uri.parse(intent.getStringExtra(MainActivity.TRAILER_URL_KEY));
        }


        this.setTitle(mMovieTitle);
        fav_toggle.setChecked(MoviesDBHelper.getFavourites(this, mMovieId));

        // Converting inte byte array to Bitmap
        imageViewDetail.setImageBitmap(BitmapFactory.decodeByteArray(mPosterImageInByte, 0, mPosterImageInByte.length));

        if (mInternetFlag == true) {

            videoUriForKey = Uri.parse(BASE_URL_FOR_VIDEO_REVIEWS).buildUpon()
                    .appendPath(mMovieId).appendPath("videos")
                    .appendQueryParameter(API_KEY_LBL, API_KEY).build();

            reviewsUri = Uri.parse(BASE_URL_FOR_VIDEO_REVIEWS).buildUpon()
                    .appendPath(mMovieId).appendPath("reviews")
                    .appendQueryParameter(API_KEY_LBL, API_KEY).build();

            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(2, null, this); // video data
            loaderManager.initLoader(3, null, this); // review data
        } else {
            performLoaderTasks(3);
        }

        TextView movieDesc = (TextView) findViewById(R.id.synopsis_tv);
        movieDesc.setText(mMovieSynopsis);

        TextView youtube_link = (TextView) findViewById(R.id.youtube_link_tv);
        youtube_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PackageManager packageManager = getApplicationContext().getPackageManager();
                Intent utubeIntent = new Intent(Intent.ACTION_VIEW, mVideoUri);

                if (utubeIntent.resolveActivity(packageManager) != null) {
                    if (Utilities.videoKey != null)
                        startActivity(utubeIntent);
                    else
                        Toast.makeText(getApplicationContext(), "No Trailer Available", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), "Offline Data", Toast.LENGTH_SHORT).show();
            }
        });

        fav_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked)
                    InsertMoviesData();
                else
                    MoviesDBHelper.deleteFromFavorites(DetailActivity.this, mMovieId, mMovieTitle, fav_toggle);
            }
        });
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        Loader loader = null;

        if (id == 2) {
            loader = new DetailLoader(getApplicationContext(), videoUriForKey.toString(), "video");
        } else if (id == 3) {
            loader = new DetailLoader(getApplicationContext(), reviewsUri.toString(), "review");
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        mReviews = Utilities.reviews;
        performLoaderTasks(loader.getId());
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    public void loadReviewIntent() {
        Intent intent1 = new Intent(getApplicationContext(), ReviewsActivity.class);
        intent1.putExtra(MainActivity.MOVIE_TITLE_KEY, mMovieTitle);
        startActivity(intent1);
    }

    public void InsertMoviesData() {

        ContentValues contentValues = new ContentValues();
        contentValues.put(MoviesEntry.COLUMN_MOVIE_ID, mMovieId);
        contentValues.put(MoviesEntry.COLUMN_MOVIE_TITLE, mMovieTitle);
        contentValues.put(MoviesEntry.COLUMN_MOVIE_RELEASE_DATE, mReleaseDate);
        contentValues.put(MoviesEntry.COLUMN_MOVIE_RATING, mRating);
        contentValues.put(MoviesEntry.COLUMN_MOVIE_FAVORITE, "Y");
        contentValues.put(MoviesEntry.COLUMN_MOVIE_POSTER, mPosterImageInByte);
        contentValues.put(MoviesEntry.COLUMN_MOVIE_SYNOPSIS, mMovieSynopsis);
        contentValues.put(MoviesEntry.COLUMN_MOVIE_TRAILER_URL, mVideoUri.toString());
        contentValues.put(MoviesEntry.COLUMN_MOVIE_REVIEWS, MoviesDBHelper.convertReviewsToJson(mReviews));

        MoviesDBHelper.onInsert(this, contentValues);  //, mMovieId);

    }

    public void performLoaderTasks(int loaderId) {
        if (loaderId == 2) {
            mVideoUri = Uri.parse(YOUTUBE_BASE_URI).buildUpon().appendQueryParameter("v", Utilities.videoKey).build();
            Log.v(LOG_TAG + "Video URI", mVideoUri.toString());
        }

        if (loaderId == 3 && mReviews != null) {
            int sizeOfArray = mReviews.size();
            if (sizeOfArray > 0) {
                view_all.setVisibility(View.VISIBLE);
                review_one_label.setText(review_one_label.getText() + "(" + String.valueOf(sizeOfArray) + ")");
                review_one.setText(mReviews.get(0));

                view_all.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadReviewIntent();
                    }
                });
            } else {
                review_one.setText("No Reviews available");
                view_all.setVisibility(View.GONE);
            }
            fav_toggle.setVisibility(View.VISIBLE);
        }
    }
}
