package com.backstrom.ben.openlocate.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.backstrom.ben.openlocate.R;
import com.backstrom.ben.openlocate.model.Point;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by benba on 2/3/2018.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private SupportMapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        double lat = 0;
        double lng = 0;
        String name = "";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lat = extras.getDouble(Point.LAT_KEY);
            lng = extras.getDouble(Point.LNG_KEY);
            name = extras.getString(Point.NAME_KEY, "");
        }

        LatLng latLng = new LatLng(lat, lng);

        MarkerOptions options = new MarkerOptions();
        options.title(name);
        options.position(latLng);

        googleMap.addMarker(options);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
    }
}
