package com.ipltd_bg.daisyandroid;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ipltd_bg.daisyandroid.BoundService.Data.ArticleData;
import com.ipltd_bg.daisyandroid.BoundService.Data.ArticleSaleData;
import com.ipltd_bg.daisyandroid.BoundService.Data.CompanyData;
import com.ipltd_bg.daisyandroid.BoundService.Data.DailyReportData;
import com.ipltd_bg.daisyandroid.BoundService.Data.FreeSaleData;
import com.ipltd_bg.daisyandroid.BoundService.Data.OperatorData;
import com.ipltd_bg.daisyandroid.BoundService.Data.StartFiskData;
import com.ipltd_bg.daisyandroid.BoundService.Data.TotalFiskData;
import com.ipltd_bg.daisyandroid.Enums.DaisyConnectionStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by alkon on 30-Mar-17.
 */

public class DaisyBarcodeBlueTooth extends Service {



    //статут на връзката
    public DaisyConnectionStatus Status = DaisyConnectionStatus.Disconnected;


    //За нуждите на BOUND SERVICE
    public class LocalBinder extends Binder {
        DaisyBarcodeBlueTooth getService() {
            // Return this instance of LocalService so clients can call public methods
            return DaisyBarcodeBlueTooth.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    private BluetoothAdapter mBluetoothAdapter;

    //Идентификатор на com през bluetooth
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;

    //Входящ поток
    private InputStream mmInStream = null;

    //Изходящ поток
    private OutputStream mmOutStream = null;


    //Конструктор
    public DaisyBarcodeBlueTooth() {

//

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onCreate();
    }

    //Осъществява връзка с устройството
    public boolean Connect(String deviceName) {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            this.Status = DaisyConnectionStatus.NotAbleToConnect;
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() == 0) {
            // More than one device paired
            this.Status = DaisyConnectionStatus.NotAbleToConnect;
        }
        Iterator<BluetoothDevice> iter = pairedDevices.iterator();
        while (iter.hasNext()) {
            mmDevice = iter.next();
            if (mmDevice.getName().equals(deviceName)) {
                break;
            } else
                mmDevice = null;

        }


        if (mmDevice == null || this.Status != DaisyConnectionStatus.Disconnected)
            return false;

        BluetoothSocket tmp = null;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            //cannot create Rfcomm
            this.Status = DaisyConnectionStatus.NotAbleToConnect;
        }
        mmSocket = tmp;
        mBluetoothAdapter.cancelDiscovery();
        InputStream tmpIn;
        OutputStream tmpOut;

//        try {
//            // Connect to the remote device through the socket. This call blocks
//            // until it succeeds or throws an exception.
//            mmSocket.connect();
//        } catch (IOException connectException) {
//            this.Status = DaisyConnectionStatus.NotAbleToConnect;
//            // Unable to connect; close the socket and return.
//            try {
//                mmSocket.close();
//            } catch (IOException closeException) {
//                //Could not close the client socket
//            }
//            return false;
//        }

        try {
            tmpIn = mmSocket.getInputStream();
        } catch (IOException e) {
            //"Error occurred when creating input stream", e);
            this.Status = DaisyConnectionStatus.NotAbleToConnect;
            return false;
        }
        try {
            tmpOut = mmSocket.getOutputStream();
        } catch (IOException e) {
            //"Error occurred when creating output stream", e);
            this.Status = DaisyConnectionStatus.NotAbleToConnect;
            return false;
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;



        this.Status = DaisyConnectionStatus.Connected;
        return true;
    }
    //Преустановява връзката с устройството
    public void Disconnect() {

        if (this.Status != DaisyConnectionStatus.Connected)
            return;


        if (this.mmSocket != null) {
            try {
                this.mmSocket.close();
            } catch (IOException e) {

            }
            this.mmSocket = null;
            this.Status = DaisyConnectionStatus.Disconnected;
        }
        //Log.d(TAG, "closed");
    }


    public void Reading(){
        byte[] mmBuffer = new byte[1024];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer);
                // Send the obtained bytes to the UI activity.

            } catch (IOException e) {
                break;
            }
        }
    }




}
