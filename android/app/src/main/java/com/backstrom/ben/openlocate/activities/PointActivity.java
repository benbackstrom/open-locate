package com.backstrom.ben.openlocate.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.backstrom.ben.openlocate.R;
import com.backstrom.ben.openlocate.fragments.PointFragment;
import com.backstrom.ben.openlocate.model.Point;

/**
 * Created by benba on 1/12/2018.
 */

public class PointActivity extends AppCompatActivity {

    private TextView mTitleView;
    private View mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point);

        String title = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            title = extras.getString(Point.NAME_KEY, getString(R.string.app_name));

        mTitleView = (TextView) findViewById(R.id.title_view);
        mBack = findViewById(R.id.back);

        mTitleView.setText(title);
        mBack.setOnClickListener((View v) -> finish());

        Fragment fragment = PointFragment.newInstance(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.root, fragment)
                .commit();
    }
}
