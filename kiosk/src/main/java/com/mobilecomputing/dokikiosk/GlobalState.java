package com.mobilecomputing.dokikiosk;

public class GlobalState {
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String newToken) {
        token = newToken;
    }

    public static GlobalState getInstance() {
        return single;
    }

    private static final GlobalState single = new GlobalState();

    GlobalState() {
        token = null;
    }
}
