package com.softaan.sweetsleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private String deviceName = null;
    private String deviceAddress;
    public String data;
    public String syncTime;
    public String nextInterval;
    public String[] dataWithTime;
    public RecyclerView files;
    public RecyclerView.Adapter storedFileAdapter;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;

    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI Initialization
        final Button buttonConnect = findViewById(R.id.buttonConnect);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final FloatingActionButton sync = findViewById(R.id.sync);
        final Button buttonCancel = findViewById(R.id.buttonCancel);
        buttonCancel.setEnabled(false);
        buttonCancel.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.GONE);
        sync.setEnabled(false);
        data = "";
        syncTime = "";
        dataWithTime = new String[]{};

        showFiles();

        // If a bluetooth device has been selected from SelectDeviceActivity
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null){
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progress and connection status
            toolbar.setSubtitle("Connecting to " + deviceName + "...");
            progressBar.setVisibility(View.VISIBLE);
            buttonConnect.setEnabled(false);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter,deviceAddress);
            createConnectThread.start();
        }

        /*
        Second most important piece of Code. GUI Handler
         */
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case CONNECTING_STATUS:
                        switch(msg.arg1){
                            case 1:
                                toolbar.setSubtitle("Connected to " + deviceName);
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(false);
                                buttonConnect.setVisibility(View.INVISIBLE);
                                buttonCancel.setEnabled(true);
                                buttonCancel.setVisibility(View.VISIBLE);
                                sync.setEnabled(true);
                                break;
                            case -1:
                                toolbar.setSubtitle("Failed to connect");
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                sync.setEnabled(false);
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        data += arduinoMsg + "\n";
                        break;
                }
                if (data.contains("Data Complete"))
                    addTime();
                    writeData();
                    showFiles();

            }
        };

        // Select Bluetooth Device
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Move to adapter list
                Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                startActivity(intent);
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectedThread.cancel();
                toolbar.setSubtitle("Disconnected");
                buttonConnect.setEnabled(true);
                buttonConnect.setVisibility(View.VISIBLE);
                buttonCancel.setEnabled(false);
                buttonCancel.setVisibility(View.INVISIBLE);
                sync.setEnabled(false);
            }
        });

        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncTime = getTime();
                connectedThread.write("Send Data");
            }
        });
    }

    void showFiles() {
        ArrayList<StoredFile> files = initFiles();
        this.files = findViewById(R.id.files);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        this.files.setLayoutManager(mLayoutManager);

        storedFileAdapter = new StoredFileAdapter(MainActivity.this, files);
        this.files.setAdapter(storedFileAdapter);
    }

    private ArrayList<StoredFile> initFiles(){
        ArrayList<StoredFile> list = new ArrayList<>();
        File folder = new File(getFilesDir().getPath());
        File[] filesInFolder = folder.listFiles();
        for (File file : filesInFolder) {
            if (!file.isDirectory()) {
                list.add(new StoredFile(file.getName()));
            }
        }
        return list;
    }

    public String getTime(){
        Date now = new Date();
        String hour = now.toString().split(" ")[3].split(":")[0];
        String minute = now.toString().split(" ")[3].split(":")[1];
        return hour + ":" + minute;
    }

    public String getDate(){
        Date now = new Date();
        String day = now.toString().split(" ")[2];
        String month = now.toString().split(" ")[1];
        String year = now.toString().split(" ")[5];
        return day + "-" + month + "-" + year;
    }

    private void addTime() {
        int hour, minute;
        nextInterval = syncTime;
        dataWithTime = data.split("\n");
        for (int i = dataWithTime.length - 2; i >= 1; i--) {
            dataWithTime[i] = nextInterval + ", " + dataWithTime[i] + "\n";
            hour = Integer.parseInt(nextInterval.split(":")[0]);
            minute = Integer.parseInt(nextInterval.split(":")[1]);
            if (hour == 0 && minute == 0){
                hour = 12;
                minute = 59;
            } else if (minute != 0) {
                minute--;
            } else if (minute == 0){
                hour--;
                minute = 59;
            }
            nextInterval = hour + ":" + minute;
        }
        data = "";
        for (String line : dataWithTime){
            data += line ;
        }
    }

    public void writeData(){
        data = data.replace("Data Complete", "");
        data = data.replace("�", "");
        String fileName = getDate() + ".csv";
        FileOutputStream fos;
        try {
            fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            //default mode is PRIVATE, can be APPEND etc.
            fos.write(data.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {e.printStackTrace();}
          catch (IOException e) {e.printStackTrace();}
    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
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
            } catch (IOException ignored) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n'){
                        readMessage = new String(buffer,0,bytes);
                        Log.e("Arduino Message",readMessage);
                        handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) { }
        }
    }

    /* ============================ Terminate Connection at BackPress ====================== */
    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        if (createConnectThread != null) {
            createConnectThread.cancel();
        }
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}