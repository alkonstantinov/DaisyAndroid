package com.ipltd_bg.daisyandroid;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ipltd_bg.daisyandroid.BoundService.Data.ArticleData;
import com.ipltd_bg.daisyandroid.BoundService.Data.ArticleSaleData;
import com.ipltd_bg.daisyandroid.BoundService.Data.CompanyData;
import com.ipltd_bg.daisyandroid.BoundService.Data.DailyReportData;
import com.ipltd_bg.daisyandroid.BoundService.Data.FontInfo;
import com.ipltd_bg.daisyandroid.BoundService.Data.FreeSaleData;
import com.ipltd_bg.daisyandroid.BoundService.Data.OperatorData;
import com.ipltd_bg.daisyandroid.BoundService.Data.StartFiskData;
import com.ipltd_bg.daisyandroid.BoundService.Data.TotalFiskData;
import com.ipltd_bg.daisyandroid.Classes.AddRemoveSums;
import com.ipltd_bg.daisyandroid.Enums.ParagraphAlignment;

import java.io.Console;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static android.R.id.message;

public class MainActivity extends AppCompatActivity {

    DaisyBlueTooth mService;
    DaisyBarcodeBlueTooth mBarcodeService;

    DatecsNFPBlueTooth mDNFPService;
    DaisyUsb mUsbService;
    boolean mBound = false;
    boolean mUsbBound = false;
    boolean mBarcodeBound = false;
    UsbManager usbManager;
    String barcodeRead = "";


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.getDevice().getName().equals("General Bluetooth HID Barcode Scanner")) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_TAB:
                    Toast.makeText(this, barcodeRead, Toast.LENGTH_SHORT).show();
                    barcodeRead = "";
                    break;
                default:
                    barcodeRead += event.getNumber();
                    break;


            }

            return true;
        } else
            return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button bConnect = (Button) findViewById(R.id.bConnect);
        bConnect.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                if (!mService.Connect("ECR00000000"))
                    Toast.makeText(v.getContext(), "cannot connect",
                            Toast.LENGTH_SHORT).show();

            }

        });

        final Button bSend = (Button) findViewById(R.id.bSend);
        bSend.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                byte[] arr = mService.PrintStatus();

            }

        });


        final Button bGetStatus = (Button) findViewById(R.id.bGetStatus);
        bGetStatus.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                OperatorData opData = new OperatorData();
                opData.setOperatorNumber("3");
                opData.setPassword("3");
                opData.setOperatorName("Sasho");

                mService.SetOperatorData(opData);

            }

        });

        final Button bAddArticle = (Button) findViewById(R.id.bAddArticle);
        bAddArticle.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                ArticleData ad = new ArticleData();
                ad.setBarcode("1234567890128");
                ad.setDept(1);
                ad.setName("АПушка2");
                ad.setPLUNum(1);
                ad.setPrice(2.62);
                ad.setStockQty(260);
                ad.setTaxGroup('Б');

                try {
                    mService.AddArticle(ad);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

        });


        final Button bAddArticle2 = (Button) findViewById(R.id.bAddArticle2);
        bAddArticle2.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                ArticleData ad = new ArticleData();
                ad.setBarcode("1234567890135");
                ad.setDept(1);
                ad.setName("Друг артикул");
                ad.setPLUNum(2);
                ad.setPrice(2.62);
                ad.setStockQty(260);
                ad.setTaxGroup('Б');

                try {
                    mService.AddArticle(ad);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

        });

        final Button bDelArticle2 = (Button) findViewById(R.id.bDelArticle2);
        bDelArticle2.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {


                try {
                    mService.RemoveArticle(2);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

        });

        final Button bNoFiskSale = (Button) findViewById(R.id.bNoFiskSale);
        bNoFiskSale.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                StartFiskData sfd = new StartFiskData();
                sfd.setClerkNum(20);
                sfd.setPassword("9999");
                sfd.setMode(0);
                sfd.setReason(0);
                sfd.setDocLink("0000026");
                sfd.setDocDT("16-04-19 14:35");
                sfd.setFiskMem("36637547");


                sfd.setUniqueSellingNumber("DY479511-OP01-0000002");
                FreeSaleData fd = new FreeSaleData();
                fd.setPrice(28.00);
                fd.setNetto(0);
                fd.setPercent(0);
                fd.setQTY(1);
                fd.setSign('+');
                fd.setTaxGr('Б');
                fd.setText1("Чушки");
                fd.setText2("Белени");

                TotalFiskData tfd = new TotalFiskData();
                tfd.setText1("Text1");
                tfd.setText2("Text2");
                tfd.setAmmount(28.00);

                tfd.setPayment('P');
                //P - в брой
                //C - кредит
                //

