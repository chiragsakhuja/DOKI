package com.mobilecomputing.dokikiosk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mobilecomputing.dokilibrary.HttpRequestTaskAsync;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class Register extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void registerKiosk(View view) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.genKeyPair();
            Key publicKey = kp.getPublic();
            Key privateKey = kp.getPrivate();

            String macAddress = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(), "bluetooth_address");

            HttpRequestTaskAsync register = new HttpRequestTaskAsync(new URL(getString(R.string.server_url) + getString(R.string.url_register_kiosk)));
            JSONObject postData = new JSONObject();
            postData.put("token", GlobalState.getInstance().getToken());
            JSONObject locationData = new JSONObject();
            locationData.put("x", -7.0);
            locationData.put("y", 8.1);
            postData.put("loc", locationData);
            postData.put("pubkey", publicKey.getEncoded());
            postData.put("mac", macAddress);
            register.execute(postData);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
