package com.tuenti.foos;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adelcampo on 16/06/16.
 */
public class Beacon {
    private int mRssi;
    private int mPrevRssi;
    private int mTxPower;
    private String mMacAddress;
    private long mLastUpdateTime;

    public Beacon() {
    }

    public void updateBeacon(String mac, int rssi, int txPower) {
        mPrevRssi = mRssi;
        mRssi = rssi;
        mTxPower = txPower;
        mMacAddress = mac;
        mLastUpdateTime = System.currentTimeMillis();
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            json.put("beacon", mMacAddress);
            json.put("rssi", getRssi());
            json.put("txpower", Math.abs(mTxPower));
            json.put("distance", getDistance());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    public int getRssi() {
        return Math.abs((mRssi + mPrevRssi) / 2);
    }

    public long getLastUpdateTime() {
        return mLastUpdateTime;
    }

    public double getDistance() {
        if (mRssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = mRssi * 1.0 / mTxPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }

    public void reset() {
        mPrevRssi = Integer.MAX_VALUE;
        mRssi = Integer.MAX_VALUE;
        mTxPower = Integer.MAX_VALUE;
        mMacAddress = "";
        mLastUpdateTime = Integer.MAX_VALUE;
    }
}
