package com.example.tradingapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HotStocksActivity extends AppCompatActivity {
    private boolean FLAG_GET_HOT_PERMISSION;
    private String USER, PASSWORD, activityFrom, twVolumeRank, twChangeUpRank, twChangeDownRank, twTurnoverRank;

    private ListView lis_hotStocksList;
    private SimpleAdapter adapter;
    private LinkedList<HashMap<String, String>> stocksList;
    private String[] from = {"name", "ticker", "price", "change", "volume", "high", "low", "yc"};
    private int[] to = {R.id.txv_hotStocksItem_name, R.id.txv_hotStocksItem_ticker, R.id.txv_hotStocksItem_price, R.id.txv_hotStocksItem_change, R.id.txv_hotStocksItem_volume, R.id.txv_hotStocksItem_high, R.id.txv_hotStocksItem_low, R.id.txv_hotStocksItem_yc};


    private Map stocksName, stocksTicker, stocksPrice, stocksChange, stocksVolume, stocksHigh, stocksLow, stocksYC;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_stocks);

        init();
        initIntent();
        get_twHotStocksList();
    }

    private void init() {
        lis_hotStocksList = findViewById(R.id.lis_hotStocksList);

        stocksList = new LinkedList<>();
        adapter = new SimpleAdapter(this, stocksList, R.layout.item_hotstocksitem, from, to);
        lis_hotStocksList.setAdapter(adapter);

        twVolumeRank = "tse_2330.tw";
        twChangeUpRank = "tse_2330.tw";
        twChangeDownRank = "tse_2330.tw";
        twTurnoverRank = "tse_2330.tw";

        stocksName = new HashMap();
        stocksTicker = new HashMap();
        stocksPrice = new HashMap();
        stocksChange = new HashMap();
        stocksVolume = new HashMap();
        stocksHigh = new HashMap();
        stocksLow = new HashMap();
        stocksYC = new HashMap();
    }

    private void initIntent() {
        Intent intent = getIntent();
        USER = intent.getStringExtra("user");
        PASSWORD = intent.getStringExtra("password");
    }


    private void get_twHotStocksList() {
        OkHttpClient client = new OkHttpClient();

        String url = "https://tradingAppServer.masterrongwu.repl.co/get_twHotStocksList?";
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
                    Log.d("zha", myResponse);
                    try {
                        JSONObject data = new JSONObject(myResponse);
                        JSONArray hotArr = data.getJSONArray("rankList");
                        if (hotArr.length()>=4){
                            twVolumeRank = hotArr.getString(0);
                            twChangeUpRank = hotArr.getString(1);
                            twChangeDownRank = hotArr.getString(2);
                            twTurnoverRank = hotArr.getString(3);

                            Log.d("zha", "twTurnoverRank   "+twTurnoverRank);

                            getTwseStocksInfo(twTurnoverRank);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    private void renewTwHotStockTurnoverRankMap() {
        Log.d("zha", twTurnoverRank);
        String [] rank = twTurnoverRank.split("\\|");
        for (int i = 0; i<rank.length; i++){
            Log.d("zha", ""+i);
            addElementToRankList(rank[i]);
        }
    }

    private void renewTwHotStockChangeDownRankMap() {

    }

    private void renewTwHotStockChangeUpRankMap() {

    }

    private void renewTwHotStockVolumeRankMap() {

    }

    private void addElementToRankList(String ticker){
        Log.d("zha", ticker)  ;
        HashMap<String, String> stocksInfo = new HashMap<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stocksInfo.put(from[0], stocksName.getOrDefault(ticker, "-").toString());
            stocksInfo.put(from[1], stocksTicker.getOrDefault(ticker, "-").toString());
            stocksInfo.put(from[2], stocksPrice.getOrDefault(ticker, "-").toString());
            stocksInfo.put(from[3], stocksChange.getOrDefault(ticker, "-").toString());
            stocksInfo.put(from[4], stocksVolume.getOrDefault(ticker, "-").toString());
            stocksInfo.put(from[5], stocksHigh.getOrDefault(ticker, "-").toString());
            stocksInfo.put(from[6], stocksLow.getOrDefault(ticker, "-").toString());
            stocksInfo.put(from[7], stocksYC.getOrDefault(ticker, "-").toString());
        }

        stocksList.add(stocksInfo);
        adapter.notifyDataSetInvalidated();
    }

    private void getTwseStocksInfo(String str) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch="+str+"&json=1&delay=0";

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
                    Log.d("zha",myResponse);
                    HotStocksActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = new JSONObject(myResponse);
                                if (data.has("msgArray")){
                                    if (data.getJSONArray("msgArray").length()==0){
                                        Log.d("zha", "E 查無結果 請檢查股票代號");
                                    }
                                    else {
                                        JSONArray arr = data.getJSONArray("msgArray");
                                        for (int i = 0; i<arr.length(); i++)
                                        {
                                            addStocksMap(arr.getJSONObject(i));
                                        }
                                    }
                                }

                            } catch (JSONException e) {
                                Log.d("zha", "Failed JSONException" + e.toString());
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    private void addStocksMap(JSONObject data) throws JSONException {
        String ticker = data.getString("ex")+"_"+data.getString("ch");
        Log.d("zha", ticker);
        stocksName.put(ticker, data.getString("n"));
        stocksTicker.put(ticker, data.getString("c"));
        stocksPrice.put(ticker, data.getString("z").substring(0, data.getString("z").length()-2));
        if (data.getString("z").equals("-")){
            stocksPrice.put(ticker, data.getString("b").split("_")[0].substring(0, data.getString("b").split("_")[0].length()-2));
        }
        Float p = Float.parseFloat(stocksPrice.get(ticker).toString());
        Float yc = Float.parseFloat(data.getString("y"));
        stocksChange.put(ticker, String.format("%.02f",(p-yc)) +" ("+String.format("%.02f", (p-yc)/yc*100) + "%)");
        stocksVolume.put(ticker, "總量："+intTransfer(Integer.valueOf(data.getString("v"))));
        stocksHigh.put(ticker, "H："+data.getString("h").substring(0, data.getString("h").length()-2));
        stocksLow.put(ticker, "L："+data.getString("l").substring(0, data.getString("l").length()-2));
        stocksYC.put(ticker, "Y："+data.getString("y").substring(0, data.getString("y").length()-2));
    }

    private String intTransfer(int i){
        String _str = "";
        if (i>1000000){
            _str = "M";
            i/=1000000;

        }
        if(i>1000){
            _str = "K";
            i/=1000;
        }

        return i+_str;
    }

    public void clickTalk(View view) {
        Intent intent = new Intent(HotStocksActivity.this, TalkActivity.class);
        intent.putExtra("user", USER);
        intent.putExtra("password", PASSWORD);
        startActivity(intent);
        clickClose(view);

    }

    public void clickHotStocks(View view) {

    }

    public void clickClose(View view) {
        finish();
        FLAG_GET_HOT_PERMISSION = false;
    }

    public void clickTest(View view) {
        Log.d("zha", ""+stocksName.size());
        Log.d("zha", stocksName.toString());
        Log.d("zha", stocksPrice.toString());
        Log.d("zha", stocksHigh.toString());
        Log.d("zha", stocksLow.toString());
        Log.d("zha", stocksYC.toString());

        stocksList.clear();
        renewTwHotStockTurnoverRankMap();
    }
}