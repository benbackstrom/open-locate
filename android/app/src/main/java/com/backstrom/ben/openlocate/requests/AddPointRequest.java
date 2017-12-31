package com.backstrom.ben.openlocate.requests;

import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.backstrom.ben.openlocate.model.Point;
import com.backstrom.ben.openlocate.util.BitmapUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by benba on 4/18/2017.
 */

public class AddPointRequest extends AuthRequest {

    private static final String TAG = AddPointRequest.class.getSimpleName();

    private Point mPoint;
    private String mAttachment;

    public AddPointRequest(String url,
                           String username, String password,
                           Response.Listener<String> listener,
                           Response.ErrorListener errorListener,
                           Point point) {
        super(Request.Method.POST, url, username, password, listener, errorListener);
        mPoint = point;
        if (mPoint.attachment != null)
            mAttachment = BitmapUtils.bitmapToString(mPoint.attachment);
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        StringBuilder builder = new StringBuilder();
        builder.append(Point.NAME_KEY);
        builder.append("=");
        builder.append(mPoint.name);

        builder.append("&");
        builder.append(Point.TIMESTAMP_KEY);
        builder.append("=");
        builder.append(String.valueOf(mPoint.timestamp));

        builder.append("&");
        builder.append(Point.LAT_KEY);
        builder.append("=");
        builder.append(mPoint.latLng.latitude);

        builder.append("&");
        builder.append(Point.LNG_KEY);
        builder.append("=");
        builder.append(mPoint.latLng.longitude);

        builder.append("&");
        builder.append(Point.NOTES_KEY);
        builder.append("=");
        builder.append(mPoint.notes);

        if (mAttachment != null) {
            builder.append("&");
            builder.append(Point.ATTACHMENT_KEY);
            builder.append("=");
            builder.append(mAttachment);
        }
        String params = builder.toString();
        Log.i(TAG, params);
        return params.getBytes();
    }
}
