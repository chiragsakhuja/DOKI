package com.mobilecomputing.dokilibrary;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequestTask extends AsyncTask<JSONObject, Void, Void> {
    private final URL postURL;

    HttpRequestTask(URL url) {
        postURL = url;
    }

    @Override
    protected Void doInBackground(JSONObject... objects) {
        for (JSONObject i : objects) {
            try {
                HttpURLConnection connection = (HttpURLConnection) postURL.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST"); // here you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                connection.setRequestProperty("Content-Type", "application/json"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`
                connection.connect();

                DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                output.writeBytes(i.toString());
                output.flush();

                int response = connection.getResponseCode();
                if (response < 200 || response > 399) {
                    throw new Exception("Could not POST");
                }

                output.close();
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}