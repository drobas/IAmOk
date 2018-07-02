package com.michaldrobny.iamok.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.michaldrobny.iamok.PermissionManager;
import com.michaldrobny.iamok.R;
import com.michaldrobny.iamok.model.Constants;
import com.michaldrobny.iamok.model.ServiceType;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.michaldrobny.iamok.PermissionManager.READ_CONTACTS_PERMISSION_REQUEST;

public class PlaceInitiatorActivity extends PickContactActivity implements OnMapReadyCallback {

    @BindView(R.id.activity_place_initiator_continue_b) Button continueButton;

    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_initiator);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if(googleMap != null) {
            googleMap.getUiSettings().setRotateGesturesEnabled(false);
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (googleMap == null) {
            return;
        } try {
            if (PermissionManager.isLocationPermissionGranted(this)) {
                continueButton.setEnabled(true);
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.setMyLocationEnabled(false);
                PermissionManager.requestLocationPermission(this);
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionManager.LOCATION_TRACKING_PERMISSION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateLocationUI();
        }
    }

    @Override
    protected void passArguments(Intent intent) {
        CameraPosition cameraPosition = googleMap.getCameraPosition();
        intent.putExtra(Constants.ARG_TYPE, ServiceType.Place.ordinal());
        intent.putExtra(Constants.ARG_LOCATION_LAT, cameraPosition.target.latitude);
        intent.putExtra(Constants.ARG_LOCATION_LNG, cameraPosition.target.longitude);
        intent.putExtra(Constants.ARG_ZOOM, cameraPosition.zoom);
    }

    @OnClick(R.id.activity_place_initiator_continue_b) void continueClick() {
        showPickContactActivity();
    }
}
