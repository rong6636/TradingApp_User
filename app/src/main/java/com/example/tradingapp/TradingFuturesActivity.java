package com.example.tradingapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.IntRange;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.SpanWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TradingFuturesActivity extends AppCompatActivity {
    private String user, password;
    TextView txv_futures_total_bid, txv_futures_total_ask, txv_futures_titleType, txv_futures_titleName, txv_futures_titlePrice, txv_futures_titleChange, txv_futures_renewTime,
            txv_futures_orderPriceSub, txv_futures_orderPriceChoose, txv_futures_orderPriceAdd, txv_futures_orderLotSub, txv_futures_orderLotChoose, txv_futures_orderLotAdd, txv_futures_showBudget;
    TextView[] txv_futures_bidlot = new TextView[5], txv_futures_bidprice = new TextView[5], txv_futures_asklot = new TextView[5], txv_futures_askprice = new TextView[5];

    Spinner spr_futures_searchFutures;
    Button btn_futures_changeFuturesType;
    Boolean FLAG_TRADING_PERMISSION = false;
    Boolean FLAG_SEARCH_FUTURES_PERMISSION = true;

    int futuresMargin = 0;

    String[] futuresList, futuresUrlList;
    int currentFuturesIndex = 0;
    float orderPrice = 0;
    float[] minfluctuation;
    int orderLot = 0;
    ArrayAdapter<String> adapterTwseFuturesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trading_futures);

        initSpinner();
        iniTextView();
        initIntent();

        searchFuturesThread();
    }

    private  void initSpinner(){
        futuresList= new String[]{"大台指近一", "大台指近二", "小台指近一", "小台指近二", "金融期近一", "電子期近一"};
        futuresUrlList= new String[]{"https://tw.screener.finance.yahoo.net/future/q?type=tick&perd=1m&mkt=01&sym=WTX%26",
                "https://tw.screener.finance.yahoo.net/future/q?type=tick&perd=1m&mkt=01&sym=WTX%40",
                "https://tw.screener.finance.yahoo.net/future/q?type=tick&perd=1m&mkt=01&sym=WMT%26",
                "https://tw.screener.finance.yahoo.net/future/q?type=tick&perd=1m&mkt=01&sym=WMT%40",
                "https://tw.screener.finance.yahoo.net/future/q?type=tick&perd=1m&mkt=01&sym=WTF%26",
                "https://tw.screener.finance.yahoo.net/future/q?type=tick&perd=1m&mkt=01&sym=WTE%26"
        };
        minfluctuation = new float[]{1, 1, 1, 1, 0.2f, 0.05f};
        adapterTwseFuturesList = new ArrayAdapter<>(TradingFuturesActivity.this, android.R.layout.simple_spinner_dropdown_item, futuresList);
        spr_futures_searchFutures = findViewById(R.id.spr_futures_searchFutures);
        spr_futures_searchFutures.setAdapter(adapterTwseFuturesList);
        spr_futures_searchFutures.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FLAG_SEARCH_FUTURES_PERMISSION = true;
                currentFuturesIndex = (int)id;
                renewUi(0);
                Toast.makeText(TradingFuturesActivity.this, "您選擇了:" + futuresList[position], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void iniTextView() {
        txv_futures_titleType = findViewById(R.id.txv_futures_titleType);
        txv_futures_titleName = findViewById(R.id.txv_futures_titleName);
        txv_futures_titlePrice = findViewById(R.id.txv_futures_titlePrice);
        txv_futures_titleChange = findViewById(R.id.txv_futures_titleChange);

        btn_futures_changeFuturesType = findViewById(R.id.btn_futures_changeFuturesType);

        txv_futures_total_bid = findViewById(R.id.txv_futures_total_bid);
        txv_futures_total_ask = findViewById(R.id.txv_futures_total_ask);

        txv_futures_bidlot[0] = findViewById(R.id.txv_futures_bidlot_1);
        txv_futures_bidprice[0] = findViewById(R.id.txv_futures_bidprcie_1);
        txv_futures_bidlot[1] = findViewById(R.id.txv_futures_bidlot_2);
        txv_futures_bidprice[1] = findViewById(R.id.txv_futures_bidprcie_2);
        txv_futures_bidlot[2] = findViewById(R.id.txv_futures_bidlot_3);
        txv_futures_bidprice[2] = findViewById(R.id.txv_futures_bidprcie_3);
        txv_futures_bidlot[3] = findViewById(R.id.txv_futures_bidlot_4);
        txv_futures_bidprice[3] = findViewById(R.id.txv_futures_bidprcie_4);
        txv_futures_bidlot[4] = findViewById(R.id.txv_futures_bidlot_5);
        txv_futures_bidprice[4] = findViewById(R.id.txv_futures_bidprcie_5);

        txv_futures_asklot[0] = findViewById(R.id.txv_futures_asklot_1);
        txv_futures_askprice[0] = findViewById(R.id.txv_futures_askprcie_1);
        txv_futures_asklot[1] = findViewById(R.id.txv_futures_asklot_2);
        txv_futures_askprice[1] = findViewById(R.id.txv_futures_askprcie_2);
        txv_futures_asklot[2] = findViewById(R.id.txv_futures_asklot_3);
        txv_futures_askprice[2] = findViewById(R.id.txv_futures_askprcie_3);
        txv_futures_asklot[3] = findViewById(R.id.txv_futures_asklot_4);
        txv_futures_askprice[3] = findViewById(R.id.txv_futures_askprcie_4);
        txv_futures_asklot[4] = findViewById(R.id.txv_futures_asklot_5);
        txv_futures_askprice[4] = findViewById(R.id.txv_futures_askprcie_5);
        txv_futures_renewTime = findViewById(R.id.txv_futures_renewTime);

        txv_futures_orderPriceSub = findViewById(R.id.txv_futures_orderPriceSub);
        txv_futures_orderPriceChoose = findViewById(R.id.txv_futures_orderPriceChoose);
        txv_futures_orderPriceAdd = findViewById(R.id.txv_futures_orderPriceAdd);
        txv_futures_orderLotSub = findViewById(R.id.txv_futures_orderLotSub);
        txv_futures_orderLotChoose = findViewById(R.id.txv_futures_orderLotChoose);
        txv_futures_orderLotAdd = findViewById(R.id.txv_futures_orderLotAdd);
        txv_futures_showBudget = findViewById(R.id.txv_futures_showBudget);
    }

    private void initIntent() {
        Intent intent = getIntent();
        user = intent.getStringExtra("user");
        password = intent.getStringExtra("password");
    }

    private void searchFuturesThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while (FLAG_SEARCH_FUTURES_PERMISSION) {
                    renewUi(count++);
                    try {
                        Thread.sleep(2000 + (int) (Math.random() * 6500));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    private void renewUi(int firstTime) {
//        otc tse
        OkHttpClient client = new OkHttpClient();
        String url = futuresUrlList[currentFuturesIndex];

        Request request = new Request.Builder()
                .url(url)
                .build();
        Log.d("zha", "Future renewUi()");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("zha", "failed onFailure");
                e.printStackTrace();
                TradingFuturesActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        renewUiTitleText("E","連線失效","-","請檢察網路狀況", "...");
                        FLAG_SEARCH_FUTURES_PERMISSION = false;
                        FLAG_TRADING_PERMISSION = false;
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();
                    Log.d("zha",myResponse);
                    TradingFuturesActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                int start, end;
                                Animation am_renew = AnimationUtils.loadAnimation(getBaseContext(), R.anim.alpha_renewtrading);

//                                當前成交價
                                start = myResponse.indexOf("\"125\":");
                                start = myResponse.indexOf(":", start) + 1;
                                end = myResponse.indexOf(",", start);
                                float price = Float.parseFloat(myResponse.substring(start, end));

//                                當前漲幅
                                start = myResponse.indexOf("\"129\":");
                                start = myResponse.indexOf(":", start) + 1;
                                end = myResponse.indexOf(",", start);
                                float yclose = Float.parseFloat(myResponse.substring(start, end));
                                float change = (price - yclose) / yclose * 100;
                                if (price == 0){
                                    price = yclose;
                                    change = 0;
                                }
                                String str_c = "";
                                if (change > 0) {
                                    str_c += "▲";
                                    txv_futures_titlePrice.setTextColor(getResources().getColor(R.color.grainer_));
                                    txv_futures_titleChange.setTextColor(getResources().getColor(R.color.grainer_));
                                } else if (change < 0) {
                                    str_c += "▼";
                                    txv_futures_titlePrice.setTextColor(getResources().getColor(R.color.loser_));
                                    txv_futures_titleChange.setTextColor(getResources().getColor(R.color.loser_));
                                }
                                str_c +=String.format("%.02f", change) + "%";

//                                最優買方出價-5
                                start = myResponse.indexOf("\"109\":");
                                start = myResponse.indexOf(":", start) + 1;
                                end = myResponse.indexOf(",", start);
                                float bestBid = Float.parseFloat(myResponse.substring(start, end));

//                                最優賣方喊價+5
                                start = myResponse.indexOf("\"110\":");
                                start = myResponse.indexOf(":", start) + 1;
                                end = myResponse.indexOf(",", start);
                                float bestAsk = Float.parseFloat(myResponse.substring(start, end));

//                                買賣方最優前五價格的量
                                int[] bestBidLot = new int[5];
                                int[] bestAskLot = new int[5];
                                start = myResponse.indexOf("\"113\":");
                                start = myResponse.indexOf(":", start) + 1;
                                end = myResponse.indexOf(",", start);
                                bestBidLot[0] = Integer.valueOf(myResponse.substring(start, end));
                                start = myResponse.indexOf("\"114\":");
                                start = myResponse.indexOf(":", start) + 1;
                                end = myResponse.indexOf(",", start);
                                bestAskLot[0] = Integer.valueOf(myResponse.substring(start, end));
                                start = myResponse.indexOf("\"115\":");
                                start = myResponse.indexOf(":", start) + 1;
                                end = myResponse.indexOf(",", start);
                                bestBidLot[1] = Integer.valueOf(myResponse.substring(start, end));
                                start = myResponse.indexOf("\"116\":");
                                start = myResponse.indexOf(":", start) + 1;
                                end = myResponse.indexOf(",", start);
                                bestAskLot[1] = Integer.valueOf(myResponse.substring(start, end));
                                start = myResponse.indexOf("\"117\":");
                                start = myResponse.indexOf(":", start) + 1;
                                end = myResponse.indexOf(",", start);
                                bestBidLot[2] = Integer.valueOf(myResponse.substring(start, end));
                                start = myResponse.indexOf("\"118\":");
                                start = myResponse.indexOf(":", start) + 1;
                                end = myResponse.indexOf(",", start);
                                bestAskLot[2] = Integer.valueOf(myResponse.substring(start, end));
                                start = myResponse.indexOf("\"119\":");
                                start = myResponse.indexOf(":", start) + 1;
                                end = myResponse.indexOf(",", start);
                                bestBidLot[3] = Integer.valueOf(myResponse.substring(start, end));
                                start = myResponse.indexOf("\"120\":");
                                start = myResponse.indexOf(":", start) + 1;
                                end = myResponse.indexOf(",", start);
                                bestAskLot[3] = Integer.valueOf(myResponse.substring(start, end));
                                start = myResponse.indexOf("\"121\":");
                                start = myResponse.indexOf(":", start) + 1;
                                end = myResponse.indexOf(",", start);
                                bestBidLot[4] = Integer.valueOf(myResponse.substring(start, end));
                                start = myResponse.indexOf("\"122\":");
                                start = myResponse.indexOf(":", start) + 1;
                                end = myResponse.indexOf(",", start);
                                bestAskLot[4] = Integer.valueOf(myResponse.substring(start, end));

                                Log.d("zha", bestAsk+", "+bestBid+", "+bestBidLot.toString()+bestAskLot.toString());

                                int tatalBidLot = 0, tatalAskLot = 0;
                                for (int i = 0; i < 5; i++) {
                                    String bp = String.format("%.02f", Float.valueOf(bestBid-i+4));
                                    String ap = String.format("%.02f", Float.valueOf(bestAsk+i-4));
                                    String bl = String.valueOf(bestBidLot[i]);
                                    String al = String.valueOf(bestAskLot[i]);

                                    if (!txv_futures_bidprice[i].getText().toString().equals(bp)){
                                        txv_futures_bidprice[i].startAnimation(am_renew);
                                        txv_futures_bidlot[i].startAnimation(am_renew);
                                    }
                                    if (!txv_futures_askprice[i].getText().toString().equals(ap)){
                                        txv_futures_askprice[i].startAnimation(am_renew);
                                        txv_futures_asklot[i].startAnimation(am_renew);
                                    }
                                    if (!txv_futures_bidlot[i].getText().toString().equals(bl)){
                                        txv_futures_bidlot[i].startAnimation(am_renew);
                                    }
                                    if (!txv_futures_asklot[i].getText().toString().equals(al)){
                                        txv_futures_asklot[i].startAnimation(am_renew);
                                    }

                                    txv_futures_bidprice[i].setText(bp);
                                    txv_futures_askprice[i].setText(ap);
                                    txv_futures_bidlot[i].setText(bl);
                                    txv_futures_asklot[i].setText(al);
                                    tatalBidLot += Integer.valueOf(bestBidLot[i]);
                                    tatalAskLot += Integer.valueOf(bestAskLot[i]);
                                }

                                txv_futures_total_bid.setText(String.valueOf(tatalBidLot));
                                txv_futures_total_ask.setText(String.valueOf(tatalAskLot));
                                renewUiTitleText("期", futuresList[currentFuturesIndex], String.format("%.02f", price), str_c, "1");
//                                        下單區更新
                                if (firstTime == 0) {
                                    orderPrice = Float.valueOf(price);
                                    orderLot = 1;
                                    txv_futures_orderPriceChoose.setText(String.format("%.02f", Float.valueOf(orderPrice)));
                                    txv_futures_orderLotChoose.setText("1");
                                    FLAG_SEARCH_FUTURES_PERMISSION = true;
                                    renewMargin();
                                }
                                FLAG_TRADING_PERMISSION = true;

                            } catch (NumberFormatException e) {
                                Log.d("zha", "Failed");
                                e.printStackTrace();
                                renewUiTitleText("E","意外錯誤","-","-", "...");
                                FLAG_SEARCH_FUTURES_PERMISSION = false;
                                FLAG_TRADING_PERMISSION = false;
                            }

                        }
                    });
                }
            }
        });
    }

    private void renewUiTitleText(String ntype, String nname, String nprice, String nchange, String ntime){
        TradingFuturesActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Animation am_renew = AnimationUtils.loadAnimation(getBaseContext(), R.anim.alpha_renewtrading);
                if (!txv_futures_titlePrice.getText().toString().equals(nprice)){
                    txv_futures_titlePrice.startAnimation(am_renew);
                }
                if (!txv_futures_titleChange.getText().toString().equals(nchange)){
                    txv_futures_titleChange.startAnimation(am_renew);
                }
                if (!txv_futures_renewTime.getText().toString().equals(ntime)){
                    txv_futures_renewTime.startAnimation(am_renew);
                }

                txv_futures_titleType.setText(ntype);
                txv_futures_titleName.setText(nname);
                txv_futures_titlePrice.setText(nprice);
                txv_futures_titleChange.setText(nchange);
                txv_futures_renewTime.setText("lastRenewTime: "+ntime);
            }
        });
    }
    public void renewMargin(){
        OkHttpClient client = new OkHttpClient();
        String url = "https://tradingAppServer.masterrongwu.repl.co/getMargin?futures="+futuresList[currentFuturesIndex];

        Request request = new Request.Builder()
                .url(url)
                .build();
        Log.d("zha", "Futures connectSeverGetMargin()");

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
                    Log.d("zha", myResponse);
                    TradingFuturesActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            futuresMargin = Integer.valueOf(myResponse);
                            txv_futures_showBudget.setText("約 TWD"+futuresMargin*orderLot);
                            Log.d("zha", "futuresMargin："+futuresMargin);
                        }
                    });
                }
            }
        });
    }


    private void connectSeverTradingFutures(String orderType) {
        OkHttpClient client = new OkHttpClient();
        String parameter = "user="+user+"&password="+password+"&type="+orderType+"&market=twFutures"+"&ticker="+futuresList[currentFuturesIndex]+"&price="+orderPrice+"&lot="+orderLot+"&time="+2118+"&name="+futuresList[currentFuturesIndex];
        String url = "https://tradingAppServer.masterrongwu.repl.co/entrust?"+parameter;
        Log.d("zha", url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("zha", "failed onFailure");
                e.printStackTrace();
                TradingFuturesActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TradingFuturesActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(TradingFuturesActivity.this)
                                        .setTitle("與伺服器連線錯誤")
                                        .setMessage("無法與伺服器連線! 目前交易伺服器離線中。\n請檢查網路狀況，若網路檢查正常，請稍後操作。")
                                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) { }
                                        }).show();
                            }
                        });
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();
                    Log.d("zha", myResponse);
                    TradingFuturesActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("zha", "run");
                            String content;
                            if (myResponse.equals("密碼錯誤"))
                                content="密碼錯誤";
                            else if (myResponse.equals("使用者不存在"))
                                    content="使用者不存在";
                            else if(myResponse.indexOf("暫時的委託單號為：")>=0){
                                content = "因交易繁忙，無法瞬時成立\n\n若委託單成立，可以於委託查看到。";
                            }
                            else if(myResponse.indexOf(user)>=0){
                                content = "委託單號："+myResponse+"\n下單成功，請到委託查看。";
                            }
                            else if(myResponse.indexOf("不在委託時間")>=0){
                                content = "不在委託時間。";
                            }
                            else {
                                content = "出現意外錯誤!";
                            }
                            new AlertDialog.Builder(TradingFuturesActivity.this)
                                    .setTitle(myResponse)
                                    .setMessage(content)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) { }
                                    }).show();
                        }
                    });
                }
                else{
                    TradingFuturesActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(TradingFuturesActivity.this)
                                    .setTitle("與伺服器連線錯誤")
                                    .setMessage("無法與伺服器連線! 目前交易伺服器離線中。\n若網路檢查正常，請稍後操作。")
                                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) { }
                                    }).show();
                        }
                    });
                }
            }
        });
    }






    public void close(View view) {
        FLAG_SEARCH_FUTURES_PERMISSION = false;
        FLAG_TRADING_PERMISSION = false;
        finish();
    }

    public void clickOrder(View view) {
        Intent intent = new Intent(TradingFuturesActivity.this, OrderActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        intent.putExtra("from", "TradingFuturesActivity");
        startActivityForResult(intent, 2);
        close(view);

    }

    public void clickTradingFutures(View view) {

    }

    public void test(View view) {

    }

    public void changeFuturesType(View view) {

    }

    public void futures_priceChoose(View view) {

    }

    public void futures_priceAdd(View view) {
        orderPrice+=minfluctuation[currentFuturesIndex];
        txv_futures_orderPriceChoose.setText(String.format("%.02f", Float.valueOf(orderPrice)));
    }

    public void futures_lotSub(View view) {
        if (orderLot > 1){
            orderLot-=1;
        }
        txv_futures_orderLotChoose.setText(String.format("%.00f", Float.valueOf(orderLot)));
        txv_futures_showBudget.setText("約 TWD"+futuresMargin*orderLot);
    }

    public void futures_priceSub(View view) {
        orderPrice-=minfluctuation[currentFuturesIndex];
        txv_futures_orderPriceChoose.setText(String.format("%.02f", Float.valueOf(orderPrice)));

    }

    public void futures_lotSChoose(View view) {

    }

    public void futures_lotAdd(View view) {
        orderLot+=1;
        txv_futures_orderLotChoose.setText(String.format("%.00f", Float.valueOf(orderLot)));
        txv_futures_showBudget.setText("約 TWD"+futuresMargin*orderLot);
    }

    public void bullFutures(View view) {
        if (FLAG_TRADING_PERMISSION) {
            String content = "委託類型：多單 " + futuresList[currentFuturesIndex] +"\n"+
                    "委託價：" + orderPrice+"\n"+
                    orderLot+"單位 (1單位 = 1口)\n\n"+
                    "提醒：每日凌晨5點到早上8點30分與1點45分到2點40分停止委託。若與持倉單位反向操作伺服器將會自動評估平倉量。";
            new AlertDialog.Builder(TradingFuturesActivity.this)
                    .setTitle("委託單確認")
                    .setMessage(content)
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            connectSeverTradingFutures("多");
                        }
                    }).show();
        }
        else{
            new AlertDialog.Builder(TradingFuturesActivity.this)
                    .setTitle("無法委託")
                    .setMessage("無法委託")
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        }

    }

    public void BearFutures(View view) {
        if (FLAG_TRADING_PERMISSION) {
            String content = "委託類型：空單 " + futuresList[currentFuturesIndex] +"\n"+
                    "委託價：" + orderPrice+"\n"+
                    orderLot+"單位 (1單位 = 1口)\n\n"+
                    "提醒：每日凌晨5點到早上8點30分與1點45分到2點40分停止委託。若與持倉單位反向操作伺服器將會自動評估平倉量。";
            new AlertDialog.Builder(TradingFuturesActivity.this)
                    .setTitle("委託單確認")
                    .setMessage(content)
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            connectSeverTradingFutures("空");
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("zha", "取消?");
                        }
                    })
                    .show();
        }else{
            new AlertDialog.Builder(TradingFuturesActivity.this)
                    .setTitle("無法委託")
                    .setMessage("無法委託")
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("zha", "取消?");
                        }
                    })
                    .show();
        }

    }

    public void clickDetail(View view) {
        Intent intent = new Intent(TradingFuturesActivity.this, DetailsActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        startActivityForResult(intent, 8);
        close(view);
    }

    public void clickPortfolio(View view) {
        Intent intent = new Intent(TradingFuturesActivity.this, portfolioActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        startActivity(intent);
        close(view);
    }
}