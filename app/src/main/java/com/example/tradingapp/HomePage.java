package com.example.tradingapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;


public class HomePage extends AppCompatActivity {
    private Intent intent;
    private String user, password;
    private String[] log_httpCllient;
    private TextView txv_bottomLog;
    private TextView[] txv_Home_Main_Price;
    private TextView[] txv_Home_Main_Change;
    private int[] txv_Home_Main_Price_i;
    private int[] txv_Home_Main_Change_i;
    private Thread UiMain;

    private ToggleButton btn_bottomtest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        initialization();

        iniThread();
    }

    private void iniThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    for (int i = 0; i < 3; i++) {
                        sendPOST(i);
                        try {
                            Thread.sleep(100+(int)(Math.random()*500));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void initialization() {
        txv_bottomLog = findViewById(R.id.txv_bottomLog);
        intent = getIntent();
        user = intent.getStringExtra("user");
        password = intent.getStringExtra("password");

        log_httpCllient = new String[]{"", "", ""};

        txv_Home_Main_Price = new TextView[3];
        txv_Home_Main_Change = new TextView[3];
        txv_Home_Main_Price_i = new int[]{R.id.txv_Home_Main1_Price, R.id.txv_Home_Main2_Price, R.id.txv_Home_Main3_Price};
        txv_Home_Main_Change_i= new int[]{R.id.txv_Home_Main1_Change, R.id.txv_Home_Main2_Change, R.id.txv_Home_Main3_Change};
        for  (int i = 0; i<3; i++)
        {
            txv_Home_Main_Price[i]=findViewById(txv_Home_Main_Price_i[i]);
            txv_Home_Main_Change[i]=findViewById(txv_Home_Main_Change_i[i]);
        }
    }


    private void updateHomeMain(int i){
        int start = log_httpCllient[i].indexOf("\"125\":");
        start = log_httpCllient[i].indexOf(":", start)+1;
        int end = log_httpCllient[i].indexOf(",", start);
        float price = Float.parseFloat(log_httpCllient[i].substring(start, end));
        txv_Home_Main_Price[i].setText(String.format("%.02f", price));

        start = log_httpCllient[i].indexOf("\"185\":");
        start = log_httpCllient[i].indexOf(":", start)+1;
        end = log_httpCllient[i].indexOf(",", start);
        float change = Float.parseFloat(log_httpCllient[i].substring(start, end));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (change>=0){
                    txv_Home_Main_Change[i].setBackgroundResource(R.drawable.background_grainer);
                }
                else{
                    txv_Home_Main_Change[i].setBackgroundResource(R.drawable.background_loser);
                }
                txv_Home_Main_Change[i].setText(String.format("%.02f", change)+"%");
            }
        });
    }

    public void testBtn(View view) {
        for (int i = 0; i < 3; i++) {
            sendPOST(i);
        }
    }

    private void sendPOST(int i) {
        OkHttpClient client = new OkHttpClient();

        String[] url = new String[]{
                "https://tw.quote.finance.yahoo.net/quote/q?type=tick&perd=1m&mkt=10&sym=%23001",
                "https://tw.quote.finance.yahoo.net/quote/q?type=tick&perd=1m&mkt=10&sym=%23026",
                "https://tw.screener.finance.yahoo.net/future/q?type=tick&perd=1m&mkt=01&sym=WTX%26"
        };

        Request request = new Request.Builder()
                .url(url[i])
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();
                    HomePage.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int start = myResponse.indexOf("\"125\":");
                            start = myResponse.indexOf(":", start)+1;
                            int end = myResponse.indexOf(",", start);
                            float price = Float.parseFloat(myResponse.substring(start, end));
                            txv_Home_Main_Price[i].setText(String.format("%.02f", price));

                            start = myResponse.indexOf("\"185\":");
                            start = myResponse.indexOf(":", start)+1;
                            end = myResponse.indexOf(",", start);
                            float change = Float.parseFloat(myResponse.substring(start, end));
                            if (change>=0){
                                txv_Home_Main_Change[i].setBackgroundResource(R.drawable.background_grainer);
                            }
                            else{
                                txv_Home_Main_Change[i].setBackgroundResource(R.drawable.background_loser);
                            }
                            txv_Home_Main_Change[i].setText(String.format("%.02f", change)+"%");
                            txv_bottomLog.setText(""+i);
                        }
                    });
                }
            }
        });
    }
}