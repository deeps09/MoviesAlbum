package com.example.myapps.moviesalbum;

import android.graphics.Bitmap;

/**
 * Created by Deepesh_Gupta1 on 08/20/2016.
 */
public class Movies {

    private String mMovieTitle;
    private String mReleaseDate;
    private String mRating;
    private String mMovieID;
    private String mDesc;
    private Bitmap mImagePoster;

    public Movies(String MovieTitle, String ReleaseDate, String Rating, String MovieID, String Desc, Bitmap ImagePoster) {
        this.mMovieTitle = MovieTitle;
        this.mReleaseDate = ReleaseDate;
        this.mRating = Rating;
        this.mMovieID = MovieID;
        this.mDesc = Desc;
        this.mImagePoster = ImagePoster;
    }

    public String getMovieTitle() {
        return mMovieTitle;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public String getRating() {
        return mRating;
    }

    public String getMovieID() {
        return mMovieID;
    }

    public Bitmap getImagePoster() {
        return mImagePoster;
    }

    public String getDesc() {
        return mDesc;
    }

}
