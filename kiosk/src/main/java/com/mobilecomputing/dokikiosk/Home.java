package com.mobilecomputing.dokikiosk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Home extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private TextView bluetoothStatus;
    private TextView mainMessage;
    private AcceptThread serverSocketThread;
    private Handler bluetoothHandler, mainHandler;

    private final static int MESSAGE_RECEIVED = 1;
    private final static int UPDATE_BLUETOOTH_STATUS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        // setSupportActionBar(myToolbar);

        // Set up handlers for display elements
        bluetoothStatus = (TextView) findViewById(R.id.bluetooth_status);
        mainMessage = (TextView) findViewById(R.id.main_message);

        mainHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_RECEIVED) {
                    mainMessage.setText(new String((byte[]) msg.obj));
                }
            }
        };

        bluetoothHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == UPDATE_BLUETOOTH_STATUS) {
                    bluetoothStatus.setText((String) msg.obj);
                }
            }
        };

        // Set up bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            bluetoothStatus.setText("Sorry, this device does not support Bluetooth");
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }
        }

        serverSocketThread = new AcceptThread(bluetoothAdapter);
        serverSocketThread.start();
    }

    public void toLoginActivity(View view) {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private final String NAME = "DOKI Kiosk";
        private final UUID MY_UUID = new UUID(0x0000000000000000L, 0xdeadbeef0badcafeL);

        public AcceptThread(BluetoothAdapter mBluetoothAdapter) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
            }
            mmServerSocket = tmp;

            bluetoothHandler.obtainMessage(UPDATE_BLUETOOTH_STATUS, -1, -1, "Waiting for connection").sendToTarget();
        }

        public void run() {
            BluetoothSocket socket;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        break;
                    }
                    break;
                }
            }
        }

        public void manageConnectedSocket(BluetoothSocket socket) {
            bluetoothHandler.obtainMessage(UPDATE_BLUETOOTH_STATUS, -1, -1, "Waiting for message").sendToTarget();
            ConnectedThread thread = new ConnectedThread(socket);
            thread.start();
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                bluetoothHandler.obtainMessage(UPDATE_BLUETOOTH_STATUS, -1, -1, "Server not running").sendToTarget();
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }

        private class ConnectedThread extends Thread {
            private final BluetoothSocket mmSocket;
            private final InputStream mmInStream;
            private final OutputStream mmOutStream;

            public ConnectedThread(BluetoothSocket socket) {
                mmSocket = socket;
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                // Get the input and output streams, using temp objects because
                // member streams are final
                try {
                    tmpIn = socket.getInputStream();
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) {
                }

                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }

            public void run() {
                byte[] buffer;  // buffer store for the stream
                int bytes; // bytes returned from read()

                // Keep listening to the InputStream until an exception occurs
                while (true) {
                    try {
                        // Read from the InputStream
                        byte[] encodedBytes = new byte[2048];
                        bytes = mmInStream.read(encodedBytes);
                        Cipher c = Cipher.getInstance("RSA");
                        c.init(Cipher.DECRYPT_MODE, GlobalState.getInstance().getPrivateKey());
                        buffer = c.doFinal(encodedBytes, 0, bytes);
                        // Send the obtained bytes to the UI activity
                        mainHandler.obtainMessage(MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                        bluetoothHandler.obtainMessage(UPDATE_BLUETOOTH_STATUS, -1, -1, "Message received").sendToTarget();
                    } catch (IOException e) {
                        break;
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    }
                }
            }

            /* Call this from the main activity to shutdown the connection */
            public void cancel() {
                try {
                    mmSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}