package com.backstrom.ben.openlocate.tasks;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.BaseBundle;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.backstrom.ben.openlocate.model.Point;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.backstrom.ben.openlocate.requests.AuthRequest.URL_KEY;

/**
 * Created by benba on 5/20/2017.
 */

public class ConvertPointsTask extends AsyncTask<String, Void, List<Point>> {

    private String mBaseUrl;
    private Listener mListener;

    public ConvertPointsTask(String baseUrl, Listener listener) {
        mBaseUrl = baseUrl;
        mListener = listener;
    }

    @Override
    protected List<Point> doInBackground(String... raw) {
        List<Point> results = null;
        String input = raw[0];

        try {
            JSONArray array = new JSONArray(input);
            results = new ArrayList<>(array.length());
            for (int i=0; i<array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                long id = object.optLong("id");
                String name = object.optString("name");
                long timestamp = object.optLong("timestamp");
                double latitude = object.optDouble("latitude");
                double longitude = object.optDouble("longitude");
                String notes = object.optString("notes");
                String mapUri = object.getString("mapUri");
                String imageUri = object.getString("imageUri");

                if (mBaseUrl != null) {
                    String tempMap = mapUri.substring(6, mapUri.length());
                    mapUri = mBaseUrl + tempMap;

                    String tempImage = imageUri.substring(6, imageUri.length());
                    imageUri = mBaseUrl + tempImage;
                }

                Point point = new Point(
                        id, mapUri, name, timestamp, new LatLng(latitude, longitude), notes, imageUri
                );
                results.add(point);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return results;
    }

    @Override
    protected void onPostExecute(List<Point> list) {
        mListener.onPointsReceived(list);
    }

    public interface Listener {
        void onPointsReceived(List<Point> results);
    }

    public Bitmap decodeBitmap(JSONArray data) {
        try {
            byte[] bytes = new byte[data.length()];
            for (int j = 0; j < data.length(); j++) {
                bytes[j] = (byte) data.getInt(j);
            }

            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
