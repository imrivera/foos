package com.tuenti.foos;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements BluetoothMgr.BeaconsCallback {
    private BluetoothMgr mBtManager;
    private EditText mNameText;
    private Button mRegisterButton;
    private Button mLeaveButton;
    private FoosUser mUser;
    private FoosApi mApi;

    //DBG
    private TextView mBeacon1;
    private TextView mBeacon2;
    private TextView mBeacon3;
    private TextView mBeacon4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mNameText = (EditText) findViewById(R.id.nameText);
        mRegisterButton = (Button) findViewById(R.id.register_button);
        mLeaveButton = (Button) findViewById(R.id.leave_button);

        mBeacon1 = (TextView) findViewById(R.id.dbg_beacon1);
        mBeacon2 = (TextView) findViewById(R.id.dbg_beacon2);
        mBeacon3 = (TextView) findViewById(R.id.dbg_beacon3);
        mBeacon4 = (TextView) findViewById(R.id.dbg_beacon4);

        mApi = new FoosApi();
    }

    @Override
    public void onStart() {
        super.onStart();

        mBtManager = new BluetoothMgr(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        mBtManager.stopScanning();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permisions[], int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mBtManager.scanForBeacons();
        }
    }

    @Override
    public void onBeaconsReceived(HashMap<String, Beacon> beacons) {
        int distance1 = beacons.get(BluetoothMgr.BEACON1_MAC).getRssi();
        int distance2 = beacons.get(BluetoothMgr.BEACON2_MAC).getRssi();
        int distance3 = beacons.get(BluetoothMgr.BEACON3_MAC).getRssi();
        int distance4 = beacons.get(BluetoothMgr.BEACON4_MAC).getRssi();

        int minDistance = Math.min(distance1, Math.min(distance2, Math.min(distance3, distance4)));
     //   int maxDistance = Math.max(distance1, Math.max(distance2, Math.max(distance3, distance4)));

        mBeacon1.setText("Beacon 1: " + distance1);
        mBeacon2.setText("Beacon 2: " + distance2);
        mBeacon3.setText("Beacon 3: " + distance3);
        mBeacon4.setText("Beacon 4: " + distance4);

        if (minDistance == distance1) {
            mBeacon1.setTextColor(Color.GREEN);
            mBeacon2.setTextColor(Color.BLACK);
            mBeacon3.setTextColor(Color.BLACK);
            mBeacon4.setTextColor(Color.BLACK);
        } else if (minDistance == distance2) {
            mBeacon1.setTextColor(Color.BLACK);
            mBeacon2.setTextColor(Color.GREEN);
            mBeacon3.setTextColor(Color.BLACK);
            mBeacon4.setTextColor(Color.BLACK);
        } else if (minDistance == distance3) {
            mBeacon1.setTextColor(Color.BLACK);
            mBeacon2.setTextColor(Color.BLACK);
            mBeacon3.setTextColor(Color.GREEN);
            mBeacon4.setTextColor(Color.BLACK);
        } else {
            mBeacon1.setTextColor(Color.BLACK);
            mBeacon2.setTextColor(Color.BLACK);
            mBeacon3.setTextColor(Color.BLACK);
            mBeacon4.setTextColor(Color.GREEN);
        }

        mApi.sendPositionInfo(beacons, mUser);
    }

    public void leaveFoos(View v) {
        mBtManager.stopScanning();
    }

    public void registerOnFoos(View v) {
        mUser = new FoosUser(mNameText.getText().toString());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mBtManager.scanForBeacons();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
        }
    }
}
