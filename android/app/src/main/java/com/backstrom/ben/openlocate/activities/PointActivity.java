package com.backstrom.ben.openlocate.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.backstrom.ben.openlocate.R;
import com.backstrom.ben.openlocate.fragments.PointFragment;

/**
 * Created by benba on 1/12/2018.
 */

public class PointActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point);

        Fragment fragment = PointFragment.newInstance(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.root, fragment)
                .commit();
    }
}
