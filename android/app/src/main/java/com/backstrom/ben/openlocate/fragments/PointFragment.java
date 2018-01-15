package com.backstrom.ben.openlocate.fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Configuration;
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
import com.backstrom.ben.openlocate.activities.ImageZoomActivity;
import com.backstrom.ben.openlocate.model.Point;
import com.backstrom.ben.openlocate.model.PointViewModel;
import com.backstrom.ben.openlocate.util.DateFormatUtil;
import com.backstrom.ben.openlocate.util.DpUtils;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by benba on 1/12/2018.
 */

public class PointFragment extends Fragment {

    private ScrollView mScrollView;
    private ProgressBar mProgressBar;
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

        PointViewModel model = ViewModelProviders.of(this).get(PointViewModel.class);
        mPoint = model.getPoint(getArguments());

        mScrollView = root.findViewById(R.id.scroll_view);
        mProgressBar = root.findViewById(R.id.progress_bar);
        mMapView = root.findViewById(R.id.map_view);
        mImageView = root.findViewById(R.id.image_view);
        mDateView = root.findViewById(R.id.date_time_text);
        mLatLngView = root.findViewById(R.id.lat_lng_text);
        mNotesView = root.findViewById(R.id.notes);

        mScrollView.setVisibility(View.INVISIBLE);

        mMapView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
             @Override
             public void onGlobalLayout() {
                 mMapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                 loadImages();
             }
        });

        String date = DateFormatUtil.getFormattedDate(mPoint.timestamp);
        mDateView.setText(date);
        mLatLngView.setText(mPoint.latLng.toString());
        mNotesView.setText(mPoint.notes);

        mImageView.setOnClickListener((View view) -> {
            Intent intent = new Intent(getContext(), ImageZoomActivity.class);
            intent.putExtra(Point.NAME_KEY, mPoint.name);
            intent.putExtra(Point.ATTACHMENT_KEY, mPoint.attachmentUri);
            getContext().startActivity(intent);
        });

        return root;
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

    private void loadImages() {
        int width = 0;
        int orientation = getContext().getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            width = mMapView.getMeasuredWidth();
        } else {
            int maxWidth = (int) getContext().getResources().getDimension(R.dimen.image_width);
            width = mMapView.getMeasuredHeight();
            if (width > maxWidth) {
                width = maxWidth;
            }
        }

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
}
