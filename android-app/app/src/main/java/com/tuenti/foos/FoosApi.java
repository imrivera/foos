package com.tuenti.foos;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by adelcampo on 16/06/16.
 */
public class FoosApi {
    private static final String FOOS_URL = "http://10.0.35.96:8421/";

    public void sendPositionInfo(Map<String, Beacon> beacons, FoosUser user) {
        HttpRequester requester = new HttpRequester();
        long currentTime = System.currentTimeMillis();
        JSONObject json = new JSONObject();
        try {
            json.put("user", user.toString());
            JSONArray array = new JSONArray();
            for (Beacon b : beacons.values()) {
                if (b.getRssi() < 100) {
                    array.put(b.toJson());
                }
            }

            json.put("beacons", array);

            Request r = new Request(FOOS_URL, json.toString(), Request.HttpMethod.POST);
            requester.execute(r);

            requester.get();

        } catch (JSONException ex) {
            Log.d("FOOS", "there was a problem creating json");
        } catch (InterruptedException ex) {
            //DO Nothing
        } catch (ExecutionException ex) {
            ex.printStackTrace();
        }
    }

    public void getPlayerQueue() {

    }

    public void enqueueToPlay(FoosUser user) {

    }
}
