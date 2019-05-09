package com.wtbtest.locationlogger;

import android.content.Context;
import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
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

    private static final int RANGE_ELEVATION = 35;
    private static final int ZENITH_ELEVATION = 90;


    private LocationManager mLocationManager;
    private GnssMeasurementsEvent.Callback mRawMeasurementsListener;
    private GnssStatus.Callback mGnssStatusListener;

    private TextView textViewStatus;
    private TextView textViewConst;
    private TextView textViewCarrierFreq;
    private TextView textViewAccumDeltaRange;
    private TextView textViewAccumDeltaRangeUnc;
    private TextView textViewTime;
    private TextView textViewSatCount;

    private double GNSSTime;


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
    }

    private void registerStatusListener() {
        mGnssStatusListener = new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                super.onSatelliteStatusChanged(status);
                textViewSatCount.setText(Integer.toString(status.getSatelliteCount()));

                int nbSumSat = status.getSatelliteCount();
                int nbAvailableSat = 0;
                for (int i =0; i < nbSumSat; ++i) {

                    if((status.getElevationDegrees(i) >= (ZENITH_ELEVATION - RANGE_ELEVATION)) &&
                            (status.getElevationDegrees(i) <= (ZENITH_ELEVATION + RANGE_ELEVATION))) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("\n" + "Satellite ID: " + status.getSvid(i) + "\n");
                        builder.append("Type: " + getSatType(status.getConstellationType(i)) + "\n");
                        builder.append("Azimuth: " + status.getAzimuthDegrees(i) + "°\n");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            //builder.append("Carrier frequency: " + status.getCarrierFrequencyHz(i) + "Hz\n");
                        }
                        //builder.append("Cn0Db: " + status.getCn0DbHz(i) + "Hz\n");
                        builder.append("Elevation: " + status.getElevationDegrees(i) + "°\n");
                        Log.v(TAG, builder.toString());
                        nbAvailableSat++;
                    }
                }
                Log.v(TAG, "Number of satellites (in best range): " + nbAvailableSat + "\n");
            }
        };
    }

    private String getSatType(int constellationType) {
        String typeSat;
        switch (constellationType){
            case GnssStatus.CONSTELLATION_GPS:
                typeSat = "GPS";
                break;
            case GnssStatus.CONSTELLATION_SBAS:
                typeSat = "SBAS";
                break;
            case GnssStatus.CONSTELLATION_GLONASS:
                typeSat = "GLONASS";
                break;
            case GnssStatus.CONSTELLATION_QZSS:
                typeSat = "QZSS";
                break;
            case GnssStatus.CONSTELLATION_BEIDOU:
                typeSat = "BEIDOU";
                break;
            case GnssStatus.CONSTELLATION_GALILEO:
                typeSat = "GALILEO";
                break;
            case GnssStatus.CONSTELLATION_UNKNOWN:
                typeSat = "UKNOWN";
                break;
            default:
                typeSat = "UKNOWN";
                break;
        }
        return typeSat;
    }

    public void registerRawMeasurements() {
        MainActivity.checkLocationPermission(this);


        mRawMeasurementsListener = new GnssMeasurementsEvent.Callback() {
            @Override
            public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {
                GnssClock clock = eventArgs.getClock();
                GNSSTime = clock.getTimeNanos() - (clock.getFullBiasNanos() + clock.getBiasNanos());
                for (GnssMeasurement m : eventArgs.getMeasurements()) {
                    textViewConst.setText(getSatType(m.getConstellationType()));
                    textViewCarrierFreq.setText(Float.toString(m.getCarrierFrequencyHz()));
                    textViewAccumDeltaRange.setText(Double.toString(m.getAccumulatedDeltaRangeMeters()));
                    textViewAccumDeltaRangeUnc.setText(Double.toString(m.getPseudorangeRateUncertaintyMetersPerSecond()));
                    textViewTime.setText(new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(Calendar.getInstance().getTime()));
                    createPseudoRange(m.getConstellationType(), clock, m.getTimeOffsetNanos(), m.getReceivedSvTimeNanos());
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

    private void createPseudoRange(int constellationType, GnssClock clock, double timeOffsetNanos, long receivedSvTimeNanos) {
        long numberNanoSecondsWeek = 604800*(10^9); //within a week
        long numberNanoSecondsDay = numberNanoSecondsWeek / 7;
        long numberNanoSeconds100Milli = 10^8;
        long weekNumberNanos = Math.floorDiv(-clock.getFullBiasNanos(), Double.doubleToLongBits(numberNanoSecondsWeek)) * numberNanoSecondsWeek;
        long dayNumberNanos = Math.floorDiv(-clock.getFullBiasNanos(), Double.doubleToLongBits(numberNanoSecondsDay)) * numberNanoSecondsDay;
        long milliSecondsNumberNanos = Math.floorDiv(-clock.getFullBiasNanos(), Double.doubleToLongBits(numberNanoSeconds100Milli)) * numberNanoSeconds100Milli;

        double tRx_Gnss = clock.getTimeNanos() - (clock.getFullBiasNanos() + clock.getBiasNanos());
        double tRx = -1;
        double tTx;
        double prMilliSeconds;
        double pr;



        switch (constellationType) {
            case GnssStatus.CONSTELLATION_GPS:
                tRx = tRx_Gnss % numberNanoSecondsWeek;
                break;
            case GnssStatus.CONSTELLATION_GLONASS:
                tRx = (tRx_Gnss % numberNanoSecondsDay) + 1.08*(10^13) - clock.getLeapSecond() * (10^9);
                break;
        }
        tTx = receivedSvTimeNanos + timeOffsetNanos;
        if(tRx != -1){
            prMilliSeconds = (tRx - tTx);
            pr = prMilliSeconds * 299792458 * (10^-9);
            //Log.v(TAG,"pseudorange : "+pr+" seconds");
         }
    }

    @Override
    protected void onStart() {
        super.onStart();

        MainActivity.checkLocationPermission(this);

        mLocationManager.registerGnssStatusCallback(mGnssStatusListener);
        mLocationManager.registerGnssMeasurementsCallback(mRawMeasurementsListener);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);
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