package com.ipltd_bg.daisyandroid;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ipltd_bg.daisyandroid.BoundService.Data.ArticleData;
import com.ipltd_bg.daisyandroid.BoundService.Data.ArticleSaleData;
import com.ipltd_bg.daisyandroid.BoundService.Data.CompanyData;
import com.ipltd_bg.daisyandroid.BoundService.Data.DailyReportData;
import com.ipltd_bg.daisyandroid.BoundService.Data.FreeSaleData;
import com.ipltd_bg.daisyandroid.BoundService.Data.OperatorData;
import com.ipltd_bg.daisyandroid.BoundService.Data.StartFiskData;
import com.ipltd_bg.daisyandroid.BoundService.Data.TotalFiskData;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    DaisyBlueTooth mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button bConnect = (Button) findViewById(R.id.bConnect);
        bConnect.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                if (!mService.Connect())
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
                sfd.setClerkNum(3);
                sfd.setPassword("3");
                sfd.setInvoice(false);
                FreeSaleData fd = new FreeSaleData();
                fd.setPrice(30.20);
                fd.setNetto(0);
                fd.setPercent(0);
                fd.setQTY(12);
                fd.setSign('+');
                fd.setTaxGr('Б');
                fd.setText1("Чушки");
                fd.setText2("Белени");

                TotalFiskData tfd = new TotalFiskData();
                tfd.setText1("Text1");
                tfd.setText2("Text2");
                tfd.setAmmount(903.22);

                tfd.setPayment('P');

                ArticleSaleData asd = new ArticleSaleData();
                asd.setQTY(1);
                asd.setPercent(0);
                asd.setNetto(0);
                asd.setPLU(1);
                asd.setPrice(0);
                asd.setSign('+');


                try {
                    mService.StartSellingFisk(sfd);

                    mService.AddFreeSale(fd);
                    mService.AddArticleSale(asd);
                    mService.TotalFisk(tfd);
                    //dbt.CancelFisk();
                    mService.EndSellingFisk();
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
                    mService.DailyReport(drd);
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


    }

    @Override
    protected void onStart() {
        super.onStart();

        // Bind to LocalService
        Intent intent = new Intent(this, DaisyBlueTooth.class);
        //startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
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
}