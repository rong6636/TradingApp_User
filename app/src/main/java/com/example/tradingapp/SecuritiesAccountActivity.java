package com.example.tradingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SecuritiesAccountActivity extends AppCompatActivity {
    private TextView txv_total_funds, txv_totalProfit, txv_availableMoney, txv_realizedProfit, txv_unrealizedProfit, txv_ROI, txv_stockROI;
    private String user, password, activityFrom;
    boolean FLAG_CONNECT_SA_PERMISSION = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_securities_account);

        iniTextView();
        initIntent();

        connectSecurityAAccountThread();
    }

    private void iniTextView() {
        txv_total_funds = findViewById(R.id.txv_total_funds);
        txv_totalProfit = findViewById(R.id.txv_totalProfit);
        txv_availableMoney = findViewById(R.id.txv_availableMoney);
        txv_realizedProfit = findViewById(R.id.txv_realizedProfit);
        txv_unrealizedProfit = findViewById(R.id.txv_unrealizedProfit);
        txv_ROI = findViewById(R.id.txv_ROI);
        txv_stockROI = findViewById(R.id.txv_stockROI);
        FLAG_CONNECT_SA_PERMISSION = true;
    }
    private void initIntent() {
        Intent intent = getIntent();
        user = intent.getStringExtra("user");
        password = intent.getStringExtra("password");
        activityFrom = intent.getStringExtra("from");
        if (activityFrom == null){
            activityFrom = "";
        }
    }


    private void connectSecurityAAccountThread() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while (FLAG_CONNECT_SA_PERMISSION) {
                    renewUiStockBestaskbid(count++);
                    try {
                        Thread.sleep(20000 + (int) (Math.random() * 60000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void renewUiStockBestaskbid(int firstTime) {
//        otc tse
        OkHttpClient client = new OkHttpClient();
//        https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_2330.tw&json=1&delay=0
        String url_replit = "https://tradingappserver.masterrongwu.repl.co/get_securityAccount";
        url_replit+="?user="+user+"&password="+password;
        Request request = new Request.Builder()
                .url(url_replit)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("zha", "failed onFailure");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();
                    Log.d("zha",myResponse);
                    SecuritiesAccountActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = new JSONObject(myResponse);
                                txv_total_funds.setText("資金："+String.valueOf(data.getInt("funds")));
                                txv_availableMoney.setText(String.valueOf(data.getInt("availableMoney")));
                                txv_totalProfit.setText(String.valueOf(data.getInt("realizedProfit")+data.getInt("unrealizedProfit")));
                                txv_realizedProfit.setText(String.valueOf(data.getInt("realizedProfit")));
                                txv_unrealizedProfit.setText(String.valueOf(data.getInt("unrealizedProfit")));
                            }catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("zha", "failed myResponse and json");
                            }
                        }
                    });
                }
            }
        });
    }




    public void clickClose(View view) {
        FLAG_CONNECT_SA_PERMISSION = false;
        finish();
    }

}