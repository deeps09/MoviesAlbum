package com.example.myapps.moviesalbum;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ReviewsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        Intent intent = getIntent();

        this.setTitle(intent.getStringExtra(MainActivity.MOVIE_TITLE_KEY)+ " Reviews: ");

        ListView review_listView = (ListView) findViewById(R.id.reviews_listview);
        TextView noReviews_tv = (TextView) findViewById(R.id.no_reviews_tv);

        ArrayAdapter<String> reviewAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, DetailActivity.mReviews);
        review_listView.setAdapter(reviewAdapter);

        review_listView.setEmptyView(noReviews_tv);
        noReviews_tv.setText("No Reviews Available");
    }
}
