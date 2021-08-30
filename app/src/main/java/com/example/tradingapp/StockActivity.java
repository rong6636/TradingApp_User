package com.example.tradingapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Vibrator;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StockActivity extends AppCompatActivity {
    TextView txv_stock_total_bid, txv_stock_total_ask, txv_stock_titleType, txv_stock_titleName, txv_stock_titlePrice, txv_stock_titleChange, txv_stock_renewTime,
            txv_stock_orderPriceSub, txv_stock_orderPriceChoose, txv_stock_orderPriceAdd, txv_stock_orderLotSub, txv_stock_orderLotChoose, txv_stock_orderLotAdd, txv_stcok_showBudget;
    TextView[] txv_stock_bidlot = new TextView[5], txv_stock_bidprice = new TextView[5], txv_stock_asklot = new TextView[5], txv_stock_askprice = new TextView[5];
    EditText edt_stock_searchstock;
    Button btn_stock_changeStockType;
    Boolean FLAG_TRADING_PERMISSION = false;
    Boolean FLAG_SEARCH_STOCK_PERMISSION = true;

    String orderStock, USER, PASSWORD, orderName;
    float orderPrice = 0, downX,downY,upX,upY;
    int orderLot = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        initIntent();
        iniTextView();
        searchStockThread();
    }

    public boolean onTouchEvent(MotionEvent event) {

        float X = event.getX(); // 觸控的 X 軸位置
        float Y = event.getY(); // 觸控的 Y 軸位置

        switch (event.getAction()) { // 判斷觸控的動作

            case MotionEvent.ACTION_DOWN: // 按下
                downX = event.getX();
                downY = event.getY();

                return true;
            case MotionEvent.ACTION_MOVE: // 拖曳

                return true;
            case MotionEvent.ACTION_UP: // 放開
                Log.d("onTouchEvent-ACTION_UP","UP");
                upX = event.getX();
                upY = event.getY();
                float x=Math.abs(upX-downX);
                float y=Math.abs(upY-downY);
                double z=Math.sqrt(x*x+y*y);
                int jiaodu=Math.round((float)(Math.asin(y/z)/Math.PI*180));//角度

                if (upY < downY && jiaodu>45) {//上
                    Log.d("onTouchEvent-ACTION_UP","角度:"+jiaodu+", 動作:上");
                }else if(upY > downY && jiaodu>45) {//下
                    Log.d("onTouchEvent-ACTION_UP","角度:"+jiaodu+", 動作:下");
                }
                return true;
        }

        return super.onTouchEvent(event);
    }


    private void initIntent() {
        Intent intent = getIntent();
        USER = intent.getStringExtra("user");
        PASSWORD = intent.getStringExtra("password");
        orderStock = intent.getStringExtra("ticker");
        if (orderStock == null) {
            orderStock="tse_2603";
        }
        orderStock.replace(".tw", "");
    }

    private void iniTextView() {
        edt_stock_searchstock = findViewById(R.id.edt_stock_searchstock);
        edt_stock_searchstock.setImeActionLabel("Custom text", KeyEvent.KEYCODE_ENTER);
        edt_stock_searchstock.setText(orderStock.substring(4));
        txv_stock_titleType = findViewById(R.id.txv_stock_titleType);
        txv_stock_titleName = findViewById(R.id.txv_stock_titleName);
        txv_stock_titlePrice = findViewById(R.id.txv_stock_titlePrice);
        txv_stock_titleChange = findViewById(R.id.txv_stock_titleChange);

        btn_stock_changeStockType = findViewById(R.id.btn_stock_changeStockType);

        txv_stock_total_bid = findViewById(R.id.txv_stock_total_bid);
        txv_stock_total_ask = findViewById(R.id.txv_stock_total_ask);

        txv_stock_bidlot[0] = findViewById(R.id.txv_stock_bidlot_1);
        txv_stock_bidprice[0] = findViewById(R.id.txv_stock_bidprcie_1);
        txv_stock_bidlot[1] = findViewById(R.id.txv_stock_bidlot_2);
        txv_stock_bidprice[1] = findViewById(R.id.txv_stock_bidprcie_2);
        txv_stock_bidlot[2] = findViewById(R.id.txv_stock_bidlot_3);
        txv_stock_bidprice[2] = findViewById(R.id.txv_stock_bidprcie_3);
        txv_stock_bidlot[3] = findViewById(R.id.txv_stock_bidlot_4);
        txv_stock_bidprice[3] = findViewById(R.id.txv_stock_bidprcie_4);
        txv_stock_bidlot[4] = findViewById(R.id.txv_stock_bidlot_5);
        txv_stock_bidprice[4] = findViewById(R.id.txv_stock_bidprcie_5);

        txv_stock_asklot[0] = findViewById(R.id.txv_stock_asklot_1);
        txv_stock_askprice[0] = findViewById(R.id.txv_stock_askprcie_1);
        txv_stock_asklot[1] = findViewById(R.id.txv_stock_asklot_2);
        txv_stock_askprice[1] = findViewById(R.id.txv_stock_askprcie_2);
        txv_stock_asklot[2] = findViewById(R.id.txv_stock_asklot_3);
        txv_stock_askprice[2] = findViewById(R.id.txv_stock_askprcie_3);
        txv_stock_asklot[3] = findViewById(R.id.txv_stock_asklot_4);
        txv_stock_askprice[3] = findViewById(R.id.txv_stock_askprcie_4);
        txv_stock_asklot[4] = findViewById(R.id.txv_stock_asklot_5);
        txv_stock_askprice[4] = findViewById(R.id.txv_stock_askprcie_5);
        txv_stock_renewTime = findViewById(R.id.txv_stock_renewTime);

        txv_stock_orderPriceSub = findViewById(R.id.txv_stock_orderPriceSub);
        txv_stock_orderPriceChoose = findViewById(R.id.txv_stock_orderPriceChoose);
        txv_stock_orderPriceAdd = findViewById(R.id.txv_stock_orderPriceAdd);
        txv_stock_orderLotSub = findViewById(R.id.txv_stock_orderLotSub);
        txv_stock_orderLotChoose = findViewById(R.id.txv_stock_orderLotChoose);
        txv_stock_orderLotAdd = findViewById(R.id.txv_stock_orderLotAdd);
        txv_stcok_showBudget = findViewById(R.id.txv_stcok_showBudget);
    }

    private void searchStockThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while (FLAG_SEARCH_STOCK_PERMISSION) {
                    renewUiStockBestaskbid(count++);
                    try {
                        Thread.sleep(2000 + (int) (Math.random() * 6500));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void renewUiStockBestaskbid(int firstTime) {
//        otc tse
        OkHttpClient client = new OkHttpClient();
//        https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_2330.tw&json=1&delay=0
        String url = "https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch="+orderStock+".tw&json=1&delay=0";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("zha", "failed onFailure");
                e.printStackTrace();
                renewUiTitleText("E", "連線失效", "-", "請檢察網路狀況", "....");
                FLAG_SEARCH_STOCK_PERMISSION = false;
                FLAG_TRADING_PERMISSION = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();
                    Log.d("zha",myResponse);
                    StockActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = new JSONObject(myResponse);
                                if (data.has("msgArray")){
                                    if (data.getJSONArray("msgArray").length()==0){
                                        renewUiTitleText("E", "查無結果", "-", "請檢查股票代號", "....");
                                        FLAG_SEARCH_STOCK_PERMISSION = false;
                                        FLAG_TRADING_PERMISSION = false;
                                    }
                                    else {
                                        JSONObject item = data.getJSONArray("msgArray").getJSONObject(0);
                                        Animation am_renew = AnimationUtils.loadAnimation(getBaseContext(), R.anim.alpha_renewtrading);
                                        // 擷取資料
                                        String n = item.getString("n");
                                        String nf = item.getString("nf");
                                        String ex = item.getString("ex");
                                        String z = item.getString("z");
                                        String tv = item.getString("tv");
                                        String v = item.getString("v");
                                        String[] bestBidP = item.getString("b").replace("-", "0.0_0.0_0.0_0.0_0.0_").split("_");
                                        String[] bestBidL = item.getString("g").replace("-", "0_0_0_0_0_").split("_");
                                        String[] bestAskP = item.getString("a").replace("-", "0.0_0.0_0.0_0.0_0.0_").split("_");
                                        String[] bestAskL = item.getString("f").replace("-", "0_0_0_0_0_").split("_");
                                        String t = item.getString("t");
                                        String y = item.getString("y");


                                        orderName = n;
                                        // 要價喊價
                                        int tatalBidLot = 0, tatalAskLot = 0;
                                        for (int i = 0; i < 5; i++) {
                                            String bp = String.format("%.02f", Float.valueOf(bestBidP[i]));
                                            String ap = String.format("%.02f", Float.valueOf(bestAskP[i]));
                                            String bl = bestBidL[i];
                                            String al = bestAskL[i];
                                            if (!txv_stock_bidprice[i].getText().toString().equals(bp)){
                                                txv_stock_bidprice[i].startAnimation(am_renew);
                                                txv_stock_bidlot[i].startAnimation(am_renew);
                                            }
                                            if (!txv_stock_askprice[i].getText().toString().equals(ap)){
                                                txv_stock_askprice[i].startAnimation(am_renew);
                                                txv_stock_asklot[i].startAnimation(am_renew);
                                            }
                                            if (!txv_stock_bidlot[i].getText().toString().equals(bl)){
                                                txv_stock_bidlot[i].startAnimation(am_renew);
                                            }
                                            if (!txv_stock_asklot[i].getText().toString().equals(al)){
                                                txv_stock_asklot[i].startAnimation(am_renew);
                                            }

                                            txv_stock_bidprice[i].setText(bp);
                                            txv_stock_bidlot[i].setText(bl);
                                            txv_stock_askprice[i].setText(ap);
                                            txv_stock_asklot[i].setText(al);
                                            tatalBidLot += Integer.valueOf(bestBidL[i]);
                                            tatalAskLot += Integer.valueOf(bestAskL[i]);
                                        }
                                        z = z.replace("-", bestBidP[0]);

                                        txv_stock_total_bid.setText(String.valueOf(tatalBidLot));
                                        txv_stock_total_ask.setText(String.valueOf(tatalAskLot));
                                        String type = "X";
                                        if (ex.equals("tse")) {
                                            type = "市";
                                        } else if (ex.equals("otc")) {
                                            type = "櫃";
                                        }

                                        float f_change = (Float.valueOf(z) - Float.valueOf(y)) / Float.valueOf(y) * 100;
                                        String str_change = String.format("%.02f", f_change) + "%";
                                        if (f_change > 0) {
                                            str_change = "▲"+str_change;
                                            txv_stock_titlePrice.setTextColor(getResources().getColor(R.color.grainer_));
                                            txv_stock_titleChange.setTextColor(getResources().getColor(R.color.grainer_));
                                        } else if (f_change < 0) {
                                            str_change = "▼"+str_change;
                                            txv_stock_titlePrice.setTextColor(getResources().getColor(R.color.loser_));
                                            txv_stock_titleChange.setTextColor(getResources().getColor(R.color.loser_));
                                        }
                                        renewUiTitleText(type, n, String.format("%.02f", Float.valueOf(z)), str_change, t);

//                                        下單區更新
                                        if (firstTime == 0) {
                                            orderPrice = Float.valueOf(z);
                                            orderLot = 1;
                                            txv_stock_orderPriceChoose.setText(String.format("%.02f", Float.valueOf(orderPrice)));
                                            txv_stock_orderLotChoose.setText("1");
                                            FLAG_SEARCH_STOCK_PERMISSION = true;
                                        }
                                        FLAG_TRADING_PERMISSION = true;
                                        txv_stcok_showBudget.setText("大約 TWD"+String.format("%.00f", orderPrice*orderLot*1000));
                                    }
                                }

                            } catch (JSONException e) {
                                Log.d("zha", "Failed");
                                e.printStackTrace();
                                renewUiTitleText("E", "意外錯誤", "-", "-", "....");
                                FLAG_SEARCH_STOCK_PERMISSION = false;
                                FLAG_TRADING_PERMISSION = false;
                            }
                        }
                    });
                }
            }
        });
    }

    private void renewUiTitleText(String ntype, String nname, String nprice, String nchange, String ntime){
        StockActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                動畫
                Animation am_renew = AnimationUtils.loadAnimation(getBaseContext(), R.anim.alpha_renewtrading);
                if (!txv_stock_titlePrice.getText().toString().equals(nprice)){
                    txv_stock_titlePrice.startAnimation(am_renew);
                }
                if (!txv_stock_titleChange.getText().toString().equals(nchange)){
                    txv_stock_titleChange.startAnimation(am_renew);
                }
                if (!txv_stock_renewTime.getText().toString().equals(ntime)){
                    txv_stock_renewTime.startAnimation(am_renew);
                }

                txv_stock_titleType.setText(ntype);
                txv_stock_titleName.setText(nname);
                txv_stock_titlePrice.setText(nprice);
                txv_stock_titleChange.setText(nchange);
                txv_stock_renewTime.setText("lastRenewTime: "+ntime);
            }
        });
    }

    public void close(View view) {
//        for(int i = 0; i<5;i++)
//        {
//            txv_stock_bidlot[i].setText(String.valueOf(i));
//            txv_stock_bidprice[i].setText(String.valueOf(i+1));
//            txv_stock_asklot[i].setText(String.valueOf(i+2));
//            txv_stock_askprice[i].setText(String.valueOf(i+3));
//        }
//        flag 重製
        FLAG_SEARCH_STOCK_PERMISSION = false;
        FLAG_TRADING_PERMISSION = false;

        finish();
    }

