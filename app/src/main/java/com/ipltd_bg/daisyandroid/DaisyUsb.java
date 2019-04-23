package com.ipltd_bg.daisyandroid;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by alkon on 30-Mar-17.
 */

public class DaisyUsb extends Service {

    private UsbDeviceConnection connection;

    private UsbManager usbManager;

    private UsbEndpoint epoint;


    //статут на връзката
    public DaisyConnectionStatus Status = DaisyConnectionStatus.Disconnected;

    public UsbManager getUsbManager() {
        return usbManager;
    }

    public void setUsbManager(UsbManager usbManager) {
        this.usbManager = usbManager;
    }

    public UsbDeviceConnection getConnection() {
        return connection;
    }

    public void setConnection(UsbDeviceConnection connection) {
        this.connection = connection;
    }


    //За нуждите на BOUND SERVICE
    public class LocalBinder extends Binder {
        DaisyUsb getService() {
            // Return this instance of LocalService so clients can call public methods
            return DaisyUsb.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();


    //Конструктор
    public DaisyUsb() {

        this.Status = DaisyConnectionStatus.Disconnected;


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
    public boolean Connect(UsbManager usbManager) {

        if (this.Status != DaisyConnectionStatus.Disconnected)
            return false;
        this.setUsbManager(usbManager);
        HashMap<String, UsbDevice> deviceList = getUsbManager().getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        UsbDevice device = null;

        while (deviceIterator.hasNext()) {
            device = deviceIterator.next();
            int pid = device.getProductId();
            int vid = device.getVendorId();
            //your code
        }

        if (device == null)
            return false;


        if (!usbManager.hasPermission(device)) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            PendingIntent pendIntent = PendingIntent.getBroadcast(getBaseContext(), 0, intent, 0);
            usbManager.requestPermission(device, pendIntent);
        }
        if (!usbManager.hasPermission(device))
            return false;
        try {
            connection = usbManager.openDevice(device);
            UsbInterface iface = device.getInterface(0);

            epoint = iface.getEndpoint(1);
            connection.claimInterface(iface, true);
        } catch (Exception ex) {
            this.Status = DaisyConnectionStatus.NotAbleToConnect;
            return false;
        }
        this.Status = DaisyConnectionStatus.Connected;
        return true;
    }


    public void PrintString(String text) {
        List<Byte> alData = new ArrayList<Byte>();
        try {
            for (byte b : text.getBytes("cp1251"))
                alData.add(b);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        alData.add((byte) 13);

        byte[] data = new byte[alData.size()];
        for (int i = 0; i < alData.size(); i++)
            data[i] = alData.get(i);
        connection.bulkTransfer(epoint, data, data.length, 100);

    }

    public void PrintBoldString(String text) {
        byte[] data = new byte[]{(byte) 27,
                (byte)69,
                (byte)1};
        connection.bulkTransfer(epoint, data, data.length, 100);


        List<Byte> alData = new ArrayList<Byte>();
        try {
            for (byte b : text.getBytes("cp1251"))
                alData.add(b);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        alData.add((byte) 13);

        data = new byte[alData.size()];
        for (int i = 0; i < alData.size(); i++)
            data[i] = alData.get(i);
        connection.bulkTransfer(epoint, data, data.length, 100);
        data = new byte[]{(byte) 27,
                (byte)69,
                (byte)0};
        connection.bulkTransfer(epoint, data, data.length, 100);

    }

    public void PrintUnderLinedString(String text) {
        byte[] data = new byte[]{(byte) 27,
                (byte)45,
                (byte)50};
        connection.bulkTransfer(epoint, data, data.length, 100);


        List<Byte> alData = new ArrayList<Byte>();
        try {
            for (byte b : text.getBytes("cp1251"))
                alData.add(b);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        alData.add((byte) 13);

        data = new byte[alData.size()];
        for (int i = 0; i < alData.size(); i++)
            data[i] = alData.get(i);
        connection.bulkTransfer(epoint, data, data.length, 100);
        data = new byte[]{(byte) 27,
                (byte)45,
                (byte)48};
        connection.bulkTransfer(epoint, data, data.length, 100);

    }

    public void PrintEmptyLine() {

        byte[] data = new byte[]{(byte) 13};
        connection.bulkTransfer(epoint, data, data.length, 100);


    }

    public void Feed(Integer n) {
        byte lines = 0;
        lines = n.byteValue();
//        try {
//            //lines = n.toString().getBytes("cp1251")[0];
//
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        byte[] data = new byte[]{(byte) 27,
                (byte)74,
                lines};
        connection.bulkTransfer(epoint, data, data.length, 100);


    }

    public void CutPaperPartially() {
        byte lines = 0;

//        try {
//            //lines = n.toString().getBytes("cp1251")[0];
//
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        byte[] data = new byte[]{(byte) 29,
                (byte)86,
                (byte)1};
        connection.bulkTransfer(epoint, data, data.length, 100);
        data = new byte[]{(byte) 27,
                (byte)109};
        connection.bulkTransfer(epoint, data, data.length, 100);



    }


    public void Disconnect() {
        if (this.Status != DaisyConnectionStatus.Connected)
            return;
        connection.close();
        this.Status = DaisyConnectionStatus.Disconnected;
    }
//
//    HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
//    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
//    UsbDevice device =null;
//                while(deviceIterator.hasNext()){
//        device = deviceIterator.next();
//        int pid = device.getProductId();
//        int vid = device.getVendorId();
//        //your code
//    }
//
//                if (device==null)
//            return;
//
//    usbManager = (UsbManager) getBaseContext().getSystemService(Context.USB_SERVICE);
//                if (!usbManager.hasPermission(device)) {
//        Intent intent = new Intent(getBaseContext(), MainActivity.class);
//        PendingIntent pendIntent = PendingIntent.getBroadcast(getBaseContext(), 0, intent, 0);
//        usbManager.requestPermission(device, pendIntent);
//    }
//
//    UsbDeviceConnection connection = usbManager.openDevice(device);
//    UsbInterface iface = device.getInterface(0);
//
//    UsbEndpoint epoint = iface.getEndpoint(1);
//                connection.claimInterface(iface, true);
//    List<Byte> alData = new ArrayList<Byte>();
//    String text = "Яхмутуру";
//                try {
//        for (byte b : text.getBytes("cp1251"))
//            alData.add(b);
//    } catch (UnsupportedEncodingException e) {
//        e.printStackTrace();
//    }
//                alData.add((byte)13);
//
//    byte[] data = new byte[alData.size()];
//                for (int i = 0; i < alData.size(); i++)
//    data[i] = alData.get(i);
//    int transfered = connection.bulkTransfer(epoint, data, data.length, 100);
//                connection.close();

}
