package com.mobilecomputing.dokimobile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Home extends AppCompatActivity
{
    private static final float kiosk_accuracy = 24.0f;
    private static final double kiosk_latitude = 30.2788;
    private static final double kiosk_altitude = 118.0;
    private static final double kiosk_longitude = -97.7748;
    private static final String kiosk_id = new String("DESKTOP-02PPB5V");
    /***************************************************************/
    private static final int dist_threshold = 100;  // in meters
    private static final int signal_threshold = -55; // in decibel
    private static final int accuracy_threshold = 50; // in meters
    /***************************************************************/
    private static final int MESSAGE_APPEND = 1;
    private static final int MESSAGE_OVERWRITE = 2;
    private static final int MESSAGE_DOKI = 3;
    /***************************************************************/
    private boolean data_ready = false;
    private boolean gps_in_use = false;
    private boolean analysis_in_progress = false;
    private boolean discovery_in_progress = false;
    private boolean kiosk_mode = false;
    /***************************************************************/
    private Handler mainHandler;
    private Location kiosk_loc;
    private TextView message_board;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private LocationManager mLocationManager;
    private BluetoothAdapter mBluetoothAdapter;
    /***************************************************************/
    /***************************************************************/
    protected void set_ready_flag(){data_ready = true;}
    protected void set_analysis_flag() {analysis_in_progress = true;}
    protected void reset_analysis_flag(){analysis_in_progress = false;}
    protected void enable_kiosk_mode() { kiosk_mode = true; }
    protected void disable_kiosk_mode() { kiosk_mode = false; }
    protected void stop_discovery() {discovery_in_progress = false;}
    protected void start_discovery()
    {
        discovery_in_progress = true;

        if (mBluetoothAdapter.startDiscovery())
        {
            message_board.setText("Searching for kiosk through Bluetooth\n");
        }
        else
        {
            message_board.setText("Starting bluetooth discovery failed!\n");
        }
    }
    /***************************************************************/
    /***************************************************************/
    private void    setup_kiosk_loc()
    {
        /* TO BE WRITTEN BY CHIRAG */
        kiosk_loc = new Location("kiosk_location");
        kiosk_loc.setLatitude(1.0);
        kiosk_loc.setAltitude(1.0);
        kiosk_loc.setAccuracy(1.0f);
        kiosk_loc.setLongitude(1.0);
    }

    @Override
    protected void onDestroy()
    {
        mBluetoothAdapter.disable();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FloatingActionButton loc_btn = (FloatingActionButton) findViewById(R.id.loc);
        FloatingActionButton send_btn = (FloatingActionButton) findViewById(R.id.send);
        FloatingActionButton connect_btn = (FloatingActionButton) findViewById(R.id.connect);
        final FloatingActionButton s_s_button = (FloatingActionButton) findViewById(R.id.start_stop);

        message_board = (TextView) findViewById(R.id.msg);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mBluetoothAdapter.enable();

        //setup_kiosk_loc();

        BroadcastReceiver dev_found_recv = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                if(BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

                    if((name!=null)&&name.equals(kiosk_id))
                    {
                        if(rssi>signal_threshold)
                        {
                            message_board.setText("Initiating data transfer to " + kiosk_id + " (RSSI = " + rssi + "dBm)\n");
                            stop_discovery();
                        }
                        else
                        {
                            message_board.setText("Kiosk in range (RSSI = " + rssi + "dBm)\n");
                        }
                    }
                }
            }
        };

        BroadcastReceiver dis_done_recv = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){

                String action = intent.getAction();

                if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                {
                    message_board.setText(message_board.getText() + "Discovery finished!\n");

                    if(discovery_in_progress)
                        mBluetoothAdapter.startDiscovery();
                }
            }
        };

        /* Register receivers */
        registerReceiver(dev_found_recv, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(dis_done_recv, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mmDevice = mBluetoothAdapter.getRemoteDevice("chj");
                (new ConnectThread()).start();
            }
        });

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mBluetoothAdapter.isEnabled()) {
                    message_board.setText("Bluetooth is not enabled!");
                } else {
                    OutputStream tmpOut = null;
                    OutputStream mmOutStream;

                    // Get the input and output streams, using temp objects because
                    // member streams are final
                    try {
                        tmpOut = mmSocket.getOutputStream();
                    } catch (IOException e) {
                    }

                    mmOutStream = tmpOut;

                    try {
                        mmOutStream.write(new byte[]{'k', 'a', 'm', 'y', 'a', 'r'});
                    } catch (IOException e) {
                    }

                    message_board.setText("Alright!");
                }
            }
        });

        loc_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(kiosk_mode)
                {
                    Snackbar.make(view, "Kiosk location detection is in progress!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                {
                    Snackbar.make(view, "Detecting kiosk location", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    enable_kiosk_mode();
                    request_location_updates();
                }
            }
        });

        s_s_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {

                if(analysis_in_progress)
                {
                    s_s_button.setImageResource(android.R.drawable.ic_media_play);
                    reset_analysis_flag();
                }
                else
                {
                    if(data_ready)
                    {
                        s_s_button.setImageResource(android.R.drawable.ic_media_pause);
                        set_analysis_flag();
                    }
                    else
                    {
                        Snackbar.make(view, "Still busy reading database!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
            }
        });

        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        assert(!gps_in_use);
                        assert(!kiosk_mode);
                        request_location_updates();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        mainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg)
            {
                if(msg.what == MESSAGE_OVERWRITE)
                {
                    message_board.setText((String)msg.obj);
                }

                if(msg.what == MESSAGE_APPEND)
                {
                    message_board.setText(message_board.getText()+(String)msg.obj);
                }

                if(msg.what == MESSAGE_DOKI)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                    builder.setMessage("DOKI thinks that you might have \""+(String)msg.obj+"\".\nWould you like to go to a health kiosk for further tests?");
                    builder.setNegativeButton("No",dialogClickListener);
                    builder.setPositiveButton("Yes",dialogClickListener);
                    builder.show();
                }
            }
        };

        DOKI engine = new DOKI();
    }

    protected void request_location_updates()
    {
        gps_in_use = true;

        message_board.setText("Requesting location updates...");

        try
        {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, mLocationListener);
        }
        catch(SecurityException e)
        {
            message_board.setText("permission denied while requesting GPS updates");
        }
    }

    protected void cancel_location_updates()
    {
        gps_in_use = false;

        try
        {
            mLocationManager.removeUpdates(mLocationListener);
        }
        catch(SecurityException e)
        {
            message_board.setText("permission denied while canceling GPS updates");
        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {

            if(kiosk_mode)
            {
                if(location.getAccuracy() < accuracy_threshold)
                {
                    kiosk_loc = location;

                    message_board.setText("Location set!");

                    disable_kiosk_mode();
                    cancel_location_updates();
                }
                else
                {
                    String res = "";

                    res += "Accuracy  : " + Float.toString(location.getAccuracy()) + "\n";
                    res += "Altitude  : " + Double.toString(location.getAltitude()) + "\n";
                    res += "Latitude  : " + Double.toString(location.getLatitude()) + "\n";
                    res += "Longitude : " + Double.toString(location.getLongitude()) + "\n";

                    message_board.setText(res);
                }
            }
            else
            {
                if(kiosk_loc.distanceTo(location)<dist_threshold)
                {
                    cancel_location_updates();
                    start_discovery();
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            message_board.setText("GPS location provider is disabled");
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            message_board.setText("GPS location provider is enabled");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            message_board.setText("GPS location provider's status has changed to " +  Integer.toString(status) );
        }
    };

    private class ConnectThread extends Thread {
        private final UUID MY_UUID = new UUID(0x0000000000000000L, 0xdeadbeef0badcafeL);

        public ConnectThread() {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private class DOKI extends Thread {

        private EKG user;
        private ArrayList<EKG> patients;
        private int cnt_match_thr;
        private InputStream files [] ;

        DOKI()
        {
            cnt_match_thr = 10;
            Signal.val_match_thr = 0.1;

            patients = new ArrayList<EKG>();

            files = new InputStream[10];

            try
            {
                files[0] = getAssets().open("fakePatient4.txt");

                for (int i = 1; i < 10; i++) {files[i] = getAssets().open(Integer.toString(i) + ".txt");}
            }
            catch(IOException e)
            {
                mainHandler.obtainMessage(MESSAGE_OVERWRITE,"Something bad happened while creating input stream\n" ).sendToTarget();
            }

            (new init_thread()).start();
            (new analyze_thread()).start();
        }

        public int search(int start_index)
        {
            for(int threshold=15; threshold>6; threshold--)
            {
                int curr_index = start_index;

                for(int i=0; i<patients.size(); i++)
                    patients.get(i).init_matches();

                int match_count = 100;

                /***********************************/
                if(!analysis_in_progress){return 0;}
                /************************************/

                while(match_count > 1)
                {
                    match_count = 0;

                    //System.out.println("Reading entry#"+Integer.toString(curr_index));

                    Signal val = user.read(curr_index);

                    for(int i=0; i<patients.size(); i++)
                    {
                        patients.get(i).search_next(val,threshold);

                        if(patients.get(i).anyMatch()) {match_count++;}
                    }

                    curr_index++;
                }

                /***********************************/
                if(!analysis_in_progress){return 0;}
                /************************************/

                // if there was no match, just lower threshold
                if(match_count==0) {continue;}

                // lets find out where the match was
                for(int i=0; i<patients.size(); i++)
                {
                    if(patients.get(i).anyMatch())
                    {
                        Integer match = patients.get(i).get_match();

                        /***********************************/
                        if(!analysis_in_progress){return 0;}
                        /************************************/

                        // lets make sure there is no more match
                        while(user.is_valid(curr_index))
                        {
                            Signal val2 = user.read(curr_index);

                            patients.get(i).search_next(val2,threshold);

                            curr_index++;

                            if(!patients.get(i).anyMatch()) {break;}

                            match = patients.get(i).get_match();
                        }

                        /* there was not enough correlation */
                        if((curr_index-start_index)<cnt_match_thr) { return curr_index; }

                        String s1 = patients.get(i).get_info();
                        String s2 = "[Accuracy: " + Integer.toString(threshold) + "/15]";
                        String s3 = "[#Match: " + Integer.toString(curr_index-start_index) + "]";
                        String s4 = "[" +   DateFormat.getTimeInstance().format(new Date()) + "]";
                        String msg = s4 + " " + s1 + " " + s2 + " " + s3 + " " + "\n";

                        mainHandler.obtainMessage(MESSAGE_DOKI,s1).sendToTarget();

                        //System.out.println(patients.get(i).get_info() + " [Accuracy: " + Integer.toString(threshold) + "/15] [Index: " + Integer.toString(match) + "/" + Integer.toString(patients.get(i).size())+"] [#Match: " + Integer.toString(curr_index-start_index) + "]");
                        //System.out.println("Path: ["+args[3+i]+"]");

                        return curr_index;
                    }
                }
            }

            // no match with any accuracy was found, go to next sample
            return  start_index+1;
        }

        private class analyze_thread extends Thread
        {
            private int index = 0;

            public void run()
            {
                while(true)
                {
                    if(analysis_in_progress && data_ready)
                    {
                        mainHandler.obtainMessage(MESSAGE_APPEND,"Searching for " +Integer.toString(index)+"\n").sendToTarget();

                        index = search(index);
                    }
                    else
                    {
                        index = 0;
                    }
                }
            }
        }

        private class init_thread extends Thread
        {
            public void run()
            {
                try
                {
                    user = new EKG(files[0]);

                    for(int i=1; i< files.length; i++) { patients.add(new EKG(files[i])); }

                    /* TODO : EKGs dont need to be thread */

                    user.start();

                    for(int i=0; i<patients.size(); i++) { patients.get(i).start(); }

                    mainHandler.obtainMessage(MESSAGE_OVERWRITE,"[" +   DateFormat.getTimeInstance().format(new Date()) + "] Reading database\n" ).sendToTarget();

                    user.join();

                    for(int i=0; i<patients.size(); i++) { patients.get(i).join(); }

                    mainHandler.obtainMessage(MESSAGE_APPEND,"[" +   DateFormat.getTimeInstance().format(new Date()) + "] Data is now loaded!\n").sendToTarget();

                    set_ready_flag();

                    return;
                }
                catch(InterruptedException e)
                {
                    mainHandler.obtainMessage(MESSAGE_OVERWRITE,"Interrupted Exception during EKG creation\n").sendToTarget();
                    System.exit(3);
                }
            }
        }
    }
}