//    ==============處理滑鼠點擊editText控件外部==============
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {//点击editText控件外部
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    assert v != null;
                    closeKeyboard(v);//软键盘工具类
                    if (editText != null) {
                        editText.clearFocus();
                    }
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return getWindow().superDispatchTouchEvent(ev) || onTouchEvent(ev);
    }
    EditText editText = null;
    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            editText = (EditText) v;
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
        }
        return false;
    }
    public static void closeKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void connectSeverTradingStock(String orderType) {
        OkHttpClient client = new OkHttpClient();
        Calendar mCal = Calendar.getInstance();
        CharSequence s = DateFormat.format("MM-dd kk:mm:ss", mCal.getTime());    // kk:24小時制, hh:12小時制

        String parameter = "user=" + USER + "&password=" + PASSWORD + "&type=" + orderType + "&market=twStocks" + "&ticker=" + orderStock + "&price=" + String.format("%.02f", orderPrice) + "&lot=" + orderLot + "&time=" + s + "&name=" + orderName;
        String url = "https://tradingAppServer.masterrongwu.repl.co/entrust?" + parameter;
        Log.d("zha", url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("zha", "failed onFailure");
                e.printStackTrace();
                StockActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(StockActivity.this)
                                .setTitle("與伺服器連線錯誤")
                                .setMessage("無法連線至伺服器，請檢查網路狀況，若網路檢查正常，請稍後操作。")
                                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();
                    Log.d("zha", myResponse);
                    StockActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String content;
                            if (myResponse.equals("密碼錯誤"))
                                content = "密碼錯誤";
                            else if (myResponse.equals("使用者不存在"))
                                content = "使用者不存在";
                            else if (myResponse.indexOf("暫時的委託單號為：") >= 0) {
                                content = "因交易繁忙，無法瞬時成立\n\n若委託單成立，可以於委託查看到。";
                            } else if (myResponse.indexOf(USER) >= 0) {
                                content = "委託單號：" + myResponse + "\n下單成功，請到委託查看。";
                            } else if (myResponse.indexOf("不在委託時間") >= 0) {
                                content = "不在委託時間。";
                            } else {
                                content = "出現意外錯誤!";
                            }
                            new AlertDialog.Builder(StockActivity.this)
                                    .setTitle(myResponse)
                                    .setMessage(content)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).show();

                        }
                    });
                } else {
                    StockActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(StockActivity.this)
                                    .setTitle("伺服器連線錯誤")
                                    .setMessage("無法與伺服器建立連線! 目前伺服器離線中。\n若網路檢查正常，請稍後操作。")
                                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).show();
                        }
                    });
                }
            }
        });
    }

    public void test(View view) {

    }

    public void changeStockType(View view) {
        String type = btn_stock_changeStockType.getText().toString();
        if (type.equals("市")){
            btn_stock_changeStockType.setText("櫃");
        }
        if (type.equals("櫃")){
            btn_stock_changeStockType.setText("市");
        }
    }

    public void searchStock(View view) {
        txv_stock_titleType.setText("-");
        txv_stock_titleName.setText("搜尋中..");
        txv_stock_titlePrice.setText("-");
        txv_stock_titleChange.setText("-");
        FLAG_SEARCH_STOCK_PERMISSION = true;
        String type = btn_stock_changeStockType.getText().toString();
        String ticker = edt_stock_searchstock.getText().toString();
        if (type.equals("市")) {
            type = "tse";
        }
        if (type.equals("櫃")) {
            type = "otc";
        }
        orderStock = type + "_" + ticker;
        searchStockThread();
    }

