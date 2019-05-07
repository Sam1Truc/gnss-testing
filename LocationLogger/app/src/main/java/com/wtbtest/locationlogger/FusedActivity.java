package com.wtbtest.locationlogger;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class FusedActivity extends AppCompatActivity {
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final int ONE_MINUTE = 1000 * 60;
    private static final int TEN_SECONDS = 1000 * 10;
    private static final int FIVE_SECONDS = 1000 * 5;
    private static final int ONE_SECOND = 1000;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location mCurrentLocation;
    private Boolean requestingLocationUpdates;
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY";

    private LogWriter logWriterPosition;
    private LogWriter logWriterAccuracy;

    private TextView textViewTime;
    private TextView textViewLat;
    private TextView textViewLong;
    private TextView textViewAlt;
    private TextView textViewProvider;
    private TextView textViewAccuracy;


    private void findTextViews() {
        textViewTime = findViewById(R.id.textViewTime);
        textViewLat = findViewById(R.id.textViewLat);
        textViewLong = findViewById(R.id.textViewLong);
        textViewAlt = findViewById(R.id.textViewAltitude);
        textViewProvider = findViewById(R.id.textViewProvider);
        textViewAccuracy = findViewById(R.id.textViewAccuracy);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fused);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        findTextViews();

        logWriterPosition = new LogWriter(getApplicationContext(), LogType.FUSEDPOSITION);
        logWriterAccuracy = new LogWriter(getApplicationContext(), LogType.FUSEDACCURACY);

        MainActivity.checkLocationPermission(this);
        createLocationRequest();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    mCurrentLocation = location;
                    updateUI();
                }
            }
        });

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult == null){
                    return;
                }
                for(Location location : locationResult.getLocations())
                {
                    if(mCurrentLocation.getTime() - location.getTime() < 0){
                        mCurrentLocation = location;

                        String lat = String.format(Locale.ENGLISH,"%.6f",location.getLatitude());
                        String lon = String.format(Locale.ENGLISH,"%.6f", location.getLongitude());
                        String acc = String.format(Locale.ENGLISH,"%.1f", location.getAccuracy());

                        logWriterPosition.writeLog(new String[] {lat, lon});
                        logWriterAccuracy.writeLog(new String[] {acc});

                        updateUI();
                    }
                }
            }
        };
        requestingLocationUpdates = true;
        updateValuesFromBundle(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if(savedInstanceState == null){
            return;
        }

        if(savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            requestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
            //updateUI();
        }
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(FIVE_SECONDS);
        locationRequest.setFastestInterval(ONE_SECOND);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void updateUI() {
        textViewTime.setText(new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(mCurrentLocation.getTime()));
        textViewLat.setText(Double.toString(mCurrentLocation.getLatitude()));
        textViewLong.setText(Double.toString(mCurrentLocation.getLongitude()));
        textViewAlt.setText(Double.toString(mCurrentLocation.getAltitude()));
        textViewProvider.setText(mCurrentLocation.getProvider());
        textViewAccuracy.setText(Float.toString(mCurrentLocation.getAccuracy()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        MainActivity.checkLocationPermission(this);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates);
        super.onSaveInstanceState(outState);
    }
}
