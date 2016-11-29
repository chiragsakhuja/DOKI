package com.mobilecomputing.dokilibrary;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequestTask {
    private final URL postURL;

    public HttpRequestTask(URL url) {
        postURL = url;
    }

    public String execute(JSONObject... objects) {
        String responseMessage = null;

        for (JSONObject i : objects) {
            try {
                /*
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream caInput = new BufferedInputStream(new FileInputStream("server.crt"));
                Certificate ca;
                try {
                    ca = cf.generateCertificate(caInput);
                    System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
                } finally {
                    caInput.close();
                }

                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);

                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, tmf.getTrustManagers(), null);
                */
                HttpURLConnection connection = (HttpURLConnection) postURL.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST"); // here you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                connection.setRequestProperty("Content-Type", "application/json"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`
                connection.connect();

                DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                output.writeBytes(i.toString());
                output.flush();

                BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int response = connection.getResponseCode();
                if (response < 200 || response > 399) {
                    throw new Exception("Could not POST");
                }

                responseMessage = input.readLine();

                output.close();
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return responseMessage;
    }
}