//    minimum price fluctuation
    public double stockTick(){
        if(orderPrice<10){
            return 0.01;
        }
        else if(orderPrice<50){
            return 0.05;
        }
        else if(orderPrice<100){
            return 0.1;
        }
        else if(orderPrice<500){
            return 0.5;
        }
        else if(orderPrice<1000){
            return 1;
        }
        else {
            return 5;
        }
    }

    public void stock_priceSub(View view) {
        if (orderLot>0.01)
        orderPrice-=stockTick();
        txv_stock_orderPriceChoose.setText(String.format("%.02f", Float.valueOf(orderPrice)));
        txv_stcok_showBudget.setText("大約 TWD"+String.format("%.00f", orderPrice*orderLot*1000));
    }

    public void stock_priceChoose(View view) {

    }

    public void stock_priceAdd(View view) {
        orderPrice+=stockTick();
        txv_stock_orderPriceChoose.setText(String.format("%.02f", Float.valueOf(orderPrice)));
        txv_stcok_showBudget.setText("大約 TWD"+String.format("%.00f", orderPrice*orderLot*1000));
    }

    public void stock_lotSub(View view) {
        if (orderLot>1)
            orderLot-=1;
        txv_stock_orderLotChoose.setText(orderLot+"");
        txv_stcok_showBudget.setText("大約 TWD"+String.format("%.00f", orderPrice*orderLot*1000));
    }

    public void stock_lotSChoose(View view) {

    }

    public void stock_lotAdd(View view) {
        orderLot+=1;
        Log.d("zha", "orderLot"+orderLot);
        txv_stock_orderLotChoose.setText(orderLot+"");
        txv_stcok_showBudget.setText("大約 TWD"+String.format("%.00f", orderPrice*orderLot*1000));
    }

    public void buyStock(View view) {
        if (FLAG_TRADING_PERMISSION) {
            String content = "委託類型：買進 " + orderName +"\n"+
                    "委託價：" + orderPrice+"\n"+
                    orderLot+"單位 (1單位 = 1000股)\n\n"+
                    "提醒：每日1點30分到2點40分停止委託。\n此證卷系統尚未完成，股票系統還在測試階段，還無法實現穩定交易。";
            new AlertDialog.Builder(StockActivity.this)
                    .setTitle("委託單確認")
                    .setMessage(content)
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            connectSeverTradingStock("買");
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { }
                    })
                    .show();
        }else{
            new AlertDialog.Builder(StockActivity.this)
                    .setTitle("無法委託")
                    .setMessage("無法委託")
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { }
                    })
                    .show();
        }

    }

    public void sellStock(View view) {
        if (FLAG_TRADING_PERMISSION) {
            String content = "委託類型：賣出 " + orderName +"\n"+
                    "委託價：" + orderPrice+"\n"+
                    orderLot+"單位 (1單位 = 1000股)\n\n"+
                    "提醒：每日1點30分到2點40分停止委託。 此證卷系統未完成 無法穩定實現交易";
            new AlertDialog.Builder(StockActivity.this)
                    .setTitle("委託單確認")
                    .setMessage(content)
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            connectSeverTradingStock("賣");
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { }
                    })
                    .show();
        }else{
            new AlertDialog.Builder(StockActivity.this)
                    .setTitle("無法委託")
                    .setMessage("無法委託")
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { }
                    })
                    .show();
        }

    }
    public void clickOrder(View view) {
        Intent intent = new Intent(StockActivity.this, OrderActivity.class);
        intent.putExtra("user", USER);
        intent.putExtra("password", PASSWORD);
        intent.putExtra("from", "StockActivity");
        startActivityForResult(intent, 2);
        close(view);

    }

    public void clickDetail(View view) {
        Intent intent = new Intent(StockActivity.this, DetailsActivity.class);
        intent.putExtra("user", USER);
        intent.putExtra("password", PASSWORD);
        intent.putExtra("from", "StockActivity");
        startActivityForResult(intent, 8);
        close(view);

    }

    public void clickPortfolio(View view) {
        Intent intent = new Intent(StockActivity.this, InStockActivity.class);
        intent.putExtra("user", USER);
        intent.putExtra("password", PASSWORD);
        intent.putExtra("from", "StockActivity");
        startActivity(intent);
        close(view);

    }

    public void clickPrice(View view) {
        TextView price = view.findViewById(view.getId());
        orderPrice = Float.valueOf(price.getText().toString());
        txv_stock_orderPriceChoose.setText(String.format("%.02f", Float.valueOf(orderPrice)));
        txv_stcok_showBudget.setText("大約 TWD"+String.format("%.00f", orderPrice*orderLot*1000));

    }
}