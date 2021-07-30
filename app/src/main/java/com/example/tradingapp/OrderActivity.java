package com.example.tradingapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
    private LinkedList<HashMap<String, String>> datalist;
    private String[] from = {"state", "name", "orderPrice", "finalPrice", "lots", "id", "type"};
    private int[] to = {R.id.btn_orderItem_del, R.id.txv_orderItem_name, R.id.txv_orderItem_orderPrice, R.id.txv_orderItem_finalPrice, R.id.txv_orderItem_lot, 945300, 945300};

    private String USER, PASSWORD, activityFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        lis_orderList = findViewById(R.id.lis_orderList);
        initIntent();
        initListView();

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
                if (!data.get("state").equals("刪")){
                    showDelOrderSure(data);
                }
                else{
                    showOrderDetails(data);
                }
            }
        });

    }

    private void showDelOrderSure(HashMap<String, String> data){
        String content = "委託單號：" + data.get("id") + "\n" + "委託動作：" +data.get("type")  + "當前狀態：" +data.get("state") + "\n" + "股票名稱："+data.get("name")+"\n"
                + "委託價：" + data.get("price") + "\n" + "委託量：" +data.get("lots") + "\n" +"如要刪單，請按確認。";
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
                .setNeutralButton("?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("zha", "??");
                    }
                })
                .show();
    }

    private void showOrderDetails(HashMap<String, String> data){
        String content = "委託單號：" + data.get("id") + "\n" + "委託動作：" +data.get("type") + "\n" + "委託股票：" +data.get("name")+"\n"
                + "委託價：" + data.get("price") + "\n" + "委託量：" +data.get("lots") + "\n\n";
        new AlertDialog.Builder(OrderActivity.this)
                .setTitle("Detail")
                .setMessage(content)
                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("zha", "確認?");
                        connectSeverDelOrder(data.get("id"));
                    }
                }).show();
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
                    Log.d("zha",myResponse);
                    OrderActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = new JSONObject(myResponse);
                                Log.d("zha", data.length()+"");
                                Iterator iterator = data.keys();
                                datalist.clear();
                                while (iterator.hasNext()){
                                    String key = (String)iterator.next();
                                    JSONObject order = data.getJSONObject(key);
                                    addElementToOrderList(order, key);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("zha", "failed myResponse");
                            }
                        }
                    });
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
                    Log.d("zha", myResponse);
                    connectSeverGetOrder();
                }
            }
        });
    }


    public void test(View view) {
    }

    public void addElementToOrderList(JSONObject order, String id) throws JSONException {
        HashMap<String, String> h_order = new HashMap<>();

        h_order.put(from[0], order.getString("state"));
        h_order.put(from[1], order.getString("name"));
        h_order.put(from[2], String.valueOf(order.getDouble("price")));
        h_order.put(from[4], String.valueOf(order.getInt("lots")));
        h_order.put(from[5], id);
        h_order.put(from[6], order.getString("type"));

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
        h_order.put(from[3], String.valueOf((m/v)));

        datalist.add(h_order);
        Log.d("zha", h_order.toString());
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
}