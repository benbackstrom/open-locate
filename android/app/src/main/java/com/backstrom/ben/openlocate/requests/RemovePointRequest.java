package com.backstrom.ben.openlocate.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.backstrom.ben.openlocate.model.Point;

/**
 * Created by benba on 1/9/2018.
 */

public class RemovePointRequest extends AuthRequest {

    private Point mPoint;

    public RemovePointRequest(String url,
                              String username, String password,
                              Response.Listener<String> listener,
                              Response.ErrorListener errorListener,
                              Point point) {
        super(Request.Method.POST, url, username, password, listener, errorListener);
        mPoint = point;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return (Point.ID + "=" + mPoint.id).getBytes();
    }
}
