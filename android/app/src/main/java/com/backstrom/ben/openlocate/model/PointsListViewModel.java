package com.backstrom.ben.openlocate.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.backstrom.ben.openlocate.requests.AuthRequest;
import com.backstrom.ben.openlocate.tasks.ConvertPointsTask;
import com.backstrom.ben.openlocate.util.VolleyErrorUtils;

import java.util.ArrayList;
import java.util.List;

import static com.backstrom.ben.openlocate.requests.AuthRequest.PASSWORD_KEY;
import static com.backstrom.ben.openlocate.requests.AuthRequest.URL_KEY;
import static com.backstrom.ben.openlocate.requests.AuthRequest.USERNAME_KEY;

/**
 * Created by benba on 1/15/2018.
 */

public class PointsListViewModel extends ViewModel implements ConvertPointsTask.Listener {

    private static final String TAG = PointsListViewModel.class.getSimpleName();

    private MutableLiveData<List<Point>> mLiveData;
    private ConvertPointsTask mTask;

    public LiveData<List<Point>> getLiveData(Context context) {
        if (mLiveData == null) {
            mLiveData = new MutableLiveData<>();
            refreshList(context);
        }

        return mLiveData;
    }

    public void forceRefresh(Context context) {
        refreshList(context);
    }

    public void refreshList(Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String baseUrl = prefs.getString(URL_KEY, null);
        String username = prefs.getString(USERNAME_KEY, null);
        String password = prefs.getString(PASSWORD_KEY, null);

        String url = baseUrl + "/get-points";

        AuthRequest request = new AuthRequest(Request.Method.POST,
                url,
                username,
                password,

                (String response) -> {
                    if (response != null) {
                        if (mTask != null) {
                            mTask.cancel(true);
                            mTask = null;
                        }

                        String workingUrl = prefs.getString(URL_KEY, null);

                        mTask = new ConvertPointsTask(workingUrl, PointsListViewModel.this);
                        mTask.execute(response);
                    }
                },
                (VolleyError error) -> {
                    VolleyErrorUtils.logError(context, error);
                    mLiveData.setValue(new ArrayList<>());
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(
                3*60*1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);
    }

    @Override
    public void onPointsReceived(List<Point> results) {
        mLiveData.setValue(results);
    }
}
