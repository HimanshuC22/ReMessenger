package com.example.chatapp.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.example.chatapp.R;
import com.example.chatapp.models.Group;
import com.example.chatapp.models.User;
import com.example.chatapp.utils.ConfirmAddress;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HolderActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Button btn;
    private String chatChild, userOrGroupId;
    protected User userMe, user;
    protected Group group;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationClient;

    ProgressBar progressBar;
    private final static int PLACE_PICKER_REQUEST = 999;
    private final static int LOCATION_REQUEST_CODE = 23;
    private final static int LOCATION_SETTINGS = 100;
    private static final String TAG = "HolderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holder);
        progressBar = findViewById(R.id.progressBar);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            chatChild = (String) bundle.get("attachment_chat_child");
            userOrGroupId = (String) bundle.get("attachment_recipient_id");
            user = (User) bundle.get("attachment_recipient_user");
            group = (Group) bundle.get("attachment_recipient_group");
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST_CODE);

        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


//        else {
//            statusCheck();
//            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//
//            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                    .findFragmentById(R.id.map);
//            mapFragment.getMapAsync(this);
//        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        statusCheck();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initMap() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Toast.makeText(HolderActivity.this, "Please Wait", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setEnabled(true);
                if (ActivityCompat.checkSelfPermission(HolderActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(HolderActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return false;
                }
//                Location location = mMap.getMyLocation();
//                if (location != null) {
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                            new LatLng(location.getLatitude(), location.getLongitude()), 19));
//                    progressBar.setVisibility(View.GONE);
//                    progressBar.setEnabled(true);
//                }
                fusedLocationClient.getLastLocation()
                        .addOnCompleteListener(HolderActivity.this, new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Log.d(TAG, "onComplete: completed");
                                Location location = task.getResult();
                                if (location != null) {
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(location.getLatitude(), location.getLongitude()), 19));

                                    CameraPosition cameraPosition = new CameraPosition.Builder()
                                            .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                                            .zoom(19)                   // Sets the zoom
                                            // Sets the orientation of the camera to east
                                            // Sets the tilt of the camera to 30 degrees
                                            .build();                   // Creates a CameraPosition from the builder
                                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                    progressBar.setVisibility(View.GONE);
                                    progressBar.setEnabled(true);
                                }
                            }
                        })
                        .addOnSuccessListener(HolderActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                Log.d(TAG, "onSuccess: success");
                                if (location != null) {
                                    Log.d(TAG, "onSuccess: location found");
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(location.getLatitude(), location.getLongitude()), 19));

                                    CameraPosition cameraPosition = new CameraPosition.Builder()
                                            .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                                            .zoom(19)                   // Sets the zoom
                                            // Sets the orientation of the camera to east
                                            // Sets the tilt of the camera to 30 degrees
                                            .build();                   // Creates a CameraPosition from the builder
                                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                    progressBar.setVisibility(View.GONE);
                                    progressBar.setEnabled(true);
                                }
                            }
                        });

                return true;
            }
        });
//        progressBar.setVisibility(View.VISIBLE);
//        progressBar.setEnabled(true);
//        progressBar.animate();
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

            }
        });

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        ltlng, 16f);
                mMap.animateCamera(cameraUpdate);
                progressBar.setVisibility(View.GONE);
            }
        });

//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                markerOptions.title(getAddress(latLng));
                mMap.clear();
                CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                        latLng, 15);
                mMap.animateCamera(location);
                mMap.addMarker(markerOptions);

            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    statusCheck();

                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
//                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                        // TODO: Consider calling
//                        //    ActivityCompat#requestPermissions
//                        // here to request the missing permissions, and then overriding
//                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                        //                                          int[] grantResults)
//                        // to handle the case where the user grants the permission. See the documentation
//                        // for ActivityCompat#requestPermissions for more details.
//                        return;
//                    }
//                    mMap.setMyLocationEnabled(true);
//                    mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
//                        @Override
//                        public void onMyLocationChange(Location location) {
//                            LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());
//                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
//                                    ltlng, 16f);
//                            mMap.animateCamera(cameraUpdate);
//                        }
//                    });
//                    Location location = mMap.getMyLocation();
//
//                    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//                        @Override
//                        public void onMapClick(LatLng latLng) {
//                            MarkerOptions markerOptions = new MarkerOptions();
//                            markerOptions.position(latLng);
//
//                            markerOptions.title(getAddress(latLng));
//                            mMap.clear();
//                            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
//                                    latLng, 15);
//                            mMap.animateCamera(location);
//                            mMap.addMarker(markerOptions);
//                        }
//                    });


                } else {
                    Toast.makeText(this, "No permission found!", Toast.LENGTH_SHORT).show();
                    this.finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    private String getAddress(LatLng latLng) {

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//            String city = addresses.get(0).getLocality();
//            String state = addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
//            String postalCode = addresses.get(0).getPostalCode();
//            String knownName = addresses.get(0).getFeatureName();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            DialogFragment dialogFragment = new ConfirmAddress();
            Bundle args = new Bundle();
            args.putDouble("lat", latLng.latitude);
            args.putDouble("long", latLng.longitude);
            args.putString("address", address);
            args.putString("attachment_chat_child", chatChild);
            args.putString("attachment_recipient_id", userOrGroupId);
            args.putParcelable("attachment_recipient_user", user);
            args.putParcelable("attachment_recipient_group", group);
            dialogFragment.setArguments(args);
            dialogFragment.show(ft, "dialog");
            return address;
        } catch (IOException e) {
            e.printStackTrace();
            return "No Address Found";

        }
    }

    public void statusCheck() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    initMap();
                } catch (ApiException e) {
                    e.printStackTrace();
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes
                                .RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(HolderActivity.this, LOCATION_SETTINGS);
                            } catch (IntentSender.SendIntentException sendIntentException) {
                                sendIntentException.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes
                                .SETTINGS_CHANGE_UNAVAILABLE:
                            break;

                    }
                }
            }
        });
//        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            buildAlertMessageNoGps();
//        }
    }

    private void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your location seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.d(TAG, "onActivityResult: map started");
                    initMap();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(this, "Location Is required!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}