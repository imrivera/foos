package com.tuenti.foos;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by adelcampo on 16/06/16.
 */
public class HttpRequester extends AsyncTask<Request, Integer, String> {

    @Override
    protected String doInBackground(Request... params) {
        for (Request r : params) {
            try {
                return doRequest(r);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    private String doRequest(Request request) throws IOException {
        URL url = new URL(request.getUrl());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        String result = null;
        try {
            if (request.getMethod() == Request.HttpMethod.POST) {
                urlConnection.setDoOutput(true);
            }

            //urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestMethod(request.getMethod().toString());
            urlConnection.setFixedLengthStreamingMode(request.getBody().getBytes().length);

            if (!request.getBody().isEmpty()) {
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                writeStream(out, request.getBody());
            }

            if (urlConnection.getResponseCode() == 200) {
             //   InputStream in = new BufferedInputStream(urlConnection.getInputStream());
              //  result = readStream(in);
            }

        } finally {
            urlConnection.disconnect();
        }

        return result;
    }

    private String readStream(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String response = reader.readLine();

        while (response != null) {
            sb.append(response);
            response = reader.readLine();
        }

        return sb.toString();
    }

    private void writeStream(OutputStream out, String data) throws IOException {
        BufferedOutputStream ow = new BufferedOutputStream(out);
        ow.write(data.getBytes());
        ow.flush();
        ow.close();
    }
}
