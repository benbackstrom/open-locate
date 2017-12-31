package com.backstrom.ben.openlocate.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.backstrom.ben.openlocate.R;
import com.backstrom.ben.openlocate.requests.AuthRequest;

import java.util.HashMap;
import java.util.Map;

import static com.backstrom.ben.openlocate.requests.AuthRequest.PASSWORD_KEY;
import static com.backstrom.ben.openlocate.requests.AuthRequest.URL_KEY;
import static com.backstrom.ben.openlocate.requests.AuthRequest.USERNAME_KEY;

/**
 * Created by benba on 3/13/2017.
 */

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private ImageView mCloseView;
    private EditText mUrlView;
    private EditText mUserView;
    private EditText mPasswordView;
    private Button mConnect;
    private TextView mErrorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mCloseView = (ImageView) findViewById(R.id.close);
        mUrlView = (EditText) findViewById(R.id.url_name);
        mUserView = (EditText) findViewById(R.id.user_name);
        mPasswordView = (EditText) findViewById(R.id.password);
        mConnect = (Button) findViewById(R.id.connect);
        mErrorView = (TextView) findViewById(R.id.error);

        mErrorView.setVisibility(View.GONE);

        mCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right);
            }
        });

        mConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = mUrlView.getText().toString();
                String username = mUserView.getText().toString();
                String password = mPasswordView.getText().toString();
                startRequest(url, username, password);
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String url = prefs.getString(URL_KEY, null);
        String username = prefs.getString(USERNAME_KEY, null);
        String password = prefs.getString(PASSWORD_KEY, null);

        if (url != null && username != null && password != null) {
            mUrlView.setText(url);
            mUserView.setText(username);
        }
    }

    public void startRequest(final String url, final String username, final String password) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String loginUrl = url + "/login";

        AuthRequest stringRequest = new AuthRequest(Request.Method.GET, loginUrl,
                username, password,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mErrorView.setVisibility(View.GONE);
                        Log.i(TAG, "Response is: "+ response);
                        if (response.equals("Access granted")) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(URL_KEY, url);
                            editor.putString(USERNAME_KEY, username);
                            editor.putString(PASSWORD_KEY, password);
                            editor.apply();

                            Toast.makeText(LoginActivity.this,
                                    getString(R.string.success),
                                    Toast.LENGTH_LONG)
                                    .show();
                            finish();
                            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "That didn't work!");
                        mErrorView.setVisibility(View.VISIBLE);
                        mErrorView.setText(error.getLocalizedMessage());
                    }
                });

        queue.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right);
    }
}
