package com.example.tradingapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
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
    private TextView txv_bottomLog, txv_accountName, txv_accountTitle;
    private ImageView img_accountAvatar;
    private TextView[] txv_Home_Main_Price;
    private TextView[] txv_Home_Main_Change;
    private int[] txv_Home_Main_Price_i;
    private int[] txv_Home_Main_Change_i;

    private ToggleButton btn_bottomtest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        initialization();
        iniThread();
        getHomePageData();
    }

    private void initialization() {
        txv_accountName = findViewById(R.id.txv_accountName);
        txv_accountTitle = findViewById(R.id.txv_accountTitle);
        img_accountAvatar = findViewById(R.id.img_accountAvatar);
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

    private void iniThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    for (int i = 0; i < 3; i++) {
                        getHomePageStockInformation(i);
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

    private void getHomePageStockInformation(int i) {
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
                    String myResponse = response.body().string();
                    HomePage.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            昨收 129  當盤漲幅 185
                            try {
                                int start = myResponse.indexOf("\"125\":");
                                start = myResponse.indexOf(":", start) + 1;
                                int end = myResponse.indexOf(",", start);
                                float price = Float.parseFloat(myResponse.substring(start, end));


                                start = myResponse.indexOf("\"129\":");
                                start = myResponse.indexOf(":", start) + 1;
                                end = myResponse.indexOf(",", start);

                                float yclose = Float.parseFloat(myResponse.substring(start, end));
                                float change = (price - yclose) / yclose * 100;
                                if (price == 0){
                                    price = yclose;
                                    change = 0;
                                }
                                txv_Home_Main_Price[i].setText(String.format("%.02f", price));
                                txv_Home_Main_Change[i].setText(String.format("%.02f", change) + "%");
                                if (change >= 0) {
                                    txv_Home_Main_Change[i].setBackgroundResource(R.drawable.background_grainer);
                                } else {
                                    txv_Home_Main_Change[i].setBackgroundResource(R.drawable.background_loser);
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode,data);
        if (requestCode == 8){
            if (data != null && data.getStringExtra("logout").equals("logout")) {
                Intent intent = new Intent(HomePage.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    public void getHomePageData() {
        String url = "https://tradingappserver.masterrongwu.repl.co/get_HomePage_Data";
        url+="?&user="+user+"&password="+password;
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();
                    Log.d("zha", myResponse);
                    HomePage.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = new JSONObject(myResponse);
                                JSONObject req = data.getJSONObject("returnHomePageInfo");
                                String accountName = req.getString("accountName");
                                String accountTitle = req.getString("accountTitle");
                                String accountAvatar = req.getString("accountAvatar");
                                Log.d("zha", "123123");
                                JSONArray twHotStocksRank_list = req.getJSONArray("twHotStocksRank_list");
                                Log.d("zha", twHotStocksRank_list.toString());
                                txv_accountName.setText(accountName);
                                txv_accountTitle.setText(accountTitle);
                                int avatar_id = 0;
                                if (accountAvatar.equals("a0"))
                                    avatar_id = R.drawable.avatar_level_1a;
                                img_accountAvatar.setImageResource(avatar_id);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("zha", "failed myResponse");
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                txv_accountName.setText("與伺服器連線失敗，若手機網路沒問題，請稍等片刻");
            }
        });
    }



    public void clickTradingStock(View view) {
        Intent intent = new Intent(HomePage.this, StockActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        startActivity(intent);
    }

    public void clickOrder(View view) {
        Intent intent = new Intent(HomePage.this, OrderActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        startActivity(intent);

    }

    public void clickTradingFutures(View view) {
        Intent intent = new Intent(HomePage.this, TradingFuturesActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        startActivity(intent);

    }

    public void clickAccount(View view) {
        Intent intent = new Intent(HomePage.this, AccountActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        startActivityForResult(intent, 8);
    }


    public void clickSecurityAccount(View view) {
        Intent intent = new Intent(HomePage.this, SecuritiesAccountActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        startActivity(intent);

    }

    public void clickDetail(View view) {
        Intent intent = new Intent(HomePage.this, DetailsActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        startActivity(intent);
    }

    public void clickPortfolio(View view) {
        Intent intent = new Intent(HomePage.this, InStockActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        startActivity(intent);
    }

    public void clickTalk(View view) {
        Intent intent = new Intent(HomePage.this, TalkActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        startActivity(intent);
    }

    public void clickHot(View view) {
        Intent intent = new Intent(HomePage.this, HotStocksActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        startActivity(intent);
    }
}