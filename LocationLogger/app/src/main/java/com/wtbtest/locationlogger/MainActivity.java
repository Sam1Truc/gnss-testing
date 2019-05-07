package com.wtbtest.locationlogger;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static final int PERMISSION_REQUEST_CODE = 698;
    public static final int WRITE_REQUEST_CODE = 699;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLocationPermission(this);
        String [] persmissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestPermissions(persmissions, WRITE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                }
                else {
                    System.exit(0);
                }
            }
            case WRITE_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                else {

                }
            }
        }
    }

    public static void checkLocationPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MainActivity.PERMISSION_REQUEST_CODE);
        }
    }

    public void onClickFuseLocation(View view) {
        Intent intent = new Intent(this, FusedActivity.class);
        startActivity(intent);
    }


    public void onClickRawGNSSLocation(View view) {
        Intent intent = new Intent(this, RawGNSSActivity.class);
        startActivity(intent);
    }
}
