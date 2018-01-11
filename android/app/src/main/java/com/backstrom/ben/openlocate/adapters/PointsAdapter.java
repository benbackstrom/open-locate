package com.backstrom.ben.openlocate.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.backstrom.ben.openlocate.R;
import com.backstrom.ben.openlocate.activities.ImageZoomActivity;
import com.backstrom.ben.openlocate.model.Point;
import com.backstrom.ben.openlocate.util.DateFormatUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.backstrom.ben.openlocate.requests.AuthRequest.URL_KEY;

/**
 * Created by benba on 3/13/2017.
 */

public class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.ViewHolder> {

    private static final String TAG = PointsAdapter.class.getSimpleName();
    public static final String NAME_KEY = "ID_KEY";
    public static final String IMAGE_URL_KEY = "IMAGE_URL_KEY";
    private List<Point> mDataset;
    private Context mContext;

    public PointsAdapter(Context context, List<Point> dataset) {
        mContext = context;
        mDataset = dataset;
    }

    @Override
    public PointsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.point_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Point point = mDataset.get(position);

        if (point.map != null) {
            holder.mapView.setImageBitmap(point.map);
        } else if (point.mapUri != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            String baseUrl = prefs.getString(URL_KEY, null);
            if (baseUrl != null) {
                String temp = point.mapUri.substring(6, point.mapUri.length());
                String url = baseUrl + temp;

                Picasso.with(mContext)
                        .load(url)
                        .into(holder.mapView);
            }
        }
        holder.pointName.setText(point.name);
        holder.timeDate.setText(DateFormatUtil.getFormattedDate(point.timestamp));

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public List<Point> getDataSet() {
        return mDataset;
    }

    public void swapList(List<Point> input) {
        mDataset = input;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View root;
        public ImageView mapView;
        public TextView pointName;
        public TextView timeDate;

        public ViewHolder(View v) {
            super(v);
            root = v;
            mapView = (ImageView) v.findViewById(R.id.map_view);
            pointName = (TextView) v.findViewById(R.id.point_name);
            timeDate = (TextView) v.findViewById(R.id.date_time);
        }
    }
}
