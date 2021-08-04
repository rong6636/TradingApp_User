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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class portfolioActivity extends AppCompatActivity {

    String user, password, activityFrom;
    private SimpleAdapter adapter;
    private boolean FLAG_RENEW_PERMISSION = false;
    private LinkedList<HashMap<String, String>> datalist;
    private ListView list_portfolioList;
    private TextView txv_portfolioRenewTime;

    private String[] from = {"ticker", "price", "ap", "lots", "type", "income"};
    private int[] to = {R.id.txv_portfolioItem_name, R.id.txv_portfolioItem_price, R.id.txv_portfolioItem_ap, R.id.txv_portfolioItem_lot, R.id.txv_portfolioItem_type, R.id.txv_portfolioItem_income};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        initListView();
        initIntent();
        getPortfolioThread();
    }

    private void initListView(){
        txv_portfolioRenewTime = findViewById(R.id.txv_portfolioRenewTime);
        list_portfolioList = findViewById(R.id.list_portfolioList);
        datalist = new LinkedList<>();
        adapter = new SimpleAdapter(this, datalist, R.layout.item_portfolioitem, from, to);
        list_portfolioList.setAdapter(adapter);

        list_portfolioList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
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

    private void getPortfolioThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (FLAG_RENEW_PERMISSION) {
                    connectSeverGetPortfolio();
                    Log.d("zha", "getPortfolioThread do end");
                    try {
                        Thread.sleep(1000 + (int) (Math.random() * 5000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d("zha", "getPortfolioThread Failed ");
                    }
                }
            }
        }).start();
    }

    private void showDetails(HashMap<String, String> data){
        Log.d("zha", data.toString());
        String content = "委託：" +data.get("type") + " " +data.get("ticker")+"\n"+"成交數："+data.get("lots")+"\n"+data.get("detail");
        new AlertDialog.Builder(portfolioActivity.this)
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
                portfolioActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();
                    Log.d("zha", "onResponse"+myResponse);
                    portfolioActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Calendar mCal = Calendar.getInstance();
                                CharSequence s = DateFormat.format("MM-dd kk:mm", mCal.getTime());    // kk:24小時制, hh:12小時制
                                txv_portfolioRenewTime.setText("更新時間："+s);

                                JSONObject data = new JSONObject(myResponse);
                                Log.d("zha", data.length()+"");

                                JSONObject twStocks = data.getJSONObject("twStocks");
                                JSONObject twFutures = data.getJSONObject("twFutures");

                                Iterator it_twStocks = twStocks.keys();
                                Iterator it_twFutures = twFutures.keys();
                                datalist.clear();

                                while (it_twStocks.hasNext()){
                                    Log.d("zha", "1111");
                                    String stock = (String)it_twStocks.next();
                                    Log.d("zha", "1112");
                                    JSONObject jStock = twStocks.getJSONObject(stock);
                                    Log.d("zha", "1113");

                                    addElementToPortfolioList(jStock, stock);
                                    Log.d("zha", "1114");
                                }

                                while (it_twFutures.hasNext()){
                                    Log.d("zha", "2111");
                                    String futures = (String)it_twFutures.next();
                                    Log.d("zha", "2112");
                                    JSONObject jFutures = twFutures.getJSONObject(futures);
                                    Log.d("zha", "2113");

                                    addElementToPortfolioList(jFutures, futures);
                                    Log.d("zha", "2114");
                                }
                                FLAG_RENEW_PERMISSION = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("zha", "failed myResponse");
                                txv_portfolioRenewTime.setText("回傳格式出錯");
                            } catch (ParseException e) {
                                e.printStackTrace();
                                Log.d("zha", "ParseException Failed ");
                                txv_portfolioRenewTime.setText("ParseException");
                            }
                        }
                    });
                }
            }
        });
    }

    public void addElementToPortfolioList(JSONObject data, String name) throws JSONException, ParseException {
        Log.d("zha", "11112");

        if (Math.abs(data.getInt("Lots"))>0)
        {
            HashMap<String, String> h_portfolio = new HashMap<>();
            Log.d("zha", data.toString());

            h_portfolio.put(from[0], name);
            h_portfolio.put(from[1], "市價未開放");
            h_portfolio.put(from[2], data.getString("Average_Price"));
            h_portfolio.put(from[3], String.valueOf(Math.abs(data.getInt("Lots"))));
            if (data.getInt("Lots") > 0) {
                h_portfolio.put(from[4], "買");
            } else {
                h_portfolio.put(from[4], "賣");
            }

            h_portfolio.put(from[5], "損益未開放");


            datalist.add(h_portfolio);
            Log.d("zha", h_portfolio.toString());
            adapter.notifyDataSetInvalidated();
        }

    }




    public void clickClose(View view) {
        FLAG_RENEW_PERMISSION = false;
        finish();

    }

    public void clickTrading(View view) {
        if (activityFrom.equals("StockActivity")){
            Intent intent = new Intent(portfolioActivity.this, StockActivity.class);
            intent.putExtra("user", user);
            intent.putExtra("password", password);
            startActivityForResult(intent, 1);
            clickClose(view);
        }
        else if (activityFrom.equals("TradingFuturesActivity")){
            Intent intent = new Intent(portfolioActivity.this, TradingFuturesActivity.class);
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
        Intent intent = new Intent(portfolioActivity.this, OrderActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        intent.putExtra("from", "TradingFuturesActivity");
        startActivityForResult(intent, 2);
        clickClose(view);
    }

    public void clickDetail(View view) {
        Intent intent = new Intent(portfolioActivity.this, DetailsActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        startActivity(intent);
        clickClose(view);
    }

    public void test(View view) {
    }
}