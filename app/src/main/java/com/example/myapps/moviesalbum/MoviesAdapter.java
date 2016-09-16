package com.example.myapps.moviesalbum;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Deepesh_Gupta1 on 08/20/2016.
 */
public class MoviesAdapter extends ArrayAdapter<Movies> {
    Context myContext;

    public MoviesAdapter(Context context, int resource, ArrayList<Movies> objects) {
        super(context, resource, objects);
        this.myContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = null;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_movie, parent, false);
        } else {
            view = convertView;
        }


        Movies movies = getItem(position);

        TextView title = (TextView) view.findViewById(R.id.movie_title_tv);
        title.setText(movies.getMovieTitle());

        TextView releaseDate = (TextView) view.findViewById(R.id.release_date_tv);
        releaseDate.setText(movies.getReleaseDate());

        TextView rating = (TextView) view.findViewById(R.id.ratings_tv);
        rating.setText(movies.getRating());

        ImageView posterImage = (ImageView) view.findViewById(R.id.image_poster);
        posterImage.setImageBitmap(movies.getImagePoster());

        return view;
    }


}
