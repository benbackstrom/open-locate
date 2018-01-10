package com.backstrom.ben.openlocate.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.backstrom.ben.openlocate.ConvertPointsTask;
import com.backstrom.ben.openlocate.R;
import com.backstrom.ben.openlocate.adapters.PointsAdapter;
import com.backstrom.ben.openlocate.model.Point;
import com.backstrom.ben.openlocate.requests.AddPointRequest;
import com.backstrom.ben.openlocate.requests.AuthRequest;
import com.backstrom.ben.openlocate.requests.RemovePointRequest;

import java.util.ArrayList;
import java.util.List;

import static com.backstrom.ben.openlocate.requests.AuthRequest.PASSWORD_KEY;
import static com.backstrom.ben.openlocate.requests.AuthRequest.URL_KEY;
import static com.backstrom.ben.openlocate.requests.AuthRequest.USERNAME_KEY;

public class MainActivity extends AppCompatActivity implements ConvertPointsTask.Listener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private PointsAdapter mAdapter;
    private FloatingActionButton mButton;
    private FrameLayout mLogin;
    private FrameLayout mEmpty;
    private FrameLayout mProgress;

    private ConvertPointsTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mButton = (FloatingActionButton) findViewById(R.id.create_point);
        mLogin = (FrameLayout) findViewById(R.id.login);
        mEmpty = (FrameLayout) findViewById(R.id.empty_message);
        mProgress = (FrameLayout) findViewById(R.id.progress_bar);

        mAdapter = new PointsAdapter(this, new ArrayList<Point>());
        setSwipeListener(mRecyclerView);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String url = prefs.getString(URL_KEY, null);
                if (url != null) {
                    Intent intent = new Intent(MainActivity.this, SendActivity.class);
                    intent.putExtra(URL_KEY, url);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_up, R.anim.no_animation);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.no_url_title);
                    builder.setMessage(R.string.no_url_message);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.show();
                }
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.no_animation);
            }
        });

        refreshList();
    }

    public void refreshList() {
        mEmpty.setVisibility(View.GONE);
        showProgress();
        RequestQueue queue = Volley.newRequestQueue(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String baseUrl = prefs.getString(URL_KEY, null);
        String username = prefs.getString(USERNAME_KEY, null);
        String password = prefs.getString(PASSWORD_KEY, null);

        String url = baseUrl + "/get-points";

        AuthRequest request = new AuthRequest(Request.Method.POST,
                url,
                username,
                password,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            if (mTask != null) {
                                mTask.cancel(true);
                                mTask = null;
                            }
                            mTask = new ConvertPointsTask(MainActivity.this);
                            mTask.execute(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        logError(error);
                        MainActivity.this.onPointsReceived(new ArrayList<Point>());
                        hideProgress();
                    }
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(
                3*60*1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);
    }

    private void setSwipeListener(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                removeItem(position);
            }
        };
        ItemTouchHelper helper = new ItemTouchHelper(simpleItemTouchCallback);
        helper.attachToRecyclerView(recyclerView);
    }

    public void showProgress() {
        mProgress.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        mProgress.setVisibility(View.GONE);
    }

    @Override
    public void onPointsReceived(List<Point> results) {
        if (results.size() == 0) {
            mEmpty.setVisibility(View.VISIBLE);
        } else {
            mEmpty.setVisibility(View.GONE);
        }
        mAdapter.swapList(results);
        hideProgress();
    }

    private void removeItem(int position) {
        List<Point> points = mAdapter.getDataSet();
        Point toRemove = points.remove(position);
        mAdapter.notifyItemRemoved(position);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String baseUrl = prefs.getString(URL_KEY, null);
        String username = prefs.getString(USERNAME_KEY, null);
        String password = prefs.getString(PASSWORD_KEY, null);

        String url = baseUrl + "/remove-point";

        RequestQueue queue = Volley.newRequestQueue(this);

        RemovePointRequest request = new RemovePointRequest(
                url,
                username,
                password,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this,
                                "Item Deleted",
                                Toast.LENGTH_SHORT)
                                    .show();
                        refreshList();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        logError(error);
                    }
                },
                toRemove
        );
        queue.add(request);
    }

    public void logError(VolleyError error) {
        Log.i(TAG, "------------------------error");
        if (error != null) {
            error.printStackTrace();
            if (error instanceof NoConnectionError) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.no_connection_error_message),
                        Toast.LENGTH_LONG)
                        .show();
            } else if (error instanceof NetworkError) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.network_error_message),
                        Toast.LENGTH_LONG)
                        .show();
            } else if (error instanceof AuthFailureError) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.authentication_error_message),
                        Toast.LENGTH_LONG)
                        .show();
            } else if (error instanceof TimeoutError) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.timeout_error_message),
                        Toast.LENGTH_LONG)
                        .show();
            } else if (error instanceof ServerError) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.server_error_message),
                        Toast.LENGTH_LONG)
                        .show();
            } else {
                if (error.networkResponse != null) {
                    String errorMessage = error.networkResponse.toString();
                    if (errorMessage != null) {
                        Toast.makeText(MainActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }
        }
    }
}
