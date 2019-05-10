package com.wtbtest.locationlogger;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class NtripActivity extends AppCompatActivity {

    private static final String TAG = "NtripActivity";

    private NtripEurefTest ntripEurefTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ntrip);
        if(!NtripService.isRunning()){
          Log.v(TAG,"Service isn't running...");
        }
        else{
            ntripEurefTest = new NtripEurefTest(this);
        }
    }
}
