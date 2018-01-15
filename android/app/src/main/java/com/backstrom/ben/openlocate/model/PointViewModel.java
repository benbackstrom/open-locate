package com.backstrom.ben.openlocate.model;

import android.arch.lifecycle.ViewModel;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by benba on 1/15/2018.
 */

public class PointViewModel extends ViewModel {

    private Point mPoint;

    public Point getPoint(Bundle args) {
        if (mPoint == null)
            createPoint(args);

        return mPoint;
    }

    public void createPoint(Bundle args) {
        long id = args.getLong(Point.ID, -1);
        String name = args.getString(Point.NAME_KEY);
        String mapUri = args.getString(Point.MAP_KEY);
        String attachmentUri = args.getString(Point.ATTACHMENT_KEY);
        long timestamp = args.getLong(Point.TIMESTAMP_KEY, -1);
        double lat = args.getDouble(Point.LAT_KEY);
        double lng = args.getDouble(Point.LNG_KEY);
        String notes = args.getString(Point.NOTES_KEY, null);
        LatLng latLng = new LatLng(lat, lng);

        mPoint = new Point(id, mapUri, name, timestamp, latLng, notes, attachmentUri);
    }
}
