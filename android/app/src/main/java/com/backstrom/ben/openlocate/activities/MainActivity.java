package com.backstrom.ben.openlocate.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.backstrom.ben.openlocate.model.PointsListViewModel;
import com.backstrom.ben.openlocate.tasks.ConvertPointsTask;
import com.backstrom.ben.openlocate.R;
import com.backstrom.ben.openlocate.adapters.PointsAdapter;
import com.backstrom.ben.openlocate.model.Point;
import com.backstrom.ben.openlocate.requests.AuthRequest;
import com.backstrom.ben.openlocate.requests.RemovePointRequest;
import com.backstrom.ben.openlocate.util.VolleyErrorUtils;

import java.util.ArrayList;
import java.util.List;

import static com.backstrom.ben.openlocate.requests.AuthRequest.PASSWORD_KEY;
import static com.backstrom.ben.openlocate.requests.AuthRequest.URL_KEY;
import static com.backstrom.ben.openlocate.requests.AuthRequest.USERNAME_KEY;

public class MainActivity extends AppCompatActivity implements ConvertPointsTask.Listener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SwipeRefreshLayout mSwipeRefresh;
    private RecyclerView mRecyclerView;
    private PointsAdapter mAdapter;
    private FloatingActionButton mButton;
    private FrameLayout mLogin;
    private FrameLayout mEmpty;
    private View mProgress;

    private PointsListViewModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeRefresh = findViewById(R.id.swipe_refresh);
        mRecyclerView = findViewById(R.id.recycler_view);
        mButton = findViewById(R.id.create_point);
        mLogin = findViewById(R.id.login);
        mEmpty = findViewById(R.id.empty_message);
        mProgress = findViewById(R.id.progress_bar);

        mAdapter = new PointsAdapter(this, new ArrayList<>());
        setSwipeListener(mRecyclerView);

        mModel = ViewModelProviders.of(this).get(PointsListViewModel.class);
        mModel.getLiveData(getApplicationContext()).observe(
            this,
            (@Nullable List<Point> points) -> {
                if (points != null && points.size() > 0)
                    mEmpty.setVisibility(View.GONE);
                else
                    mEmpty.setVisibility(View.VISIBLE);

                mAdapter.swapList(points);
                hideProgress();
            }
        );
        showProgress();

        mSwipeRefresh.setOnRefreshListener(() -> mModel.forceRefresh(getApplicationContext()) );

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        mButton.setOnClickListener((View view) -> {
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
                builder.setPositiveButton(
                    R.string.ok,
                    (DialogInterface dialogInterface, int i) ->
                        dialogInterface.dismiss()
                );
                builder.show();
            }
        });

        mLogin.setOnClickListener((View view) -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.no_animation);
        });
    }

    private void setSwipeListener(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

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
        mSwipeRefresh.setRefreshing(true);
    }

    public void hideProgress() {
        mProgress.setVisibility(View.GONE);
        mSwipeRefresh.setRefreshing(false);
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

        String url = baseUrl + "/point/" + toRemove.id;

        RequestQueue queue = Volley.newRequestQueue(this);

        RemovePointRequest request = new RemovePointRequest(
                url,
                username,
                password,
                (String response) -> {
                    Toast.makeText(MainActivity.this,
                            "Item Deleted",
                            Toast.LENGTH_SHORT)
                                .show();
                    showProgress();
                    mModel.forceRefresh(getApplicationContext());
                },
                (VolleyError error) ->
                    VolleyErrorUtils.logError(MainActivity.this, error)
                ,
                toRemove
        );
        queue.add(request);
    }
}
