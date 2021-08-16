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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    Spinner spr_details_choiceRange;
    ArrayAdapter<String> adapterRangeTime;
    String [] rangeTime;
    int [] irangeTime;
    int currentRangeTimeIndex = 0;
    private String[] from = {"time", "ticker", "type", "ap", "lots", "income", "id", "detail"};
    private int[] to = {R.id.txv_detailItemTime, R.id.txv_detailItem_name, R.id.txv_detailItem_type, R.id.txv_detailItem_ap, R.id.txv_detailItem_lot, R.id.txv_detailItem_income, 945300, 123};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        initListView();
        initIntent();
        initSpinner();
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
                Log.d("zha", datalist.get((int)id).toString());
                HashMap<String, String> data = datalist.get((int)id);
                showDetails(data);
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

    private  void initSpinner(){
        rangeTime = new String[]{"今天", "近三天", "近月", "近季", "近年"};
        irangeTime = new int[]{ 1, 3, 31, 90, 365};

        Log.d("zha", "111");

        adapterRangeTime = new ArrayAdapter<>(DetailsActivity.this, android.R.layout.simple_spinner_dropdown_item, rangeTime);
        Log.d("zha", "112");
        spr_details_choiceRange = findViewById(R.id.spr_details_choiceRange);
        Log.d("zha", "113");
        spr_details_choiceRange.setAdapter(adapterRangeTime);
        Log.d("zha", "114");
        spr_details_choiceRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FLAG_GETORDER_PERMISSION = true;
                currentRangeTimeIndex = (int)id;
                Toast.makeText(DetailsActivity.this, "您選擇了:" + rangeTime[position], Toast.LENGTH_SHORT).show();
                connectSeverGetOrder();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getDetailsThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (FLAG_GETORDER_PERMISSION) {
                    connectSeverGetOrder();
                    txv_detailsRenewTime.setText("更新時間： 連線中...");
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

    public static int CompareDaysOfTwo(String sdate) throws ParseException {
        sdate = sdate.substring(0, 10);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse(sdate);
        Calendar aCalendar = Calendar.getInstance();
        aCalendar.setTime(date);
        int idate = aCalendar.get(Calendar.DAY_OF_YEAR);
        Date today = new Date();
        aCalendar.setTime(today);
        int idaytodate = aCalendar.get(Calendar.DAY_OF_YEAR);

        return idaytodate - idate;
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
                            } catch (ParseException e) {
                                e.printStackTrace();
                                Log.d("zha", "ParseException Failed ");
                            }
                        }
                    });
                }
            }
        });
    }

    public void addElementToDetailsList(JSONObject details, String id, String dayTime) throws JSONException, ParseException {
        Log.d("zha", "11112");
        if (CompareDaysOfTwo(dayTime) < irangeTime[currentRangeTimeIndex]){
            HashMap<String, String> h_details = new HashMap<>();
            Log.d("zha", details.toString());

            h_details.put(from[0], dayTime);
            Log.d("zha", details.toString());
            h_details.put(from[1], details.getString("name"));
            Log.d("zha", details.toString());
            h_details.put(from[2], details.getString("type"));
            Log.d("zha", details.toString());
            h_details.put(from[3], String.valueOf(details.getDouble("a_p")));
            h_details.put(from[4], String.valueOf(details.getInt("totalLots")));
            h_details.put(from[5], String.valueOf(details.getInt("income")));
            h_details.put(from[6], id);

            String connent = "";
            JSONObject detail = details.getJSONObject("detail");
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

            datalist.add(h_details);
            Log.d("zha", h_details.toString());
            adapter.notifyDataSetInvalidated();
        }
    }


    private void showDetails(HashMap<String, String> data){
        Log.d("zha", data.toString());
        String content = "委託：" +data.get("type") + " " +data.get("ticker")+"\n"+ "委託時間：" +data.get("time") + "\n" +"成交數："+data.get("lots")+"\n"+data.get("detail");
        new AlertDialog.Builder(DetailsActivity.this)
                .setTitle(data.get("id") + " 細項")
                .setMessage(content)
                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("zha", "確認?");
                    }
                }).show();
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

    public void clickOrder(View view) {
        Intent intent = new Intent(DetailsActivity.this, OrderActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        intent.putExtra("from", "TradingFuturesActivity");
        startActivityForResult(intent, 2);
        clickClose(view);

    }
    public void test(View view) {

    }

    public void clickPortfolio(View view) {

        Intent intent = new Intent(DetailsActivity.this, InStockActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        startActivity(intent);
        clickClose(view);
    }
}