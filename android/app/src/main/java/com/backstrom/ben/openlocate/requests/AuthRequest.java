package com.backstrom.ben.openlocate.requests;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by benba on 4/8/2017.
 */

public class AuthRequest extends StringRequest {

    public static final String URL_KEY = "URL_KEY";
    public static final String USERNAME_KEY = "USERNAME_KEY";
    public static final String PASSWORD_KEY = "PASSWORD_KEY";

    private String username;
    private String password;

    public AuthRequest(int method, String url,
                       String username, String password,
                       Response.Listener<String> listener,
                       Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        this.username = username;
        this.password = password;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> params = new HashMap<>();
        String creds = String.format("%s:%s", username, password);
        String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
        params.put("Authorization", auth);
        return params;
    }
}