//                ArticleSaleData asd = new ArticleSaleData();
//                asd.setQTY(1);
//                asd.setPercent(0);
//                asd.setNetto(0);
//                asd.setPLU(1);
//                asd.setPrice(0);
//                asd.setSign('+');


                try {
                    mService.StartSellingFisk(sfd);

                    mService.AddFreeSale(fd);
                    //mService.AddArticleSale(asd);
                    mService.TotalFisk(tfd);
                    //dbt.CancelFisk();
                    mService.EndSellingFisk();
                    mService.CutPaper();
                    mService.Disconnect();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

        });


        final Button bCopySale = (Button) findViewById(R.id.bCopySale);
        bCopySale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.PrintCopy();
                mService.Disconnect();

            }
        });

        final Button bDaily = (Button) findViewById(R.id.bDaily);
        bDaily.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                DailyReportData drd = new DailyReportData();
                drd.setItem(3);
                drd.setOption(true);


                try {
                    //mService.DailyReport(drd);
                    mService.DailyExReport(drd);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

        });


        final Button bNoFisk = (Button) findViewById(R.id.bNoFisk);
        bNoFisk.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {


                try {
                    mService.StartNoFisk();
                    mService.PrintNoFisk("Маса три");
                    mService.PrintNoFisk("------------");
                    mService.PrintNoFisk("чушки");


                    mService.EndNoFisk();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

        });

        final Button bSetCompanyData = (Button) findViewById(R.id.bSetCompanyData);
        bSetCompanyData.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                CompanyData cd = new CompanyData();
                cd.setAddress("ЬЬЬЬ 51");
                cd.setName("дЕЙВЯЕВЕВЕЯВЗО9О");

                try {
                    mService.SetCompanyData(cd);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

        });

        final Button bConnectNFP = (Button) findViewById(R.id.bConnectNFP);
        bConnectNFP.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
                if (!mUsbService.Connect(manager))
                    Toast.makeText(v.getContext(), "cannot connect",
                            Toast.LENGTH_SHORT).show();

            }

        });

        final Button bPrintNFP = (Button) findViewById(R.id.bPrintNFP);
        bPrintNFP.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mUsbService.PrintString("един ред дейба");
                mUsbService.PrintEmptyLine();
                mUsbService.PrintBoldString("Важен ред дейба");
                mUsbService.PrintUnderLinedString("Важен ред дейба");

                mUsbService.Feed(150);
                mUsbService.CutPaperPartially();

            }

        });
        final Button bDiscNFP = (Button) findViewById(R.id.bDiscNFP);
        bDiscNFP.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mUsbService.Disconnect();


            }

        });


        Button bConnectDatecsNFP = (Button) findViewById(R.id.bConnectDatecsNFP);
        bConnectDatecsNFP.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                if (!mDNFPService.Connect("DPP-450"))
                    Toast.makeText(v.getContext(), "cannot connect",
                            Toast.LENGTH_SHORT).show();

            }

        });
        Button bPrintDatecsNFP = (Button) findViewById(R.id.bPrintDatecsNFP);
        bPrintDatecsNFP.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                FontInfo info = new FontInfo();
                info.setBig(true);
                info.setUnderline(true);
                info.setBold(true);
                mDNFPService.SetFont(info);
                mDNFPService.SetAlignment(ParagraphAlignment.Center);
                mDNFPService.PrintLine("Центрирано подчертано болд голямо");

                info.setBig(false);
                info.setUnderline(false);
                info.setBold(true);
                mDNFPService.SetFont(info);
                mDNFPService.SetAlignment(ParagraphAlignment.Right);
                mDNFPService.PrintLine("Дясно болд");
                info.setBig(false);
                info.setUnderline(false);
                info.setBold(false);
                mDNFPService.SetFont(info);
                mDNFPService.SetAlignment(ParagraphAlignment.Left);
                mDNFPService.PrintLine("Лявно малко нормално");

                mDNFPService.PrintNewLine();
                mDNFPService.PrintNewLine();
                mDNFPService.PrintFeed((byte) 200);

            }

        });
        Button bDiscDatecsNFP = (Button) findViewById(R.id.bDiscDatecsNFP);
        bDiscDatecsNFP.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                mDNFPService.Disconnect();

            }

        });

        Button bBarcodeConnect = (Button) findViewById(R.id.bBarcodeConnect);
        bBarcodeConnect.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                if (!mBarcodeService.Connect("General Bluetooth HID Barcode Scanner"))
                    Toast.makeText(v.getContext(), "cannot connect",
                            Toast.LENGTH_SHORT).show();

            }

        });
        Button bReading = (Button) findViewById(R.id.bReading);
        bReading.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mBarcodeService.Reading();
            }

        });


        Button bDiagnosticInfo = (Button) findViewById(R.id.bDiagnosticInfo);
        bDiagnosticInfo.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                mService.GetDiagnosticData();

            }

        });


        Button bCancelFisk = (Button) findViewById(R.id.bCancelFisk);
        bCancelFisk.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                mService.CancelFisk();

            }

        });


        Button bGetLastFisk = (Button) findViewById(R.id.bGetLastFisk);
        bGetLastFisk.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                String s = mService.GetLastFisk();
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();

            }

        });


        Button bPrintTest = (Button) findViewById(R.id.bPrintTest);
        bPrintTest.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                //mService.mo

            }

        });

        Button bAddRemoveSum = (Button) findViewById(R.id.bAddRemoveSum);
        bAddRemoveSum.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                AddRemoveSums ars= new AddRemoveSums();
                ars.setSum(-50);
                ars.setText1("text 1");
                ars.setText2("text 2");

                try {
                    mService.AddRemoveSum(ars);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        // Bind to LocalService
        Intent intent = new Intent(this, DaisyBlueTooth.class);
        //startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        intent = new Intent(this, DaisyUsb.class);
        bindService(intent, mUsbConnection, Context.BIND_AUTO_CREATE);

        intent = new Intent(this, DatecsNFPBlueTooth.class);
        bindService(intent, mDatecsNFPConnection, Context.BIND_AUTO_CREATE);

        intent = new Intent(this, DaisyBarcodeBlueTooth.class);
        bindService(intent, mBarcodeConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        if (mUsbBound) {
            unbindService(mUsbConnection);
            mUsbBound = false;
        }
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            DaisyBlueTooth.LocalBinder binder = (DaisyBlueTooth.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private ServiceConnection mDatecsNFPConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            DatecsNFPBlueTooth.LocalBinder binder = (DatecsNFPBlueTooth.LocalBinder) service;
            mDNFPService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private ServiceConnection mUsbConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            DaisyUsb.LocalBinder binder = (DaisyUsb.LocalBinder) service;
            mUsbService = binder.getService();
            mUsbBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mUsbBound = false;
        }
    };

    private ServiceConnection mBarcodeConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            DaisyBarcodeBlueTooth.LocalBinder binder = (DaisyBarcodeBlueTooth.LocalBinder) service;
            mBarcodeService = binder.getService();
            mBarcodeBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }


    };


}

