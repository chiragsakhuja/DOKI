package com.mobilecomputing.dokimobile;

import android.location.Location;

import java.io.DataOutputStream;

public class Communication {
    public ServerInfo getNearbyInfo(Location currentLocation) {
        return null;
    }

    public void sendToKiosk(DataOutputStream data) {

    }
}

class ServerInfo {
    String macAddress;
    Location location;
}