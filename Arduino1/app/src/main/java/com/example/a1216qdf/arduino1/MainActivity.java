package com.example.a1216qdf.arduino1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    TextView myLabel;
    Button openButton,closeButton,btnahead,btnLeft,btnRight,btnBack,btnStop;
    BluetoothAdapter mbluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile  boolean stopWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openButton = (Button)findViewById(R.id.btnConnect);
        closeButton = (Button)findViewById(R.id.btndisconnect);
        btnahead = (Button)findViewById(R.id.btnahead);
        btnLeft = (Button)findViewById(R.id.btnLeft);
        btnRight = (Button)findViewById(R.id.btnRight);
        btnBack = (Button)findViewById(R.id.btnBack);
        btnStop = (Button)findViewById(R.id.btnStop);
        myLabel = (TextView)findViewById(R.id.label);

        openButton.setOnClickListener(openbtListener);
        closeButton.setOnClickListener(closebtListener);
        btnahead.setOnClickListener(moveListener);
        btnLeft.setOnClickListener(moveListener);
        btnRight.setOnClickListener(moveListener);
        btnBack.setOnClickListener(moveListener);
        btnStop.setOnClickListener(moveListener);

    }



    public Button.OnClickListener openbtListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try{
                findBT();
                openBT();
            }
            catch (IOException ex){

            }
        }
    };

    public Button.OnClickListener closebtListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try{
                closeBT();
            }
            catch (IOException ex){

            }
        }
    };

    public Button.OnClickListener moveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.btnahead: {
                    try {
                        sendData("1");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case R.id.btnBack: {
                    try {
                        sendData("2");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case R.id.btnLeft: {
                    try {
                        sendData("3");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case R.id.btnRight: {
                    try {
                        sendData("4");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case R.id.btnStop: {
                    try {
                        sendData("5");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    };

    public void findBT(){
        mbluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mbluetoothAdapter == null){
            myLabel.setText("NO BLUETOOTH");
        }

        if (!mbluetoothAdapter.isEnabled()){
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBlueTooth,0);
        }

        Set<BluetoothDevice> pairedDevices = mbluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0){
            for (BluetoothDevice device :pairedDevices){

                myLabel.setText(device.getName());
                mmDevice = device;
                break;
            }
        }
    }

    public void openBT() throws IOException{
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        if (mmDevice!=null){
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);

            mmSocket.connect();
            if (mmSocket.isConnected()){
                openButton.setEnabled(false);
            }
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();

        }
    }

    public void beginListenForData(){
        final Handler handler = new Handler();
        final byte delimiter = 10;

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker){
                    try{
                        int bytesAvaible = mmInputStream.available();
                        if (bytesAvaible > 0){
                            byte[] packetBytes = new byte[bytesAvaible];
                            mmInputStream.read(packetBytes);
                            for (int i=0;i<bytesAvaible;i++)
                            {
                                byte b = packetBytes[i];
                                if (b == delimiter){
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer,0,encodedBytes,0,encodedBytes.length);
                                    final String data = new String(encodedBytes,"US-ASCII");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            String t1 = myLabel.getText().toString();
                                            t1 = t1+data;
                                            myLabel.setText(t1);
                                        }
                                    });
                                }
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex){
                        stopWorker = true;
                    }
                }
            }
        });
        workerThread.start();
    }

    public void sendData(String i) throws IOException{
        String msg = i;
        mmOutputStream.write(msg.getBytes());
        myLabel.setText("Data Sent");
    }

    public void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        myLabel.setText("Bluetooth Closed");
        openButton.setEnabled(true);
    }
}
