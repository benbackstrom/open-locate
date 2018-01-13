package com.backstrom.ben.openlocate.fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.backstrom.ben.openlocate.R;
import com.backstrom.ben.openlocate.model.Point;
import com.backstrom.ben.openlocate.util.DateFormatUtil;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by benba on 1/12/2018.
 */

public class PointFragment extends Fragment {

    private ScrollView mScrollView;
    private ProgressBar mProgressBar;
    private View mBack;
    private TextView mTitleView;
    private ImageView mMapView;
    private ImageView mImageView;
    private TextView mDateView;
    private TextView mLatLngView;
    private TextView mNotesView;

    private Point mPoint;

    public static PointFragment newInstance(Bundle args) {
        PointFragment fragment = new PointFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_point, null);

        createPoint();

        mScrollView = (ScrollView) root.findViewById(R.id.scroll_view);
        mProgressBar = (ProgressBar) root.findViewById(R.id.progress_bar);
        mBack = root.findViewById(R.id.back);
        mTitleView = (TextView) root.findViewById(R.id.title_view);
        mMapView = (ImageView) root.findViewById(R.id.map_view);
        mImageView = (ImageView) root.findViewById(R.id.image_view);
        mDateView = (TextView) root.findViewById(R.id.date_time_text);
        mLatLngView = (TextView) root.findViewById(R.id.lat_lng_text);
        mNotesView = (TextView) root.findViewById(R.id.notes);

        mScrollView.setVisibility(View.INVISIBLE);

        mMapView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
             @Override
             public void onGlobalLayout() {
                 mMapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                 int width = mMapView.getMeasuredWidth();

                 Picasso.with(getContext())
                         .load(mPoint.mapUri)
                         .resize(width, width)
                         .centerCrop()
                         .into(mMapView);
                 Picasso.with(getContext())
                         .load(mPoint.attachmentUri)
                         .resize(width, width)
                         .centerCrop()
                         .into(mImageView, new Callback() {
                             @Override
                             public void onSuccess() {
                                 animateToVisible();
                             }

                             @Override
                             public void onError() {
                                 Toast.makeText(getContext(),
                                         "There was an error loading the image for this point.",
                                         Toast.LENGTH_SHORT)
                                         .show();
                                 animateToVisible();
                             }
                         });
             }
        });

        mBack.setOnClickListener((View v) -> getActivity().finish());
        mTitleView.setText(mPoint.name);
        String date = DateFormatUtil.getFormattedDate(mPoint.timestamp);
        mDateView.setText(date);
        mLatLngView.setText(mPoint.latLng.toString());
        mNotesView.setText(mPoint.notes);

        return root;
    }

    private void createPoint() {
        Bundle args = getArguments();
        long id = args.getLong(Point.ID, -1);
        String name = args.getString(Point.NAME_KEY);
        String mapUri = args.getString(Point.MAP_KEY);
        String attachmentUri = args.getString(Point.ATTACHMENT_KEY);
        long timestamp = args.getLong(Point.TIMESTAMP_KEY, -1);
        double lat = args.getDouble(Point.LAT_KEY);
        double lng = args.getDouble(Point.LNG_KEY);
        String notes = args.getString(Point.NOTES_KEY, null);
        LatLng latLng = new LatLng(lat, lng);

        mPoint = new Point(id, mapUri, name, timestamp, latLng, notes, attachmentUri);
    }

    private void animateToVisible() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(400);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener((ValueAnimator valueAnimator) ->
            mScrollView.setAlpha(valueAnimator.getAnimatedFraction()));

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mProgressBar.setVisibility(View.GONE);
                mScrollView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {}

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });

        AnimatorSet set = new AnimatorSet();
        set.play(animator);
        set.start();
    }
}
