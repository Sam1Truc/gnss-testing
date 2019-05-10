package com.wtbtest.locationlogger;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.util.Log;

public class NtripEurefTest extends Ntrip {

    private static final String TAG = "NtripEurefTest";

    public NtripEurefTest(Activity activity) {
        super(activity);
        SERVERIP = "www.euref-ip.net";
        SERVERPORT = "80";
        USERNAME = "Samtruc";
        PASSWORD = "Info+4953";
        MOUNTPOINT = "ENIS00GBR0";
        MACAddress = "B0:89:00:1C:75:54";
    }

    @Override
    public void UpdateStatus(String fixtype, String info1, String info2) {
        Log.v(TAG, "Fixtype: " + fixtype + "\ninfo1: " + info1 + "\ninfo2: " +  info2);
    }

    @Override
    public void UpdateLogAppend(String msg) {
        Log.v(TAG,msg);
    }

    @Override
    public void UpdatePosition(double time, double lat, double lon) {
        Log.v(TAG,"Time: " + time + "\nLat: " + lat + "\nLon: " + lon);
    }

    @Override
    public void onServiceConnected() {
        Log.v(TAG,"Service connected");
    }
}
