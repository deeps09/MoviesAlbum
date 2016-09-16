package com.example.myapps.moviesalbum;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by Deepesh_Gupta1 on 08/26/2016.
 */
public class MoviesCursorAdapter extends CursorAdapter {

    boolean mShowToggle = true;
    int mToggleVisibility = View.VISIBLE;

    public MoviesCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    public MoviesCursorAdapter(Context context, Cursor c, boolean toggle, int toggleVisibility) {
        super(context, c, 0);
        this.mShowToggle = toggle;
        this.mToggleVisibility = toggleVisibility;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_movie, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        byte[] bytes = cursor.getBlob(FavouritesActivity.COL_INDEX_MOVIE_POSTER);
        Bitmap posterImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        viewHolder.fav_toggle_favorites.setVisibility(mToggleVisibility);
        viewHolder.fav_toggle_favorites.setChecked(mShowToggle);
        viewHolder.title.setText(cursor.getString(FavouritesActivity.COL_INDEX_MOVIE_TITLE));
        viewHolder.releaseDate.setText(cursor.getString(FavouritesActivity.COL_INDEX_MOVIE_RELEASE_DATE));
        viewHolder.rating.setText(cursor.getString(FavouritesActivity.COL_INDEX_MOVIE_RATING));
        viewHolder.posterImage.setImageBitmap(posterImage);
    }

    public class ViewHolder {
        public final TextView title;
        public final TextView releaseDate;
        public final TextView rating;
        public final ImageView posterImage;
        public final ToggleButton fav_toggle_favorites;

        public ViewHolder(View view){
            title = (TextView) view.findViewById(R.id.movie_title_tv);
            releaseDate = (TextView) view.findViewById(R.id.release_date_tv);
            rating = (TextView) view.findViewById(R.id.ratings_tv);
            posterImage = (ImageView) view.findViewById(R.id.image_poster);
            fav_toggle_favorites = (ToggleButton) view.findViewById(R.id.fav_toggle_favourites);
        }
    }
}
