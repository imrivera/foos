package com.tuenti.foos;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by adelcampo on 16/06/16.
 */
public class BluetoothMgr extends ScanCallback {
    public static final int AVAILABLE_BEACONS = 4;
    public static final int FRAME_TYPE_OFFSET = 11;
    public static final int EDDYSTONE_URL_FLAG = 0x10;
    public static final int TX_POWER_OFFSET = 12;
    public static final String BEACON1_MAC = "E9:A2:96:32:7D:A1";
    public static final String BEACON2_MAC = "DA:DD:A6:DE:22:8F";
    public static final String BEACON3_MAC = "DF:5A:57:36:FB:5B";
    public static final String BEACON4_MAC = "D2:73:A4:46:55:29";
    private static final int MAX_TIMEOUT = 15000;

    private BluetoothAdapter mAdapter;
    private BluetoothLeScanner mScanner;
    private ArrayList<ScanFilter> mFilters;
    private ScanSettings mSettings;
    private HashMap<String, Beacon> mBeacons;
    private HashMap<String, Double> mRefreshTimes;
    private BeaconsCallback mCallback;

    public BluetoothMgr(BeaconsCallback callback) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mScanner = mAdapter.getBluetoothLeScanner();
        mSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        mFilters = new ArrayList<>();
        mFilters.add(new ScanFilter.Builder().setDeviceAddress(BEACON1_MAC).build());
        mFilters.add(new ScanFilter.Builder().setDeviceAddress(BEACON2_MAC).build());
        mFilters.add(new ScanFilter.Builder().setDeviceAddress(BEACON3_MAC).build());
        mFilters.add(new ScanFilter.Builder().setDeviceAddress(BEACON4_MAC).build());

        mBeacons = new HashMap<>();
        mBeacons.put(BEACON1_MAC, new Beacon());
        mBeacons.put(BEACON2_MAC, new Beacon());
        mBeacons.put(BEACON3_MAC, new Beacon());
        mBeacons.put(BEACON4_MAC, new Beacon());

        mCallback = callback;
    }

    public void scanForBeacons() {
        Log.d("FOOS", "Scan Started");
        mScanner.startScan(mFilters, mSettings, this);
    }

    public void stopScanning() {
        Log.d("FOOS", "Scan finished");
        mScanner.stopScan(this);
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        ScanRecord record = result.getScanRecord();
        byte data[] = record.getBytes();
        String kk = new String(data);

        if (data[FRAME_TYPE_OFFSET] == EDDYSTONE_URL_FLAG) {
            int txPower = data[TX_POWER_OFFSET];
            String macAddress = result.getDevice().getAddress();

            Beacon beacon = mBeacons.get(macAddress);
            beacon.updateBeacon(macAddress, result.getRssi(), txPower);
        }

        for (Beacon b : mBeacons.values())
        {
            long currentTime = System.currentTimeMillis();
            if (currentTime - b.getLastUpdateTime() > MAX_TIMEOUT)
            {
                b.reset();
            }
        }

        mCallback.onBeaconsReceived(mBeacons);

        super.onScanResult(callbackType, result);
    }

    // ScanCallback methods

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        Log.d("FOOS", "jsdlñkfjsñdkfj");
        super.onBatchScanResults(results);
    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
    }

    public interface BeaconsCallback {
        void onBeaconsReceived(HashMap<String, Beacon> mBeacons);
    }
}
