package com.backstrom.ben.openlocate.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.backstrom.ben.openlocate.R;
import com.backstrom.ben.openlocate.activities.PointActivity;
import com.backstrom.ben.openlocate.model.Point;
import com.backstrom.ben.openlocate.util.DateFormatUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by benba on 3/13/2017.
 */

public class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.ViewHolder> {

    private static final String TAG = PointsAdapter.class.getSimpleName();
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

        Picasso.with(mContext)
                .load(point.mapUri)
                .into(holder.mapView);

        holder.pointName.setText(point.name);
        holder.timeDate.setText(DateFormatUtil.getFormattedDate(point.timestamp));

        holder.root.setOnClickListener((View view) -> {
            Bundle args = new Bundle();
            args.putLong(Point.ID, point.id);
            args.putString(Point.NAME_KEY, point.name);
            args.putString(Point.MAP_KEY, point.mapUri);
            args.putString(Point.ATTACHMENT_KEY, point.attachmentUri);
            args.putLong(Point.TIMESTAMP_KEY, point.timestamp);
            args.putDouble(Point.LAT_KEY, point.latLng.latitude);
            args.putDouble(Point.LNG_KEY, point.latLng.longitude);
            args.putString(Point.NOTES_KEY, point.notes);

            Intent intent = new Intent(mContext, PointActivity.class);
            intent.putExtras(args);
            mContext.startActivity(intent);
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
