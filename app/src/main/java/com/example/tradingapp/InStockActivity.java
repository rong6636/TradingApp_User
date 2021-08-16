package com.example.tradingapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InStockActivity extends AppCompatActivity {
    String user, password, activityFrom;
    private SimpleAdapter adapter;
    private boolean FLAG_RENEW_PERMISSION = false, FLAG_RENEW_ITEM_PERMISSION = false;
    private LinkedList<HashMap<String, String>> datalist;
    private ListView list_inStockList;
    private TextView txv_inStockRenewTime;
    Map futuresPrice = new HashMap();
    Map urlList = new HashMap();

    private JSONObject jsonTwStock;
    private JSONObject jsonTwFutures;

    private String[] from = {"ticker", "price", "ap", "lots", "type", "income"};
    private int[] to = {R.id.txv_stockActivityItem_name, R.id.txv_stockActivityItem_price, R.id.txv_stockActivityItem_ap, R.id.txv_stockActivityItem_lot, R.id.txv_stockActivityItem_type, R.id.txv_stockActivityItem_income};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_stock);


        initialization();
        initListView();
        initIntent();
        connectSeverGetPortfolio();
        renewItemThread();
    }

    private void renewItemThread() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (FLAG_RENEW_PERMISSION) {
                    getAllFuturesPrice();
                    try {
                        Thread.sleep(1500 + (int) (Math.random() * 5000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        renewPortfolioItem();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void initialization() {
        futuresPrice.put("大台指近一", 0);
        futuresPrice.put("大台指近二", 0);
        futuresPrice.put("小台指近一", 0);
        futuresPrice.put("小台指近二", 0);
        futuresPrice.put("金融期近一", 0);
        futuresPrice.put("電子期近一", 0);


        urlList.put("大台指近一", "https://tw.screener.finance.yahoo.net/future/q?type=tick&perd=1m&mkt=01&sym=WTX%26");
        urlList.put("大台指近二", "https://tw.screener.finance.yahoo.net/future/q?type=tick&perd=1m&mkt=01&sym=WTX%40");
        urlList.put("小台指近一", "https://tw.screener.finance.yahoo.net/future/q?type=tick&perd=1m&mkt=01&sym=WMT%26");
        urlList.put("小台指近二", "https://tw.screener.finance.yahoo.net/future/q?type=tick&perd=1m&mkt=01&sym=WMT%40");
        urlList.put("金融期近一", "https://tw.screener.finance.yahoo.net/future/q?type=tick&perd=1m&mkt=01&sym=WTF%26");
        urlList.put("電子期近一", "https://tw.screener.finance.yahoo.net/future/q?type=tick&perd=1m&mkt=01&sym=WTE%26");
    }

    private void initListView(){
        txv_inStockRenewTime = findViewById(R.id.txv_inStockRenewTime);
        list_inStockList = findViewById(R.id.list_inStockList);
        datalist = new LinkedList<>();
        adapter = new SimpleAdapter(this, datalist, R.layout.item_instockitem, from, to);
        list_inStockList.setAdapter(adapter);

        list_inStockList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("zha", datalist.get((int)id).toString());
                HashMap<String, String> data = datalist.get((int)id);
                showDetails(data);
            }
        });
        FLAG_RENEW_PERMISSION = true;
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


    private void showDetails(HashMap<String, String> data){
        Log.d("zha", data.toString());
        String content = "委託：" +data.get("type") + " " +data.get("ticker")+"\n"+"成交數："+data.get("lots")+"\n"+data.get("detail");
        new AlertDialog.Builder(InStockActivity.this)
                .setTitle(data.get("id") + " 細項")
                .setMessage(content)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("zha", "OK?");
                    }
                }).show();
    }


    private void connectSeverGetPortfolio() {
        OkHttpClient client = new OkHttpClient();

        String parameter = "user="+user+"&password="+password;
        String url = "https://tradingAppServer.masterrongwu.repl.co/getPortfolio?"+parameter;
        Log.d("zha", url);
        Request request = new Request.Builder()
                .url(url)
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
                    Log.d("zha", "onResponse"+myResponse);
                    InStockActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Calendar mCal = Calendar.getInstance();
                                CharSequence s = DateFormat.format("MM-dd kk:mm", mCal.getTime());    // kk:24小時制, hh:12小時制
                                txv_inStockRenewTime.setText("更新時間："+s);

                                JSONObject data = new JSONObject(myResponse);
                                Log.d("zha", data.length()+"");

                                jsonTwStock = data.getJSONObject("twStocks");
                                jsonTwFutures = data.getJSONObject("twFutures");
                                renewPortfolioItem();

                                FLAG_RENEW_ITEM_PERMISSION = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("zha", "failed myResponse");
                                txv_inStockRenewTime.setText("回傳格式出錯");
                                FLAG_RENEW_ITEM_PERMISSION = false;
                            }
                        }
                    });
                }
            }
        });
    }

    private void getFuturesPrice(String ticker){
        String url = (String) urlList.get(ticker);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
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
                    InStockActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            昨收 129  當盤漲幅 185
                            try {
                                int start = myResponse.indexOf("\"125\":");
                                start = myResponse.indexOf(":", start) + 1;
                                int end = myResponse.indexOf(",", start);
                                float price = Float.parseFloat(myResponse.substring(start, end));
                                futuresPrice.put(ticker, price);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    private void getAllFuturesPrice() {
        getFuturesPrice("大台指近一");
        getFuturesPrice("大台指近二");
        getFuturesPrice("小台指近一");
        getFuturesPrice("小台指近二");
        getFuturesPrice("金融期近一");
        getFuturesPrice("電子期近一");
    }

    public void renewPortfolioItem() throws JSONException {
        datalist.clear();
        Iterator it_twStocks = jsonTwStock.keys();
        Iterator it_twFutures = jsonTwFutures.keys();

        while (it_twStocks.hasNext()){
            String stock = (String)it_twStocks.next();
            JSONObject jStock = jsonTwStock.getJSONObject(stock);
            addElementToPortfolioList(jStock, stock);
        }

        while (it_twFutures.hasNext()){
            String futures = (String)it_twFutures.next();
            JSONObject jFutures = jsonTwFutures.getJSONObject(futures);
            addElementToPortfolioList(jFutures, futures);
        }
    }

    public void addElementToPortfolioList(JSONObject data, String name) throws JSONException {
        Log.d("zha", "11112");
        InStockActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Math.abs(data.getInt("Lots"))>0)
                    {
                        HashMap<String, String> h_inStock = new HashMap<>();
                        Log.d("zha", data.toString());
                        Log.d("zha", name);

                        h_inStock.put(from[0], name);
                        h_inStock.put(from[1], futuresPrice.get(name).toString());
                        h_inStock.put(from[2], data.getString("Average_Price"));
                        h_inStock.put(from[3], String.valueOf(Math.abs(data.getInt("Lots"))));
                        if (data.getInt("Lots") > 0) {
                            h_inStock.put(from[4], "買");
                        } else {
                            h_inStock.put(from[4], "賣");
                        }
                        double income = 0;
                        if ((Float)futuresPrice.get(name) > 0){
                            income = ((Float)futuresPrice.get(name)-data.getDouble("Average_Price"))*data.getInt("Lots");
                        }
                        if (name.equals("大台指近一") || name.equals("大台指近二") || name.equals("電子期近一")){
                            income*=200;
                        }
                        else if (name.equals("小台指近一") || name.equals("小台指近二")){
                            income*=50;
                        }
                        else if (name.equals("金融期近一")){
                            income*=1000;
                        }
                        h_inStock.put(from[5], String.valueOf(Math.round(income)));

                        datalist.add(h_inStock);
                        Log.d("zha", h_inStock.toString());
                        adapter.notifyDataSetInvalidated();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    public void clickClose(View view) {
        FLAG_RENEW_PERMISSION = false;
        finish();
    }

    public void clickTrading(View view) {
        if (activityFrom.equals("StockActivity")){
            Intent intent = new Intent(InStockActivity.this, StockActivity.class);
            intent.putExtra("user", user);
            intent.putExtra("password", password);
            startActivityForResult(intent, 1);
            clickClose(view);
        }
        else if (activityFrom.equals("TradingFuturesActivity")){
            Intent intent = new Intent(InStockActivity.this, TradingFuturesActivity.class);
            intent.putExtra("user", user);
            intent.putExtra("password", password);
            startActivityForResult(intent, 1);
            clickClose(view);
        }
        else{
            clickClose(view);
        }
    }

    public void clickOrder(View view) {
        Intent intent = new Intent(InStockActivity.this, OrderActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        intent.putExtra("from", "TradingFuturesActivity");
        startActivityForResult(intent, 2);
        clickClose(view);
    }

    public void clickDetail(View view) {
        Intent intent = new Intent(InStockActivity.this, DetailsActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        startActivity(intent);
        clickClose(view);
    }

    public void test(View view) {
    }
}
