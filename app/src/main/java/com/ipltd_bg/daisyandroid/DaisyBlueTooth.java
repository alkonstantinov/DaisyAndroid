package com.ipltd_bg.daisyandroid;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Created by alkon on 30-Mar-17.
 */

public class DaisyBlueTooth extends Service {

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
        DaisyBlueTooth getService() {
            // Return this instance of LocalService so clients can call public methods
            return DaisyBlueTooth.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    private BluetoothAdapter mBluetoothAdapter;

    //Брояч на операциите
    private int cntr = 0x1F;

    //Извлича следваща операция
    private int GetCntr() {
        cntr++;
        if (cntr >= 0xff - 0x20) {
            cntr = 0x20;
        }
        return cntr;
    }

    //Таймаут между запис и четене
    private int sleepBetweenWriteAndRead = 300;

    //Идентификатор на com през bluetooth
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;

    //Входящ поток
    private InputStream mmInStream = null;

    //Изходящ поток
    private OutputStream mmOutStream = null;


    //Конструктор
    public DaisyBlueTooth() {

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
        SetCounterToCorrectValue();
        return true;
    }

    //Изчислява CRC
    private byte[] CalcBcc(byte[] arr) {
        int sum = 0;
        for (int i = 1; i < arr.length - 5; i++)
            sum += arr[i] & 0xFF;
        String hex = Integer.toHexString(sum);
        while (hex.length() < 4)
            hex = "0" + hex;
        byte[] result = new byte[4];
        result[0] = (byte) Integer.parseInt("3" + hex.charAt(0), 16);
        result[1] = (byte) Integer.parseInt("3" + hex.charAt(1), 16);
        result[2] = (byte) Integer.parseInt("3" + hex.charAt(2), 16);
        result[3] = (byte) Integer.parseInt("3" + hex.charAt(3), 16);
        return result;
    }

    //Извлича отговор от буфер
    private byte[] GetResponse(byte[] resp, int bytesRead) {
        byte[] result = null;
        int idx = 0;
        while (idx < bytesRead) {
            if (resp[idx] == 0x15) {
                result = new byte[1];
                result[0] = 0x15;
                return result;
            }
            if (resp[idx] == 0x16) {
                idx++;
                if (result == null) {
                    result = new byte[1];
                    result[0] = 0x16;
                }
            }

            if (resp[idx] == 0x01) {
                int endIdx = idx + 1;
                while (resp[endIdx] != 0x03 && endIdx < bytesRead)
                    endIdx++;
                result = new byte[endIdx - idx + 1];
                for (int i = idx; i < endIdx + 1; i++)
                    result[i - idx] = resp[i];
                return result;
            }
        }
        return result;
    }

    //Изпраща команда на устройството и връща резултат
    private byte[] SendCommand(byte command, byte[] data) throws IOException, InterruptedException {
        synchronized (this) {
            int dataSize = (data == null ? 0 : data.length);
            int cntr = GetCntr();
            int sum = 0;
            byte[] cmd = new byte[10 + dataSize];
            cmd[0] = 0x01;
            cmd[1] = (byte) (0x20 + 4 + dataSize);
            cmd[2] = (byte) cntr;
            cmd[3] = command;
            for (int i = 0; i < dataSize; i++) {
                cmd[4 + i] = data[i];
                sum += data[i];
            }

            cmd[4 + dataSize] = 0x05;
            byte[] crc = CalcBcc(cmd);
            cmd[5 + dataSize] = crc[0];
            cmd[6 + dataSize] = crc[1];
            cmd[7 + dataSize] = crc[2];
            cmd[8 + dataSize] = crc[3];
            cmd[9 + dataSize] = 0x03;

            mmOutStream.write(cmd);
            Thread.sleep(sleepBetweenWriteAndRead);
            byte[] resp = new byte[4096];
            int bytesRead = mmInStream.read(resp);
            byte[] result = GetResponse(resp, bytesRead);
            while (result == null || result[0] == 0x16) {
                Thread.sleep(sleepBetweenWriteAndRead);
                bytesRead = mmInStream.read(resp);
                result = GetResponse(resp, bytesRead);
            }


            return result;
        }
    }

    //Отпечатва статус на устройството
    public byte[] PrintStatus() {
        if (this.Status != DaisyConnectionStatus.Connected)
            return null;
        try {
            return SendCommand((byte) 0x47, null);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;

    }

    //Увеличава брояча на операциите до валиден нмер
    private void SetCounterToCorrectValue() {
        if (this.Status != DaisyConnectionStatus.Connected)
            return;
        byte[] result = null;
        do {


            try {
                result = SendCommand((byte) 0x4A, null);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (result == null || result[0] == 0x15);

    }

    //Връща масив от списък
    private byte[] GetAsArray(List<Byte> lst) {

        byte[] data = new byte[lst.size()];
        for (int i = 0; i < lst.size(); i++)
            data[i] = lst.get(i);
        return data;
    }

    //Записва данни за оператор
    public boolean SetOperatorData(OperatorData opData) {
        if (this.Status != DaisyConnectionStatus.Connected)
            return false;
        List<Byte> alData = new ArrayList<Byte>();
        for (byte b : opData.getOperatorNumber().getBytes(StandardCharsets.US_ASCII))
            alData.add(b);

        alData.add((byte) ',');
        for (byte b : opData.getPassword().getBytes(StandardCharsets.US_ASCII))
            alData.add(b);
        alData.add((byte) ',');
        for (byte b : opData.getOperatorName().getBytes(StandardCharsets.US_ASCII))
            alData.add(b);

        byte[] data = GetAsArray(alData);
        byte[] result = null;
        try {
            result = SendCommand((byte) 0x66, data);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        if (result != null) {
            SetLastError(result);
            this.Status = DaisyConnectionStatus.Connected;
        }

        return result != null;
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

    //Добавя артикул
    public boolean AddArticle(ArticleData articleData) throws UnsupportedEncodingException {
        if (this.Status != DaisyConnectionStatus.Connected)
            return false;
        byte[] result = null;
        List<Byte> alData = new ArrayList<Byte>();
        alData.add((byte) 'P');//!!!!!!!!!!!!!!!!!!!!!!!!!!

        alData.add((articleData.getTaxGroup() + "").getBytes("cp1251")[0]);
        for (byte b : Integer.toString(articleData.getPLUNum()).getBytes("cp1251"))
            alData.add(b);
        alData.add((byte) ',');
        for (byte b : Double.toString(articleData.getPrice()).getBytes("cp1251"))
            alData.add(b);
        alData.add((byte) ',');
        for (byte b : articleData.getName().getBytes("cp1251"))
            alData.add(b);
        alData.add((byte) 0x0A);
        for (byte b : articleData.getBarcode().getBytes("cp1251"))
            alData.add(b);
        alData.add((byte) ',');
        for (byte b : Integer.toString(articleData.getDept()).getBytes("cp1251"))
            alData.add(b);
        alData.add((byte) ',');
        alData.add((byte) '1');
        alData.add((byte) ',');
        for (byte b : Integer.toString(articleData.getStockQty()).getBytes("cp1251"))
            alData.add(b);

        byte[] data = GetAsArray(alData);
        try {
            result = SendCommand((byte) 0x6B, data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (result != null) {
            SetLastError(result);
            this.Status = DaisyConnectionStatus.Connected;
        }

        return result != null && result.length > 1 && result[4] == 80;

    }

    //Премахва артикул
    public boolean RemoveArticle(int pluNum) throws UnsupportedEncodingException {
        if (this.Status != DaisyConnectionStatus.Connected)
            return false;
        byte[] result = null;
        List<Byte> alData = new ArrayList<Byte>();
        alData.add((byte) 'D');//!!!!!!!!!!!!!!!!!!!!!!!!!!

        for (byte b : Integer.toString(pluNum).getBytes("cp1251"))
            alData.add(b);

        byte[] data = GetAsArray(alData);
        try {
            result = SendCommand((byte) 0x6B, data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (result != null) {
            SetLastError(result);
            this.Status = DaisyConnectionStatus.Connected;
        }

        return result != null;

    }


    //Отказва касова бележка
    public boolean CancelFisk() {
        if (this.Status != DaisyConnectionStatus.InFisk)
            return false;
        byte[] result = null;
        try {
            result = SendCommand((byte) 0x82, null);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (result != null) {
            SetLastError(result);
            this.Status = DaisyConnectionStatus.Connected;
        }
        return result != null;
    }

    //Започва касова бележка
    public boolean StartSellingFisk(StartFiskData sfd) throws UnsupportedEncodingException {
        if (this.Status != DaisyConnectionStatus.Connected)
            return false;
        byte[] result = null;
        List<Byte> alData = new ArrayList<Byte>();

        for (byte b : Integer.toString(sfd.getClerkNum()).getBytes("cp1251"))
            alData.add(b);
        alData.add((byte) ',');
        for (byte b : sfd.getPassword().getBytes("cp1251"))
            alData.add(b);
        if (sfd.getInvoice()) {
            alData.add((byte) ',');
            alData.add((byte) '1');
            alData.add((byte) ',');
            alData.add((byte) 'I');
        }
        byte[] data = GetAsArray(alData);

        try {
            result = SendCommand((byte) 0x30, data);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (result != null) {
            SetLastError(result);
            this.Status = DaisyConnectionStatus.InFisk;
        }
        return result != null;
    }

    //Добавя свободна продажба в касова бележка
    public boolean AddFreeSale(FreeSaleData freeSaleData) throws UnsupportedEncodingException {
        if (this.Status != DaisyConnectionStatus.InFisk)
            return false;
        byte[] result = null;
        List<Byte> alData = new ArrayList<Byte>();

        for (byte b : freeSaleData.getText1().getBytes("cp1251"))
            alData.add(b);
        if (freeSaleData.getText2() != null) {
            alData.add((byte) 0x0A);
            for (byte b : freeSaleData.getText2().getBytes("cp1251"))
                alData.add(b);
        }
        alData.add((byte) 0x09);
        alData.add((freeSaleData.getTaxGr() + "").getBytes("cp1251")[0]);
        if (freeSaleData.getSign() == '-')
            alData.add((byte) '-');
        for (byte b : Double.toString(freeSaleData.getPrice()).getBytes("cp1251"))
            alData.add(b);
        if (freeSaleData.getQTY() != 0) {
            alData.add((byte) '*');
            for (byte b : Double.toString(freeSaleData.getQTY()).getBytes("cp1251"))
                alData.add(b);
        }
        if (freeSaleData.getNetto() != 0 || freeSaleData.getPercent() != 0) {
            alData.add((byte) ',');
            if (freeSaleData.getNetto() != 0) {
                for (byte b : ("$" + Double.toString(freeSaleData.getNetto())).getBytes("cp1251"))
                    alData.add(b);
            } else {
                for (byte b : Double.toString(freeSaleData.getPercent()).getBytes("cp1251"))
                    alData.add(b);
                alData.add((byte) '%');
            }
        }


        byte[] data = GetAsArray(alData);
        try {
            result = SendCommand((byte) 0x31, data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (result != null) {
            SetLastError(result);
        }

        return result != null;

    }

    //Добавя артикул в касова бележка
    public boolean AddArticleSale(ArticleSaleData articleSaleData) throws UnsupportedEncodingException {
        if (this.Status != DaisyConnectionStatus.InFisk)
            return false;
        byte[] result = null;
        List<Byte> alData = new ArrayList<Byte>();
        if (articleSaleData.getSign() == '-')
            alData.add((byte) '-');
        for (byte b : Integer.toString(articleSaleData.getPLU()).getBytes("cp1251"))
            alData.add(b);
        alData.add((byte) '*');
        for (byte b : Double.toString(articleSaleData.getQTY()).getBytes("cp1251"))
            alData.add(b);
        if (articleSaleData.getPercent() != 0) {
            alData.add((byte) ',');
            for (byte b : Double.toString(articleSaleData.getPercent()).getBytes("cp1251"))
                alData.add(b);

        }
        if (articleSaleData.getPrice() != 0) {
            alData.add((byte) '@');
            for (byte b : Double.toString(articleSaleData.getPrice()).getBytes("cp1251"))
                alData.add(b);

        }
        if (articleSaleData.getNetto() != 0) {
            alData.add((byte) '$');
            for (byte b : Double.toString(articleSaleData.getNetto()).getBytes("cp1251"))
                alData.add(b);

        }


        byte[] data = GetAsArray(alData);
        try {
            result = SendCommand((byte) 0x3A, data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (result != null) {
            SetLastError(result);
        }

        return result != null;

    }


    //Отпечатва тотал в касова бележка
    public boolean TotalFisk(TotalFiskData tfd) throws UnsupportedEncodingException {
        if (this.Status != DaisyConnectionStatus.InFisk)
            return false;
        byte[] result = null;
        List<Byte> alData = new ArrayList<Byte>();

        for (byte b : tfd.getText1().getBytes("cp1251"))
            alData.add(b);
        if (tfd.getText2() != null) {
            alData.add((byte) 0x0A);
            for (byte b : tfd.getText2().getBytes("cp1251"))
                alData.add(b);
        }
        alData.add((byte) 0x09);
        alData.add((tfd.getPayment() + "").getBytes("cp1251")[0]);
        for (byte b : Double.toString(tfd.getAmmount()).getBytes("cp1251"))
            alData.add(b);


        byte[] data = GetAsArray(alData);
        try {
            result = SendCommand((byte) 0x35, data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (result != null) {
            SetLastError(result);
        }
        return result != null;

    }

    //Прелратява касова бележка
    public boolean EndSellingFisk() {
        if (this.Status != DaisyConnectionStatus.InFisk)
            return false;

        byte[] result = null;
        try {
            result = SendCommand((byte) 0x38, null);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (result != null) {
            SetLastError(result);
            this.Status = DaisyConnectionStatus.Connected;
        }
        return result != null;
    }

    public boolean PrintCopy() {
        if (this.Status != DaisyConnectionStatus.Connected)
            return false;
        this.Status = DaisyConnectionStatus.InFisk;
        byte[] result = null;


        byte[] data = new byte[]{(byte) '1'};
        try {
            result = SendCommand((byte) 0x6D, data);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (result != null) {
            SetLastError(result);
            this.Status = DaisyConnectionStatus.Connected;
        }
        return result != null;
    }


    //Дневен отчет
    public boolean DailyReport(DailyReportData drd) throws UnsupportedEncodingException {
        if (this.Status != DaisyConnectionStatus.Connected)
            return false;

        byte[] result = null;
        byte[] data = new byte[2];
        if (drd.getOption()) {
            data[0] = Integer.toString(drd.getItem()).getBytes("cp1251")[0];
            data[1] = (byte) 'N';
        } else data = null;
        try {
            result = SendCommand((byte) 0x45, data);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (result != null)
            SetLastError(result);

        return result != null;
    }

    //Разширен дневен отчет
    public boolean DailyExReport(DailyReportData drd) throws UnsupportedEncodingException {
        if (this.Status != DaisyConnectionStatus.Connected)
            return false;

        byte[] result = null;
        byte[] data = new byte[1];
        data[0] = Integer.toString(drd.getItem()).getBytes("cp1251")[0];
        try {
            result = SendCommand((byte) 0x6C, data);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (result != null)
            SetLastError(result);
        return result != null;
    }

    //Установява последна грешка или НЯМА ГРЕШКА
    private void SetLastError(byte[] response) {
        int errNo = 0;
        for (int i = 0; i < response.length; i++) {
            if (response[i] == 0x04) {
                errNo = response[i + 4] & 0xFF - 0x80;
                break;
            }
        }

        switch (errNo) {
            case 0:
                this.LastStatusError = "Няма грешка";
                break;
            case 1:
                this.LastStatusError = "Въпросната операция ще доведе до препълване";
                break;
            case 3:
                this.LastStatusError = "Нямате право на повече продажби в този бон";
                break;
            case 4:
                this.LastStatusError = "Нямате право на повече плащания в този бон";
                break;
            case 5:
                this.LastStatusError = "Опит за извършване на нулева транзакция";
                break;
            case 6:
                this.LastStatusError = "Опит за извършване на продажба, след като е започнатоплащане";
                break;
            case 7:
                this.LastStatusError = "Нямате право на такава операция";
                break;
            case 8:
                this.LastStatusError = "Забранена за продажби дан. група";
                break;
            case 9:
                this.LastStatusError = "Опит за издаване на разширена клиентска бележка (фактура) без въведен диапазон";
                break;
            case 11:
                this.LastStatusError = "Въвеждане на повече от една десетична точка";
                break;
            case 12:
                this.LastStatusError = "Въвеждане на повече от един символ '+' или '-' *";
                break;
            case 13:
                this.LastStatusError = "Символът '+' или '-' не е в първа позиция *";
                break;
            case 14:
                this.LastStatusError = "Недопустим символ, напр. баркод, който съдържа не само цифри";
                break;
            case 15:
                this.LastStatusError = "Повече от допустимия брой знаци след десетичната точка";
                break;
            case 16:
                this.LastStatusError = "Въведени са повече от разрешения брой символи";
                break;
            case 20:
                this.LastStatusError = "В дадената ситуация не се обслужва натиснатия от Вас клавиш";
                break;
            case 21:
                this.LastStatusError = "Стойността е извън допустимите граници";
                break;
            case 22:
                this.LastStatusError = "Виж системен параметър 10";
                break;
            case 23:
                this.LastStatusError = "Опит за \"дълбок\" войд след отстъпка/надбавка в/у междинна сума";
                break;
            case 24:
                this.LastStatusError = "Опит за \"дълбок\" войд на несъществуваща транзакция";
                break;
            case 25:
                this.LastStatusError = "Опит за извършване на плащане, без да има продажби";
                break;
            case 26:
                this.LastStatusError = "Опит за продажба на артикул с количество, надвишаващо запаса му";
                break;
            case 27:
                this.LastStatusError = "Некоректна комуникация с ел. везна";
                break;
            case 41:
                this.LastStatusError = "Некоректен баркод (грешна контролна сума)";
                break;
            case 42:
                this.LastStatusError = "Опит за продажба с нулев баркод";
                break;
            case 43:
                this.LastStatusError = "Опит за програмиране с тегловен баркод";
                break;
            case 44:
                this.LastStatusError = "Опит за продажба с непрограмиран баркод";
                break;
            case 45:
                this.LastStatusError = "Опит за програмиране на вече съществуващ баркод";
                break;
            case 61:
                this.LastStatusError = "Опит за работа с КЛЕН, неотговарящ на изискванията **";
                break;
            case 66:
                this.LastStatusError = "Некоректна парола *";
                break;
            case 70:
                this.LastStatusError = "!!! Не е открита ФП !!!! **";
                break;
            case 71:
                this.LastStatusError = "!!! Некоректни данни във ФП !!!! **";
                break;
            case 72:
                this.LastStatusError = "!!! Грешка при запис във ФП !!!! **";
                break;
            case 76:
                this.LastStatusError = "Необходима е информация от сървъра на НАП **";
                break;
            case 90:
                this.LastStatusError = "Не е нулиран периодичният отчет";
                break;
            case 91:
                this.LastStatusError = "Не е нулиран дневният финансов отчет";
                break;
            case 92:
                this.LastStatusError = "Не е нулиран отчетът по оператори";
                break;
            case 93:
                this.LastStatusError = "Не е нулиран отчетът по артикули";
                break;
            case 97:
                this.LastStatusError = "Не може да се препрограмира това поле";
                break;
            case 81:
                this.LastStatusError = "Дневният финансов отчет е препълнен";
                break;
            case 83:
                this.LastStatusError = "Отчетът по оператори е препълнен";
                break;
            case 84:
                this.LastStatusError = "Отчетът по артикули е препълнен";
                break;
            case 85:
                this.LastStatusError = "Периодичният отчет е препълнен";
                break;
            case 102:
                this.LastStatusError = "Няма комуникация между ФУ и модема **";
                break;
            case 107:
                this.LastStatusError = "SIM картата н амодема е заключена. **";
                break;
            case 108:
                this.LastStatusError = "3 поредни опита за въвеждане на грешна парола. ***";
                break;
            case 110:
                this.LastStatusError = "Подменена SIM карта **";
                break;
            case 111:
                this.LastStatusError = "Грешка при комуникация между ДТ и сървъра на НАП **";
                break;
            case 113:
                this.LastStatusError = "Сървърът на НАП не приема подадените му данни **";
                break;
            case 117:
                this.LastStatusError = "Неуспешен опит за регистрация на модема в мрежата на мобилния оператор";
                break;
            case 118:
                this.LastStatusError = "Операцията е забранена";
                break;
            case 120:
                this.LastStatusError = "Невъведена стойност в задължително поле";
                break;
            case 124:
                this.LastStatusError = "ФУ не разпознава поставения КЛЕН (КЛЕН-ът е активиран на друго ФУ) **";
                break;
            case 125:
                this.LastStatusError = "Ако ФУ е регистирано, то не може да работи без КЛЕН **";
                break;
            case 126:
                this.LastStatusError = "КЛЕН е близо до препълване или препълнен. Трябва да се замени. **        ";
                break;
            default:
                this.LastStatusError = "Няма грешка";
                break;
        }
    }


    //Започва нефискален бон
    public boolean StartNoFisk() throws UnsupportedEncodingException {
        if (this.Status != DaisyConnectionStatus.Connected)
            return false;
        byte[] result = null;

        try {
            result = SendCommand((byte) 0x26, null);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (result != null) {
            SetLastError(result);
            this.Status = DaisyConnectionStatus.InNoFisk;
        }
        return result != null;
    }


    //Започва нефискален бон
    public boolean EndNoFisk() throws UnsupportedEncodingException {
        if (this.Status != DaisyConnectionStatus.InNoFisk)
            return false;
        byte[] result = null;

        try {
            result = SendCommand((byte) 0x27, null);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (result != null) {
            SetLastError(result);
            this.Status = DaisyConnectionStatus.Connected;
        }
        return result != null;
    }

    //Печата ред в нефискален бон
    public boolean PrintNoFisk(String text) throws UnsupportedEncodingException {
        if (this.Status != DaisyConnectionStatus.InNoFisk)
            return false;
        byte[] result = null;
        List<Byte> alData = new ArrayList<Byte>();

        for (byte b : text.getBytes("cp1251"))
            alData.add(b);

        byte[] data = GetAsArray(alData);

        try {
            result = SendCommand((byte) 0x2A, data);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (result != null) {
            SetLastError(result);
        }
        return result != null;
    }

    //Променя име и адрес на фирма
    public boolean SetCompanyData(CompanyData cd) throws UnsupportedEncodingException {
        if (this.Status != DaisyConnectionStatus.Connected)
            return false;
        byte[] result = null;
        List<Byte> alData = new ArrayList<Byte>();
        alData.add((byte) '0');
        for (byte b : cd.getName().getBytes("cp1251"))
            alData.add(b);

        byte[] data = GetAsArray(alData);

        try {
            result = SendCommand((byte) 0x2B, data);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        alData = new ArrayList<Byte>();
        alData.add((byte) '1');
        for (byte b : cd.getAddress().getBytes("cp1251"))
            alData.add(b);

        if (result != null) {
            SetLastError(result);
        }
        return result != null;
    }


}
