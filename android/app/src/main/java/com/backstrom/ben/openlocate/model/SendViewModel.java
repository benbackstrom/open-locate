package com.backstrom.ben.openlocate.model;

import android.arch.lifecycle.ViewModel;
import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by benba on 1/15/2018.
 */

public class SendViewModel extends ViewModel {
    
    private Bitmap mBitmap;
    private String mPlaceName;
    private String mNotes;
    private LatLng mLatLng;
    private String mUrl;

    public void clear() {
        mBitmap = null;
        mPlaceName = null;
        mNotes = null;
        mLatLng = null;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public String getPlaceName() {
        return mPlaceName;
    }

    public void setPlaceName(String placeName) {
        mPlaceName = placeName;
    }

    public String getNotes() {
        return mNotes;
    }

    public void setNotes(String notes) {
        mNotes = notes;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }

    public void setLatLng(LatLng latLng) {
        mLatLng = latLng;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }
}
