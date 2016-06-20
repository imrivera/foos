package com.tuenti.foos;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adelcampo on 16/06/16.
 */
public class FoosUser {
    private String mName;

    public FoosUser(String name) {
        this.mName = name;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
