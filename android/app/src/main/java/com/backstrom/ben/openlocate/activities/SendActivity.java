package com.backstrom.ben.openlocate.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.backstrom.ben.openlocate.R;
import com.backstrom.ben.openlocate.model.Point;
import com.backstrom.ben.openlocate.model.SendViewModel;
import com.backstrom.ben.openlocate.requests.AddPointRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;

import static com.backstrom.ben.openlocate.requests.AuthRequest.PASSWORD_KEY;
import static com.backstrom.ben.openlocate.requests.AuthRequest.URL_KEY;
import static com.backstrom.ben.openlocate.requests.AuthRequest.USERNAME_KEY;

/**
 * Created by benba on 3/13/2017.
 */

public class SendActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = SendActivity.class.getSimpleName();

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 127;
    private static final int REQUEST_CHECK_LOCATION_SETTINGS = 128;
    private static final int REQUEST_CAMERA_IMAGE = 129;

    private boolean mConnected = false;
    private boolean mWasPaused = false;

    private SendViewModel mModel;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String mUrl;
    private LocationRequest mLocationRequest;
    private LocationListener mLocationListener;
    private File mPhotoFile;

    private ImageView mPictureView;
    private ImageView mCloseView;
    private EditText mNameEdit;
    private EditText mNotesEdit;
    private FloatingActionButton mSendButton;
    private TextView mLatLngView;
    private TextView mErrorView;

    private Response.Listener<String> mAdditionListener;
    private Response.ErrorListener mErrorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        mModel = ViewModelProviders.of(this).get(SendViewModel.class);

        mPictureView = findViewById(R.id.picture_view);
        mCloseView = findViewById(R.id.close);
        mNameEdit = findViewById(R.id.place_name);
        mNotesEdit = findViewById(R.id.notes);
        mSendButton = findViewById(R.id.create_point);
        mLatLngView = findViewById(R.id.lat_lng);
        mErrorView = findViewById(R.id.error);

        mUrl = getIntent().getExtras().getString(URL_KEY, null);
        if (mUrl == null)
            mUrl = mModel.getUrl();
        else
            mModel.setUrl(mUrl);

        if (mUrl == null)
            mSendButton.setVisibility(View.GONE);

        if (isExternalStorageReadable()) {
            File photosDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            mPhotoFile = new File(photosDir, "temp.jpg");
        }

        if (mModel.getPlaceName() != null && mModel.getPlaceName().length() > 0)
            mNameEdit.setText(mModel.getPlaceName());

        if (mModel.getNotes() != null && mModel.getNotes().length() > 0)
            mNotesEdit.setText(mModel.getNotes());

        if (mModel.getBitmap() != null)
            mPictureView.setImageBitmap(mModel.getBitmap());

        mPictureView.setOnClickListener((View view) -> {
            if (mPhotoFile != null) {
                Uri contentUri = FileProvider.getUriForFile(SendActivity.this,
                        "com.backstrom.ben.openlocate.fileprovider",
                        mPhotoFile);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_CAMERA_IMAGE);
                }
            }
        });

        mCloseView.setOnClickListener((View view) -> {
            finish();
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_down);
        });

        mSendButton.setOnClickListener((View view) ->
            sendRequest());
        
        mErrorView.setVisibility(View.GONE);

        mLocationListener = (Location location) -> {
            mLastLocation = location;
            updateLocation();
        };

        mAdditionListener = (String response) -> {
            Toast.makeText(SendActivity.this, response, Toast.LENGTH_LONG).show();
            mErrorView.setVisibility(View.GONE);
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_down);
        };

        mErrorListener = (VolleyError error) -> {
            Log.i(TAG, "-----------error: "+error.toString());
            error.printStackTrace();
            mErrorView.setVisibility(View.VISIBLE);
            mErrorView.setText(error.getMessage());
        };

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mModel.setPlaceName(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mNotesEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mModel.setNotes(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // We are already connecting on the first run of the activity. We only want to restart
        // location updates in onResume if the activity was paused before.
        if (mConnected && mWasPaused)
            checkPermissionsAndTryStartLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWasPaused = true;
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CHECK_LOCATION_SETTINGS:
                    startLocationUpdates();
                    break;
                case REQUEST_CAMERA_IMAGE:
                    Bitmap thumbnail = data.getParcelableExtra("data");
                    if (thumbnail == null) {
                        if (isExternalStorageReadable()) {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            thumbnail = BitmapFactory.decodeFile(mPhotoFile.getPath(), options);

                            if (isExternalStorageWritable()) {
                                if (mPhotoFile != null && mPhotoFile.exists())
                                    mPhotoFile.delete();
                            }
                        }
                    }

                    mModel.setBitmap(thumbnail);
                    if (thumbnail != null)
                        mPictureView.setImageBitmap(thumbnail);
                    break;
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        mErrorView.setText(R.string.location_connection_error);
        mErrorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mConnected = true;
        checkPermissionsAndTryStartLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int var1) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    retrieveLocation();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.permissions_error);
                    builder.setMessage(R.string.permissions_error_message);
                    builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
                    builder.show();
                    mSendButton.setVisibility(View.GONE);
                }
            }
        }
    }

    private void sendRequest() {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String addPointUrl = mUrl + "/add";
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String username = prefs.getString(USERNAME_KEY, null);
        final String password = prefs.getString(PASSWORD_KEY, null);

        Point point = new Point(-1,
                null,
                mNameEdit.getText().toString(),
                System.currentTimeMillis(),
                mModel.getLatLng(),
                mNotesEdit.getText().toString(),
                mModel.getBitmap());

        AddPointRequest additionRequest = new AddPointRequest(addPointUrl,
                username,
                password,
                mAdditionListener,
                mErrorListener,
                point);
        additionRequest.setRetryPolicy(new DefaultRetryPolicy(
                3*60*1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        mModel.clear();
        queue.add(additionRequest);
    }

    private void checkPermissionsAndTryStartLocationUpdates() {
        boolean hasCoarseLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean hasFineLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (hasCoarseLocation && hasFineLocation) {
            retrieveLocation();
            tryStartLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }
    }

    public void retrieveLocation() {
        // only do this if we have ACCESS_COARSE_LOCATION permission
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                updateLocation();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void tryStartLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
        result.setResultCallback(settingsResult -> {
            final Status status = settingsResult.getStatus();
            final LocationSettingsStates states = settingsResult.getLocationSettingsStates();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    startLocationUpdates();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        status.startResolutionForResult(SendActivity.this, REQUEST_CHECK_LOCATION_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        });
    }

    public void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void updateLocation() {
        LatLng latLng = new LatLng(
                mLastLocation.getLatitude(),
                mLastLocation.getLongitude()
        );
        String latLngText = latLng.latitude+", "+latLng.longitude;
        if (mLatLngView != null)
            mLatLngView.setText(latLngText);

        mModel.setLatLng(latLng);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_down);
    }
}
