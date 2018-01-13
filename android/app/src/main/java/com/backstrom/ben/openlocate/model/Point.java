package com.backstrom.ben.openlocate.model;

import android.graphics.Bitmap;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by benba on 3/13/2017.
 */

public class Point {

    public static final String ID = "id";
    public static final String MAP_KEY = "map";
    public static final String NAME_KEY = "name";
    public static final String TIMESTAMP_KEY = "timestamp";
    public static final String LAT_LNG_KEY = "latlng";
    public static final String LAT_KEY = "lat";
    public static final String LNG_KEY = "lng";
    public static final String NOTES_KEY = "notes";
    public static final String ATTACHMENT_KEY = "attachment";

    public long id;
    public String mapUri;
    public Bitmap map;
    public String name;
    public long timestamp;
    public LatLng latLng;
    public String notes;
    public String attachmentUri;
    public Bitmap attachment;

    public Point(long id, Bitmap map, String name, long timestamp, LatLng latLng, String people, Bitmap attachment) {
        this.id = id;
        this.map = map;
        this.name = name;
        this.timestamp = timestamp;
        this.latLng = latLng;
        this.notes = people;
        this.attachment = attachment;
    }

    public Point(long id, String mapUri, String name, long timestamp, LatLng latLng, String notes, String attachmentUri) {
        this.id = id;
        this.mapUri = mapUri;
        this.name = name;
        this.timestamp = timestamp;
        this.latLng = latLng;
        this.notes = notes;
        this.attachmentUri = attachmentUri;
    }

    @Override
    public String toString() {
        return "Point: "+ID+"="+id+", "+
                TIMESTAMP_KEY+"="+timestamp+", "+
                LAT_LNG_KEY+"="+latLng+", "+
                ATTACHMENT_KEY+"="+attachmentUri+", "+
                NOTES_KEY+"="+notes;
    }
}
