package com.ipltd_bg.daisyandroid;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ipltd_bg.daisyandroid.BoundService.Data.ArticleData;
import com.ipltd_bg.daisyandroid.BoundService.Data.ArticleSaleData;
import com.ipltd_bg.daisyandroid.BoundService.Data.CompanyData;
import com.ipltd_bg.daisyandroid.BoundService.Data.DailyReportData;
import com.ipltd_bg.daisyandroid.BoundService.Data.FontInfo;
import com.ipltd_bg.daisyandroid.BoundService.Data.FreeSaleData;
import com.ipltd_bg.daisyandroid.BoundService.Data.OperatorData;
import com.ipltd_bg.daisyandroid.BoundService.Data.StartFiskData;
import com.ipltd_bg.daisyandroid.BoundService.Data.TotalFiskData;
import com.ipltd_bg.daisyandroid.Enums.DaisyConnectionStatus;
import com.ipltd_bg.daisyandroid.Enums.ParagraphAlignment;

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

public class DatecsNFPBlueTooth extends Service {

    //Последна срещната грешка или НЯМА ГРЕШКА
    private String LastStatusError;


    //статут на връзката
    public DaisyConnectionStatus Status = DaisyConnectionStatus.Disconnected;

    //Извлича последната грешка или НЯМА ГРЕШКА
    public String getLastStatusError() {
        return LastStatusError;
    }

    //Подава стойност на последната грешка
    public void setLastStatusError(String lastStatusError) {
        LastStatusError = lastStatusError;
    }

    //За нуждите на BOUND SERVICE
    public class LocalBinder extends Binder {
        DatecsNFPBlueTooth getService() {
            // Return this instance of LocalService so clients can call public methods
            return DatecsNFPBlueTooth.this;
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
    public DatecsNFPBlueTooth() {

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

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            this.Status = DaisyConnectionStatus.NotAbleToConnect;
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                //Could not close the client socket
            }
            return false;
        }

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
        try {
            mmOutStream.write(new byte[]{0x1B, 0x75, 17}); //Сетва 1251
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }


    //Преустановява връзката с устройството
    public void Disconnect() {

        if (this.Status != DaisyConnectionStatus.Connected)
            return;


        if (this.mmInStream != null) {
            try {
                this.mmInStream.close();
            } catch (IOException e) {

            }
            this.mmInStream = null;
        }
        if (this.mmOutStream != null) {
            try {
                this.mmOutStream.close();
            } catch (IOException e) {

            }
            this.mmOutStream = null;
        }
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

    private byte[] ListToArr(List<Byte> lst) {
        byte[] data = new byte[lst.size()];
        for (int i = 0; i < lst.size(); i++)
            data[i] = lst.get(i);
        return data;

    }

    public boolean PrintLine(String line) {
        if (this.Status != DaisyConnectionStatus.Connected)
            return false;

        List<Byte> lText = new ArrayList<Byte>();
        try {
            for (byte b : line.getBytes("cp1251"))
                lText.add(b);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        lText.add((byte) 0xA);
        try {
            //mmOutStream.write(new byte[]{0x1b, 0x21, (byte) 0x0});
            mmOutStream.write(ListToArr(lText));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean PrintNewLine() {
        if (this.Status != DaisyConnectionStatus.Connected)
            return false;

        try {
            //mmOutStream.write(new byte[]{0x1b, 0x21, (byte) 0x0});
            mmOutStream.write(new byte[]{(byte) 0xA});
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    public boolean SetFont(FontInfo info) {
        if (this.Status != DaisyConnectionStatus.Connected)
            return false;

        int bt = 0;
        if (!info.isBig())
            bt += 1;
        if (info.isBold())
            bt += 8;
        if (info.isUnderline())
            bt += 128;
        try {
            mmOutStream.write(new byte[]{0x1b, 0x21, (byte) bt});

        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    public boolean SetAlignment(ParagraphAlignment alignment) {
        if (this.Status != DaisyConnectionStatus.Connected)
            return false;

        int bt = 0;
        switch (alignment) {
            case Left:
                bt = 0;
                break;
            case Center:
                bt = 1;
                break;
            case Right:
                bt = 2;
                break;
        }
        try {
            mmOutStream.write(new byte[]{0x1b, 0x61, (byte) bt});

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

//    //00000000
//    public boolean PrintImportantLine(String line) {
//        if (this.Status != DaisyConnectionStatus.Connected)
//            return false;
//
//        List<Byte> lText = new ArrayList<Byte>();
//        try {
//            for (byte b : line.getBytes("cp1251"))
//                lText.add(b);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        lText.add((byte) 0xA);
//        try {
//            mmOutStream.write(new byte[]{0x1b, 0x21, (byte) 0x9D});
//
//            mmOutStream.write(ListToArr(lText));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return true;
//    }

    public boolean PrintFeed(byte n) {
        if (this.Status != DaisyConnectionStatus.Connected)
            return false;

        try {
            mmOutStream.write(new byte[]{0x1b, 0x4A, n});
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

}
