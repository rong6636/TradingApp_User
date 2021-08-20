package com.example.tradingapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OrderActivity extends AppCompatActivity {
    boolean FLAG_GETORDER_PERMISSION = true;

    private SimpleAdapter adapter;
    private ListView lis_orderList;
    private TextView txv_orderRenewTime;
    private LinkedList<HashMap<String, String>> datalist;
    private String[] from = {"state", "name", "type", "orderPrice", "finalPrice", "lots", "id", "time", "cover_lots", "new_lots"};
    private int[] to = {R.id.btn_orderItem_del, R.id.txv_orderItem_name, R.id.txv_orderItem_type, R.id.txv_orderItem_orderPrice, R.id.txv_orderItem_finalPrice, R.id.txv_orderItem_lot, 945300, 945300};

    private String USER, PASSWORD, activityFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        lis_orderList = findViewById(R.id.lis_orderList);
        initListView();

        initIntent();
        getOrderThread();
    }

    private void initIntent() {
        Intent intent = getIntent();
        USER = intent.getStringExtra("user");
        PASSWORD = intent.getStringExtra("password");
        activityFrom = intent.getStringExtra("from");
        if (activityFrom == null){
            activityFrom = "";
        }
    }

    private void initListView(){
        txv_orderRenewTime = findViewById(R.id.txv_orderRenewTime);
        datalist = new LinkedList<>();
        adapter = new SimpleAdapter(this, datalist, R.layout.item_orderitem, from, to);
        lis_orderList.setAdapter(adapter);

        lis_orderList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView txv = (TextView) view.findViewById(R.id.btn_orderItem_del);
//                Toast.makeText(OrderActivity.this, "你點擊了:" + txv.getText(), Toast.LENGTH_SHORT).show();
                Log.d("zha", datalist.get((int)id).toString());
                HashMap<String, String> data = datalist.get((int)id);
                if (data.get("state").equals("委"))
                    showOrderDetail(data);
                else
                    showOrderDetail_2(data);
            }
        });
    }

    private void showOrderDetail(HashMap<String, String> data){
        String content = "委託：" +data.get("type")+" "+data.get("name")+ "\n委託時間：" + data.get("time") + "\n當前狀態：" +data.get("state") + "\n"
                + "委託價：" + data.get("orderPrice") + "\n" + "委託量：" + data.get("lots") + "\n"
                + "已成交量：" +(Integer.valueOf(data.get("lots")) - Integer.valueOf(data.get("new_lots")) - Integer.valueOf(data.get("cover_lots"))) + "\n"
                + "未成交量：" +(Integer.valueOf(data.get("new_lots")) + Integer.valueOf(data.get("cover_lots"))) + "\n";
        new AlertDialog.Builder(OrderActivity.this)
                .setTitle("委託書號：" + data.get("id"))
                .setMessage(content)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("zha", "OK?");
                    }
                })
                .setNegativeButton("刪單", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("zha", "刪單?");
                        showOrderDelSure(data);
                    }
                })
                .show();
    }

    private void showOrderDetail_2(HashMap<String, String> data){
        String content = "委託：" +data.get("type")+" "+data.get("name") + "\n當前狀態：" +data.get("state") + "\n"
                + "委託價：" + data.get("orderPrice") + "\n" + "委託量：" +data.get("lots") + "\n"+ "委託時間：" +data.get("time") + "\n";
        new AlertDialog.Builder(OrderActivity.this)
                .setTitle("委託書號：" + data.get("id"))
                .setMessage(content)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("zha", "OK?");
                    }
                })
                .show();
    }

    private void showOrderDelSure(HashMap<String, String> data){
        String content = "委託單號：" + data.get("id") + "\n" + "委託動作：" +data.get("type")  + "\n當前狀態：" +data.get("state") + "\n" + "股票名稱："+data.get("name")+"\n"
                + "委託價：" + data.get("orderPrice") + "\n" + "委託量：" +data.get("lots") + "\n" + "委託時間：" +data.get("time") + "\n" +"如要刪單，請按確認。";
        new AlertDialog.Builder(OrderActivity.this)
                .setTitle("動作：刪單")
                .setMessage(content)
                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("zha", "確認?");
                        connectSeverDelOrder(data.get("id"));
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

    public void close(View view) {
        FLAG_GETORDER_PERMISSION = false;
        finish();
    }

    private void getOrderThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (FLAG_GETORDER_PERMISSION) {
                    connectSeverGetOrder();
                    txv_orderRenewTime.setText("更新時間： 連接中...");
                    Log.d("zha", "getOrderThread do end");
                    try {
                        Thread.sleep(10000 + (int) (Math.random() * 110000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d("zha", "getOrderThread Failed ");
                    }
                }
            }
        }).start();
    }

    private void connectSeverGetOrder() {
        OkHttpClient client = new OkHttpClient();

        String parameter = "user="+USER+"&password="+PASSWORD;
        String url = "https://tradingAppServer.masterrongwu.repl.co/getOrder?"+parameter;
        Log.d("zha", url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("zha", "failed onFailure");
                e.printStackTrace();
                OrderActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();
                    OrderActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Calendar mCal = Calendar.getInstance();
                                CharSequence s = DateFormat.format("MM-dd kk:mm", mCal.getTime());    // kk:24小時制, hh:12小時制
                                txv_orderRenewTime.setText("更新時間："+s);

                                JSONObject req = new JSONObject(myResponse);
                                JSONArray data = req.getJSONArray("orderlist");
                                datalist.clear();

                                for (int i = 0; i< data.length(); i++)
                                {
                                    JSONObject order = (JSONObject) data.get(i);

                                    addElementToOrderList(order, order.getString("key"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("zha", "failed myResponse");
                            }
                        }
                    });
                }
                else{
                    Log.d("zha", "failed onResponse");
                }
            }
        });
    }


    private void connectSeverDelOrder(String orderId) {
        OkHttpClient client = new OkHttpClient();

        String parameter = "user="+USER+"&password="+PASSWORD+"&orderId="+orderId;
        String url = "https://tradingAppServer.masterrongwu.repl.co/delOrder?"+parameter;
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

                    if (myResponse.indexOf("忙碌")>=0){
                        Log.d("zha", "del 忙碌中");
                    }
                    if(myResponse.equals(orderId)) {
                        Log.d("zha", myResponse);
                        connectSeverGetOrder();
                    }
                }
            }
        });
    }


    public void test(View view) {
    }

    public void addElementToOrderList(JSONObject order, String id) throws JSONException {
        HashMap<String, String> h_order = new HashMap<>();

        Log.d("zha", order.toString())  ;
        h_order.put(from[0], order.getString("state"));
        h_order.put(from[1], order.getString("name"));
        h_order.put(from[2], order.getString("type"));
        h_order.put(from[3], String.valueOf(order.getDouble("price")));
        h_order.put(from[5], String.valueOf(order.getInt("lots")));
        h_order.put(from[6], id);
        h_order.put(from[7], order.getString("time"));
        h_order.put(from[8], order.getString("cover_lots"));
        h_order.put(from[9], order.getString("new_lots"));

        int m = 0, v = 0;
        JSONObject pv = order.getJSONObject("p_v");
        if (pv.length() > 0) {
            Iterator iterator = pv.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                v += order.getJSONObject("p_v").getInt(key);
                m += Double.valueOf(key) * order.getJSONObject("p_v").getInt(key);
            }
        }
        else{
            v = 1;
        }
        h_order.put(from[4], String.valueOf((m/v)));

        datalist.add(h_order);
        adapter.notifyDataSetInvalidated();
    }

    public void clickTrading(View view) {
        if (activityFrom.equals("StockActivity")){
            Intent intent = new Intent(OrderActivity.this, StockActivity.class);
            intent.putExtra("user", USER);
            intent.putExtra("password", PASSWORD);
            startActivityForResult(intent, 1);
            close(view);

        }
        else if (activityFrom.equals("TradingFuturesActivity")){
            Intent intent = new Intent(OrderActivity.this, TradingFuturesActivity.class);
            intent.putExtra("user", USER);
            intent.putExtra("password", PASSWORD);
            startActivityForResult(intent, 1);
            close(view);

        }
        else{
            close(view);
        }
    }

    public void clickDetail(View view) {
        Intent intent = new Intent(OrderActivity.this, DetailsActivity.class);
        intent.putExtra("user", USER);
        intent.putExtra("password", PASSWORD);
        startActivity(intent);
        close(view);

    }

    public void clickPortfolio(View view) {
        Intent intent = new Intent(OrderActivity.this, InStockActivity.class);
        intent.putExtra("user", USER);
        intent.putExtra("password", PASSWORD);
        startActivity(intent);
        close(view);

    }
}