package com.mobilecomputing.dokikiosk;

import java.security.Key;

public class GlobalState {
    private String token;
    private Key privateKey;

    public String getToken() {
        return token;
    }
    public void setToken(String newToken) {
        token = newToken;
    }

    public Key getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(Key newKey) {
        privateKey = newKey;
    }
    public static GlobalState getInstance() {
        return single;
    }

    private static final GlobalState single = new GlobalState();

    GlobalState() {
        token = null;
    }
}
