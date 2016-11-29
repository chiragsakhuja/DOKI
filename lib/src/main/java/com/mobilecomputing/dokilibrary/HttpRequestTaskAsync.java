package com.mobilecomputing.dokilibrary;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.net.URL;

public class HttpRequestTaskAsync extends AsyncTask<JSONObject, Void, String> {
    private final HttpRequestTask syncTask;

    public HttpRequestTaskAsync(URL url) {
        syncTask = new HttpRequestTask(url);
    }

    @Override
    protected String doInBackground(JSONObject... objects) {
        return syncTask.execute(objects);
    }
}