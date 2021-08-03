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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DetailsActivity extends AppCompatActivity {

    String user, password, activityFrom;
    private SimpleAdapter adapter;
    private boolean FLAG_GETORDER_PERMISSION = false;
    private LinkedList<HashMap<String, String>> datalist;
    private ListView list_detailsList;
    private TextView txv_detailsRenewTime;
    private String[] from = {"time", "ticker", "type", "ap", "lots", "income", "id", "detail"};
    private int[] to = {R.id.txv_detailItemTime, R.id.txv_detailItem_name, R.id.txv_detailItem_type, R.id.txv_detailItem_ap, R.id.txv_detailItem_lot, R.id.txv_detailItem_income, 945300, 123};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        initListView();
        initIntent();
        getDetailsThread();
    }

    private void initListView(){
        txv_detailsRenewTime = findViewById(R.id.txv_detailsRenewTime);
        list_detailsList = findViewById(R.id.list_detailsList);
        datalist = new LinkedList<>();
        adapter = new SimpleAdapter(this, datalist, R.layout.item_detailsitem, from, to);
        list_detailsList.setAdapter(adapter);

        list_detailsList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView txv = (TextView) view.findViewById(R.id.btn_orderItem_del);
//                Toast.makeText(OrderActivity.this, "你點擊了:" + txv.getText(), Toast.LENGTH_SHORT).show();
                Log.d("zha", datalist.get((int)id).toString());
                HashMap<String, String> data = datalist.get((int)id);
                if (data.get("state").equals("委")){
//                    showDelOrderSure(data);
                }
                else if (!data.get("state").equals("刪")){
//                    showOrderDetails(data);
                }
            }
        });


        FLAG_GETORDER_PERMISSION = true;
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

    private void getDetailsThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (FLAG_GETORDER_PERMISSION) {
                    connectSeverGetOrder();
                    Log.d("zha", "getDetailsThread do end");
                    try {
                        Thread.sleep(10000 + (int) (Math.random() * 110000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d("zha", "getDetailsThread Failed ");
                    }
                }
            }
        }).start();
    }



    private void connectSeverGetOrder() {
        OkHttpClient client = new OkHttpClient();

        String parameter = "user="+user+"&password="+password;
        String url = "https://tradingAppServer.masterrongwu.repl.co/getDetails?"+parameter;
        Log.d("zha", url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("zha", "failed onFailure");
                e.printStackTrace();
                DetailsActivity.this.runOnUiThread(new Runnable() {
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
                    DetailsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Calendar mCal = Calendar.getInstance();
                                CharSequence s = DateFormat.format("MM-dd kk:mm", mCal.getTime());    // kk:24小時制, hh:12小時制
                                txv_detailsRenewTime.setText("更新時間："+s);

                                JSONObject data = new JSONObject(myResponse);
                                Log.d("zha", data.length()+"");
                                Iterator iterator = data.keys();
                                datalist.clear();
                                while (iterator.hasNext()){
                                    String dayTime = (String)iterator.next();
                                    JSONObject day = data.getJSONObject(dayTime);

                                    Iterator iteratori = day.keys();
                                    while (iteratori.hasNext()) {
                                        String id = (String)iteratori.next();
                                        Log.d("zha", id);
                                        JSONObject detail = day.getJSONObject(id);
                                        addElementToDetailsList(detail, id, dayTime);
                                    }
                                }
                                FLAG_GETORDER_PERMISSION = true;
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

    public void addElementToDetailsList(JSONObject details, String id, String dayTime) throws JSONException {
        HashMap<String, String> h_details = new HashMap<>();
        Log.d("zha", details.toString());

        h_details.put(from[0], dayTime);
        Log.d("zha", "11112");
        Log.d("zha", details.getString("ticker"));
        Log.d("zha", dayTime);
        h_details.put(from[1], details.getString("ticker"));
        Log.d("zha", "11113");
        h_details.put(from[2], details.getString("type"));
        Log.d("zha", "11114");
        h_details.put(from[3], String.valueOf(details.getDouble("a_p")));
        Log.d("zha", "11116");
        h_details.put(from[4], String.valueOf(details.getInt("totalLots")));
        Log.d("zha", "11117");
        h_details.put(from[5], String.valueOf(details.getInt("income")));
        Log.d("zha", "11118");
        h_details.put(from[6], id);

        Log.d("zha", "11119");
        String connent = "";
        JSONObject detail = details.getJSONObject("detail");
        Log.d("zha", "11120");
        if (detail.length() > 0) {
            Iterator iterator = detail.keys();
            while (iterator.hasNext()) {
                String time = (String) iterator.next();
                connent += time+"："+details.getJSONObject("detail").getString(time)+"\n";
            }
        }
        else{
            connent = "NO Detail";
        }
        h_details.put(from[7], connent);
        Log.d("zha", "11121");

        datalist.add(h_details);
        Log.d("zha", h_details.toString());
        adapter.notifyDataSetInvalidated();
    }


    public void clickClose(View view) {
        FLAG_GETORDER_PERMISSION = false;
        finish();
    }

    public void clickTrading(View view) {
        if (activityFrom.equals("StockActivity")){
            Intent intent = new Intent(DetailsActivity.this, StockActivity.class);
            intent.putExtra("user", user);
            intent.putExtra("password", password);
            startActivityForResult(intent, 1);
            clickClose(view);
        }
        else if (activityFrom.equals("TradingFuturesActivity")){
            Intent intent = new Intent(DetailsActivity.this, TradingFuturesActivity.class);
            intent.putExtra("user", user);
            intent.putExtra("password", password);
            startActivityForResult(intent, 1);
            clickClose(view);
        }
        else{
            clickClose(view);
        }

    }

    public void test(View view) {

    }
}