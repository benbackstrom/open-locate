package com.backstrom.ben.openlocate.activities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.backstrom.ben.openlocate.R;
import com.backstrom.ben.openlocate.adapters.PointsAdapter;
import com.backstrom.ben.openlocate.model.Point;
import com.squareup.picasso.Picasso;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

import static com.backstrom.ben.openlocate.requests.AuthRequest.URL_KEY;

/**
 * Created by benba on 5/22/2017.
 */

public class ImageZoomActivity extends AppCompatActivity {

    private View mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_zoom);

        mBack = findViewById(R.id.back);
        ImageViewTouch imageView = (ImageViewTouch) findViewById(R.id.image_view);

        mBack.setOnClickListener((View view) ->
                finish());

        String url = getIntent().getExtras().getString(Point.ATTACHMENT_KEY);

        if (url != null) {
            Picasso.with(this)
                    .load(url)
                    .into(imageView);
        } else {
            Toast.makeText(this,
                    getString(R.string.image_zoom_issue),
                    Toast.LENGTH_LONG).show();
        }
    }
}
