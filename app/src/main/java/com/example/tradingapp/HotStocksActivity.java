package com.example.tradingapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
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
    private boolean FLAG_RENEW_LISTVIEW_UI;
    private String USER, PASSWORD, activityFrom, twVolumeRank, twChangeUpRank, twChangeDownRank, twTurnoverRank;

    private ListView lis_hotStocksList;
    private listLayoutAdapter adapter;
    private String[] from = {"name", "ticker", "price", "change", "volume", "high", "low", "yc", "ExCh"};
    private int[] to = {R.id.txv_hotStocksItem_name, R.id.txv_hotStocksItem_ticker, R.id.txv_hotStocksItem_price, R.id.txv_hotStocksItem_change, R.id.txv_hotStocksItem_volume, R.id.txv_hotStocksItem_high, R.id.txv_hotStocksItem_low, R.id.txv_hotStocksItem_yc};
    private Map stocksExCh, stocksName, stocksTicker, stocksPrice, stocksChange, stocksVolume, stocksHigh, stocksLow, stocksYC;

    private Spinner spr_hotStocks_choiceType;
    ArrayAdapter<String> adapterChoiceType;
    private String [] choiceType;
    private String [] rankList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_stocks);

        init();
        initIntent();
        get_twHotStocksList();

        initSpinner();
    }

    //自訂listlayoutadapter，繼承 BaseAdapter。
    //再實做出 getCount、getItem、getitemid、getView函式。
    public class listLayoutAdapter extends BaseAdapter {

        private LayoutInflater listlayoutInflater;

        public listLayoutAdapter(Context c){
            listlayoutInflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            return rankList.length;
        }

        @Override
        public Object getItem(int position) {
            return rankList[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = listlayoutInflater.inflate(R.layout.item_hotstocksitem,null);
            //設定自訂樣板上物件對應的資料。
            TextView txv_name =  convertView.findViewById(R.id.txv_hotStocksItem_name);
            TextView txv_ticker =  convertView.findViewById(R.id.txv_hotStocksItem_ticker);
            TextView txv_price =  convertView.findViewById(R.id.txv_hotStocksItem_price);
            TextView txv_volume =  convertView.findViewById(R.id.txv_hotStocksItem_volume);
            TextView txv_change =  convertView.findViewById(R.id.txv_hotStocksItem_change);
            TextView txv_high =  convertView.findViewById(R.id.txv_hotStocksItem_high);
            TextView txv_low =  convertView.findViewById(R.id.txv_hotStocksItem_low);
            TextView txv_yc =  convertView.findViewById(R.id.txv_hotStocksItem_yc);
            // 物件字串設定
            String yesterdayClose = stocksYC.getOrDefault(rankList[position], "-").toString();
            String price = stocksPrice.getOrDefault(rankList[position], "-").toString();
            String change = stocksChange.getOrDefault(rankList[position], "-").toString();
            if (!price.equals("-") && !yesterdayClose.equals("-")){
                Float p = Float.valueOf(price);
                Float yc = Float.valueOf(yesterdayClose);

                // 處理"漲", "跌"顏色
                int changeColor = getColor(R.color.primary_white);
                if (p-yc > 0) {
                    changeColor = getColor(R.color.grainer_);
                    change = "▴"+change;
                }
                else if (p-yc <0) {
                    changeColor = getColor(R.color.loser_);
                    change = "▾"+change;
                }
                txv_change.setTextColor(changeColor);
                txv_price.setTextColor(changeColor);
                if (Math.abs(Float.valueOf(change.replace("▾", "").replace("▴", ""))) > 9.5){
                    txv_price.setTextColor(getColor(R.color.white));
                    txv_price.setBackgroundColor(changeColor);
                }

                change = String.format("%.02f",Math.abs(p-yc))+" ("+change+"%)";
            }

            txv_name.setText(stocksName.getOrDefault(rankList[position], "-").toString());
            txv_ticker.setText(stocksTicker.getOrDefault(rankList[position], "-").toString());
            txv_price.setText(price);
            txv_volume.setText(stocksVolume.getOrDefault(rankList[position], "-").toString());
            txv_change.setText(change);
            txv_high.setText("H："+stocksHigh.getOrDefault(rankList[position], "-").toString());
            txv_low.setText("L："+stocksLow.getOrDefault(rankList[position], "-").toString());
            txv_yc.setText("Y："+yesterdayClose);

            return convertView;
        }
    }

    private void init() {
        twVolumeRank = "tse_2330.tw";
        twChangeUpRank = "tse_2330.tw";
        twChangeDownRank = "tse_2330.tw";
        twTurnoverRank = "tse_2330.tw";

        rankList = new String[]{};

        stocksName = new HashMap();
        stocksTicker = new HashMap();
        stocksPrice = new HashMap();
        stocksChange = new HashMap();
        stocksVolume = new HashMap();
        stocksHigh = new HashMap();
        stocksLow = new HashMap();
        stocksYC = new HashMap();
        stocksExCh = new HashMap();
        FLAG_RENEW_LISTVIEW_UI = false;

        lis_hotStocksList = findViewById(R.id.lis_hotStocksList);

        adapter = new listLayoutAdapter(this);
        lis_hotStocksList.setAdapter(adapter);

        lis_hotStocksList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toStockActivity(stocksExCh.get(rankList[position]).toString());
            }
        });
    }

    private void toStockActivity(String exCh) {
        Intent intent = new Intent(HotStocksActivity.this, StockActivity.class);
        intent.putExtra("user", USER);
        intent.putExtra("password", PASSWORD);
        intent.putExtra("ticker", exCh);
        startActivity(intent);

    }

    private void initIntent() {
        Intent intent = getIntent();
        USER = intent.getStringExtra("user");
        PASSWORD = intent.getStringExtra("password");
    }

    private  void initSpinner(){
        Toast.makeText(HotStocksActivity.this, "等待連線，取得資料中...", Toast.LENGTH_SHORT).show();
        choiceType = new String[]{"交易額", "成交量", "漲幅", "跌幅"};

        spr_hotStocks_choiceType = findViewById(R.id.spr_hotStocks_choiceType);

        adapterChoiceType = new ArrayAdapter<>(HotStocksActivity.this, android.R.layout.simple_spinner_dropdown_item, choiceType);
        spr_hotStocks_choiceType.setAdapter(adapterChoiceType);
        spr_hotStocks_choiceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(HotStocksActivity.this, "您選擇了:" + choiceType[position], Toast.LENGTH_SHORT).show();

                if (choiceType[position].equals("交易額")){
                    renewTwHotStockTurnoverRankMap();
                }
                else if (choiceType[position].equals("成交量")){
                    renewTwHotStockVolumeRankMap();
                }
                else if (choiceType[position].equals("漲幅")){
                    renewTwHotStockChangeUpRankMap();
                }
                else if (choiceType[position].equals("跌幅")){
                    renewTwHotStockChangeDownRankMap();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 初始化為 交易額RANK
        initSpinnerFromTurnoverRank();
    }
    private void initSpinnerFromTurnoverRank() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int c = 0;
                while (!FLAG_RENEW_LISTVIEW_UI){
                    c+=1;
                    Log.d("zha", "c"+c);
                    try {
                        Thread.sleep(1000 + (int) (Math.random() * 500));
                        if (FLAG_RENEW_LISTVIEW_UI){
                            Thread.sleep(2000);
                            Log.d("zha", "c s"+c);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (c > 5){
                        break;
                    }
                }
                Log.d("zha", "renewTwHotStockTurnoverRankMap  "+c);
                HotStocksActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        renewTwHotStockTurnoverRankMap();
                    }
                });
            }
        }).start();
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

                            getTwseStocksInfo(twTurnoverRank);
                            Log.d("zha", "twTurnoverRank");
                            getTwseStocksInfo(twVolumeRank);
                            Log.d("zha", "twVolumeRank");
                            getTwseStocksInfo(twChangeUpRank);
                            Log.d("zha", "twChangeUpRank");
                            getTwseStocksInfo(twChangeDownRank);
                            Log.d("zha", "twChangeDownRank");


                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    private void renewTwHotStockTurnoverRankMap() {
        Log.d("zha", "twTurnoverRank    " + twTurnoverRank);
        if (FLAG_RENEW_LISTVIEW_UI) {
            rankList = twTurnoverRank.split("\\|");
            adapter = new listLayoutAdapter(this);
            lis_hotStocksList.setAdapter(adapter);
        }
    }

    private void renewTwHotStockChangeDownRankMap() {
        Log.d("zha", "twChangeDownRank    " + twChangeDownRank);
        if (FLAG_RENEW_LISTVIEW_UI) {
            rankList = twChangeDownRank.split("\\|");
            adapter = new listLayoutAdapter(this);
            lis_hotStocksList.setAdapter(adapter);
        }
    }

    private void renewTwHotStockChangeUpRankMap() {
        Log.d("zha", "twChangeUpRank    " + twChangeUpRank);
        if (FLAG_RENEW_LISTVIEW_UI) {
            rankList = twChangeUpRank.split("\\|");
            adapter = new listLayoutAdapter(this);
            lis_hotStocksList.setAdapter(adapter);
        }
    }

    private void renewTwHotStockVolumeRankMap() {
        Log.d("zha", "twVolumeRank    " + twVolumeRank);
        if (FLAG_RENEW_LISTVIEW_UI) {
            rankList = twVolumeRank.split("\\|");

            adapter = new listLayoutAdapter(this);
            lis_hotStocksList.setAdapter(adapter);
        }
    }


    private void getTwseStocksInfo(String str) {
        FLAG_RENEW_LISTVIEW_UI = false;
        OkHttpClient client = new OkHttpClient();
        String url = "https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch="+str+"&json=1&delay=0";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("zha", "failed onFailure");
                FLAG_RENEW_LISTVIEW_UI = false;
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();
                    Log.d("zha","myResponse："+myResponse);
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
                                        FLAG_RENEW_LISTVIEW_UI = true;
                                    }
                                }

                            } catch (JSONException e) {
                                Log.d("zha", "Failed JSONException" + e.toString());
                                FLAG_RENEW_LISTVIEW_UI = false;
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
        stocksExCh.put(ticker, data.getString("ex")+"_"+data.getString("c"));
        stocksName.put(ticker, data.getString("n"));
        stocksTicker.put(ticker, data.getString("c"));
        if (!data.getString("z").equals("-")){
            stocksPrice.put(ticker, data.getString("z").substring(0, data.getString("z").length()-2));
        }
        else if (!data.getString("b").equals("-")){
            stocksPrice.put(ticker, data.getString("b").split("_")[0].substring(0, data.getString("b").split("_")[0].length()-2));
        }
        else if (data.getString("b").equals("-")){
            String sP = data.getString("a").split("_")[0].substring(0, data.getString("a").split("_")[0].length()-2);
            if (Float.valueOf(data.getString("a").split("_")[0].substring(0, data.getString("a").split("_")[0].length()-2)) == 0){
                sP = data.getString("a").split("_")[1].substring(0, data.getString("a").split("_")[1].length()-2);
            }
            stocksPrice.put(ticker, sP);
        }
        Float p = Float.parseFloat(stocksPrice.get(ticker).toString());
        Float yc = Float.parseFloat(data.getString("y"));
        stocksChange.put(ticker, String.format("%.02f", (p-yc)/yc*100));
        stocksVolume.put(ticker, formatIntToKMStr(Integer.valueOf(data.getString("v"))));
        stocksHigh.put(ticker, data.getString("h").substring(0, data.getString("h").length()-2));
        stocksLow.put(ticker, data.getString("l").substring(0, data.getString("l").length()-2));
        stocksYC.put(ticker, data.getString("y").substring(0, data.getString("y").length()-2));
    }

    private String formatIntToKMStr(int i){
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

    private String formatIntToCommaStr(float num){
        DecimalFormat df = new DecimalFormat("#,###.00");
        return df.format(num);
    }

    public void clickTalk(View view) {
        Intent intent = new Intent(HotStocksActivity.this, TalkActivity.class);
        intent.putExtra("user", USER);
        intent.putExtra("password", PASSWORD);
        startActivity(intent);
        clickClose(view);
    }

    public void clickHotStocks(View view) {
        Log.d("zha", formatIntToCommaStr(100000000));
        Log.d("zha", formatIntToCommaStr(100000000));

    }

    public void clickClose(View view) {
        finish();
        FLAG_RENEW_LISTVIEW_UI = false;
    }

    public void clickTest(View view) {

        adapter = new listLayoutAdapter(this);
        lis_hotStocksList.setAdapter(adapter);
    }
}