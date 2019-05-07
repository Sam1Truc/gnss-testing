package com.wtbtest.locationlogger;

import android.content.Context;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RawGNSSActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "RawMeasurementsFileLogger";
    private static final String TAG_STATUS = "GNSSStatus";
    private static final String TAG_CONST = "GNSSConst";
    private static final String TAG_CARRIERFREQ = "GNSSCarrierFreq";
    private static final String TAG_ACCDELTARANGE = "GNSSAccDeltaRange";
    private static final String TAG_ACCDELTARANGEUNC = "GNSSAccDeltaRangeUnc";
    private static final String TAG_TIME = "GNSSTime";
    private static final String TAG_CONSTCOUNT = "GNSSSatCount";


    private LocationManager mLocationManager;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;
    private GnssMeasurementsEvent.Callback mRawMeasurementsListener;
    private GnssStatus.Callback mGnssStatusListener;

    private TextView textViewStatus;
    private TextView textViewConst;
    private TextView textViewCarrierFreq;
    private TextView textViewAccumDeltaRange;
    private TextView textViewAccumDeltaRangeUnc;
    private TextView textViewTime;
    private TextView textViewSatCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw_gnss);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        textViewStatus = findViewById(R.id.textViewGNSSStatus);
        textViewStatus.setText("UNKNOWN");
        textViewConst = findViewById(R.id.textViewConstellation);
        textViewCarrierFreq = findViewById(R.id.textViewCarrierFreq);
        textViewAccumDeltaRange = findViewById(R.id.textViewAccumDeltaRange);
        textViewAccumDeltaRangeUnc = findViewById(R.id.textViewAccumDeltaRangeUnc);
        textViewTime = findViewById(R.id.textViewTime);
        textViewSatCount = findViewById(R.id.textViewSatCount);

        if(savedInstanceState != null)
        {
            textViewStatus.setText(savedInstanceState.getString(TAG_STATUS));
            textViewConst.setText(savedInstanceState.getString(TAG_CONST));
            textViewCarrierFreq.setText(savedInstanceState.getString(TAG_CARRIERFREQ));
            textViewAccumDeltaRange.setText(savedInstanceState.getString(TAG_ACCDELTARANGE));
            textViewAccumDeltaRangeUnc.setText(savedInstanceState.getString(TAG_ACCDELTARANGEUNC));
            textViewTime.setText(savedInstanceState.getString(TAG_TIME));
            textViewSatCount.setText(savedInstanceState.getString(TAG_CONSTCOUNT));
        }

        registerRawMeasurements();
        registerStatusListener();

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void registerStatusListener() {
        mGnssStatusListener = new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                super.onSatelliteStatusChanged(status);
                textViewSatCount.setText(Integer.toString(status.getSatelliteCount()));
            }
        };
    }

    public void registerRawMeasurements() {
        MainActivity.checkLocationPermission(this);


        mRawMeasurementsListener = new GnssMeasurementsEvent.Callback() {
            @Override
            public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {
                for (GnssMeasurement m : eventArgs.getMeasurements()) {

                    setConstellationType(m.getConstellationType());
                    textViewCarrierFreq.setText(Float.toString(m.getCarrierFrequencyHz()));
                    textViewAccumDeltaRange.setText(Double.toString(m.getAccumulatedDeltaRangeMeters()));
                    textViewAccumDeltaRangeUnc.setText(Double.toString(m.getPseudorangeRateUncertaintyMetersPerSecond()));
                    textViewTime.setText(new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(Calendar.getInstance().getTime()));

                }
                super.onGnssMeasurementsReceived(eventArgs);

            }

            @Override
            public void onStatusChanged(int status) {
                int gnssMeasurementStatus = status;
                setStatusUI(gnssMeasurementStatus);
                super.onStatusChanged(status);
            }

        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        /*
        final LocationRequest locationRequest = new LocationRequest();

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setMaxWaitTime(500);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(100);*/


        MainActivity.checkLocationPermission(this);
        /*mFusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null);
        */
        mLocationManager.registerGnssStatusCallback(mGnssStatusListener);
        mLocationManager.registerGnssMeasurementsCallback(mRawMeasurementsListener);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationManager.removeUpdates(this);
        mLocationManager.unregisterGnssStatusCallback(mGnssStatusListener);
        mLocationManager.unregisterGnssMeasurementsCallback(mRawMeasurementsListener);
    }

    private void setStatusUI(int status) {
        switch (status) {
            case GnssMeasurementsEvent.Callback.STATUS_NOT_SUPPORTED:
                Log.v(TAG, "Status: NOT_SUPPORTED");
                textViewStatus.setText("NOT SUPPORTED");
                break;
            case GnssMeasurementsEvent.Callback.STATUS_READY:
                Log.v(TAG, "Status: READY");
                textViewStatus.setText("READY");
                break;
            case GnssMeasurementsEvent.Callback.STATUS_LOCATION_DISABLED:
                Log.v(TAG, "Status: LOCATION_DISABLED");
                textViewStatus.setText("LOCATION DISABLED");
                break;
            case GnssMeasurementsEvent.Callback.STATUS_NOT_ALLOWED:
                Log.v(TAG, "Status: NOT_ALLOWED");
                textViewStatus.setText("NOT ALLOWED");
                break;
            default:
                Log.v(TAG, "Status: UNKOWN");
                textViewStatus.setText("UKNOWN");
                break;
        }
        textViewTime.setText(new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(Calendar.getInstance().getTime()));
    }

    private void setConstellationType(int constellationType) {
        switch (constellationType){
            case GnssStatus.CONSTELLATION_GPS:
                textViewConst.setText("GPS");
                break;
            case GnssStatus.CONSTELLATION_SBAS:
                textViewConst.setText("SBAS");
                break;
            case GnssStatus.CONSTELLATION_GLONASS:
                textViewConst.setText("GLONASS");
                break;
            case GnssStatus.CONSTELLATION_QZSS:
                textViewConst.setText("QZSS");
                break;
            case GnssStatus.CONSTELLATION_BEIDOU:
                textViewConst.setText("BEIDOU");
                break;
            case GnssStatus.CONSTELLATION_GALILEO:
                textViewConst.setText("GALILEO");
                break;
            case GnssStatus.CONSTELLATION_UNKNOWN:
                textViewConst.setText("UNKNOWN");
                break;
            default:
                textViewConst.setText("UKNOWN");
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(TAG_STATUS,textViewStatus.getText().toString());
        outState.putString(TAG_CONST,textViewConst.getText().toString());
        outState.putString(TAG_CARRIERFREQ,textViewCarrierFreq.getText().toString());
        outState.putString(TAG_ACCDELTARANGE,textViewAccumDeltaRange.getText().toString());
        outState.putString(TAG_ACCDELTARANGEUNC, textViewAccumDeltaRangeUnc.getText().toString());
        outState.putString(TAG_TIME, textViewTime.getText().toString());
        outState.putString(TAG_CONSTCOUNT,textViewSatCount.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}