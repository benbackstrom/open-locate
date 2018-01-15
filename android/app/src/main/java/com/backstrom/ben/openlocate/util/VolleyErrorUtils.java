package com.backstrom.ben.openlocate.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.backstrom.ben.openlocate.R;

/**
 * Created by benba on 1/15/2018.
 */

public class VolleyErrorUtils {

    public static final String TAG = VolleyErrorUtils.class.getSimpleName();

    public static void logError(Context context, VolleyError error) {
        Log.i(TAG, "------------------------error");
        if (error != null) {
            error.printStackTrace();
            if (error instanceof NoConnectionError) {
                Toast.makeText(context,
                        context.getString(R.string.no_connection_error_message),
                        Toast.LENGTH_LONG)
                        .show();
            } else if (error instanceof NetworkError) {
                Toast.makeText(context,
                        context.getString(R.string.network_error_message),
                        Toast.LENGTH_LONG)
                        .show();
            } else if (error instanceof AuthFailureError) {
                Toast.makeText(context,
                        context.getString(R.string.authentication_error_message),
                        Toast.LENGTH_LONG)
                        .show();
            } else if (error instanceof TimeoutError) {
                Toast.makeText(context,
                        context.getString(R.string.timeout_error_message),
                        Toast.LENGTH_LONG)
                        .show();
            } else if (error instanceof ServerError) {
                Toast.makeText(context,
                        context.getString(R.string.server_error_message),
                        Toast.LENGTH_LONG)
                        .show();
            } else {
                if (error.networkResponse != null) {
                    String errorMessage = error.networkResponse.toString();
                    if (errorMessage != null) {
                        Toast.makeText(context,
                                errorMessage,
                                Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }
        }
    }
}
