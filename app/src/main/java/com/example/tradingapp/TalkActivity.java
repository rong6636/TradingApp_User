package com.example.tradingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TalkActivity extends AppCompatActivity {
    boolean FLAG_GET_TALK_PERMISSION = true;

    private TalkAdapter adapter;
    private ListView lsv_talk;
    private EditText edt_talkInputTalk;
    private Button btn_talkSpeak;

    private String USER, PASSWORD, activityFrom;
    private List<String> lUser;
    private List<String> lMsg;
    private List<String> lTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);

        initVeiw();
        initIntent();
        initListView();
        iniThread();
    }

    private void initVeiw(){
        edt_talkInputTalk = findViewById(R.id.edt_talkInputTalk);
        btn_talkSpeak = findViewById(R.id.btn_talkSpeak);
    }

    private void initIntent() {
        Intent intent = getIntent();
        USER = intent.getStringExtra("user");
        PASSWORD = intent.getStringExtra("password");
    }

    private void iniThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (FLAG_GET_TALK_PERMISSION) {
                    connectSeverGetTalk();
                    try {
                        Thread.sleep(5000+(int)(Math.random()*10000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    private void renewTalkUi(){
        Log.d("zha", "rT");
        Log.d("zha", "rT2");
    }

    private void initListView(){
        lUser = new ArrayList<String>();
        lMsg = new ArrayList<String>();
        lTime = new ArrayList<String>();

        lUser.add("Admin");

        lMsg.add("----- 最後的歷史資訊 -----");

        lTime.add("2021/01/01 00:00:01");

        lsv_talk = findViewById(R.id.lsv_talk);
        adapter = new TalkAdapter(this, lUser, lMsg, lTime);
        lsv_talk.setAdapter(adapter);
    }

    class TalkAdapter extends ArrayAdapter<String>{
        Context context;
        List<String> rUserName;
        List<String> rMsg;
        List<String> rTime;

        TalkAdapter (Context c, List<String> userName, List<String> msg, List<String> time){
            super(c, R.layout.item_talkitem, R.id.txv_talkItem_time_l, time);
            this.context = c;
            this.rUserName = userName;
            this.rMsg = msg;
            this.rTime = time;
        }
        
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View item_talkitem = layoutInflater.inflate(R.layout.item_talkitem, parent, false);

            LinearLayout llo_talkItem_r = item_talkitem.findViewById(R.id.llo_talkItem_r);
            LinearLayout llo_talkItem_l = item_talkitem.findViewById(R.id.llo_talkItem_l);

            TextView txv_talkItem_msg ;
            TextView txv_talkItem_time;
            if (rUserName.get(position).equals(USER)){
                llo_talkItem_l.setVisibility(View.INVISIBLE);
                llo_talkItem_r.setVisibility(View.VISIBLE);
                txv_talkItem_msg = item_talkitem.findViewById(R.id.txv_talkItem_msg_r);
                txv_talkItem_time = item_talkitem.findViewById(R.id.txv_talkItem_time_r);
            }
            else {
                llo_talkItem_l.setVisibility(View.VISIBLE);
                llo_talkItem_r.setVisibility(View.INVISIBLE);
                txv_talkItem_msg = item_talkitem.findViewById(R.id.txv_talkItem_msg_l);
                txv_talkItem_time = item_talkitem.findViewById(R.id.txv_talkItem_time_l);
            }

            txv_talkItem_msg.setText(rMsg.get(position));
            txv_talkItem_time.setText(rTime.get(position));

            return item_talkitem;
        }

    }

    private void connectSeverGetTalk() {
        OkHttpClient client = new OkHttpClient();

        String url = "https://tradingAppServer.masterrongwu.repl.co/get_talk";
        Log.d("zha", url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("zha", "failed onFailure");
                e.printStackTrace();
                TalkActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FLAG_GET_TALK_PERMISSION = false;

                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();
                    TalkActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject req = new JSONObject(myResponse);
                                JSONArray data = req.getJSONArray("msg");
                                lUser.clear();
                                lMsg.clear();
                                lTime.clear();
                                for (int i = 0; i< data.length(); i++)
                                {
                                    JSONObject d = (JSONObject) data.get(i);
                                    lUser.add(d.getString("userName"));
                                    lMsg.add(d.getString("msg"));
                                    lTime.add(d.getString("time"));
                                }
                                adapter.notifyDataSetInvalidated();
                                FLAG_GET_TALK_PERMISSION = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("zha", "failed myResponse");
                                FLAG_GET_TALK_PERMISSION = false;
                            }
                        }
                    });

                }
                else{
                    Log.d("zha", "failed onResponse");
                    FLAG_GET_TALK_PERMISSION = false;
                }

            }
        });
    }

    private void connectSeverPutSpeak() {
        OkHttpClient client = new OkHttpClient();

        String url = "https://tradingAppServer.masterrongwu.repl.co/spead_in_talk";
        String parameter = "?&user="+USER+"&msg="+editText.getText().toString();
        Log.d("zha", url);
        Request request = new Request.Builder()
                .url(url+parameter)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("zha", "failed onFailure");
                e.printStackTrace();
                TalkActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FLAG_GET_TALK_PERMISSION = false;

                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();
                    TalkActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (myResponse.equals("spead_in_talk"))
                                connectSeverGetTalk();
                        }
                    });

                }
                else{
                    Log.d("zha", "failed onResponse");
                    FLAG_GET_TALK_PERMISSION = false;
                }
            }
        });
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


    public void clickTalk(View view) {

    }

    public void clickMostActive(View view) {
        Intent intent = new Intent(TalkActivity.this, HotStocksActivity.class);
        intent.putExtra("user", USER);
        intent.putExtra("password", PASSWORD);
        startActivity(intent);
        clickClose(view);

    }

    public void clickSpeak(View view) {
        btn_talkSpeak.setVisibility(View.INVISIBLE);
        connectSeverPutSpeak();
        edt_talkInputTalk.setText("");
        btn_talkSpeak.setVisibility(View.VISIBLE);

    }

    public void clickClose(View view) {
        finish();
        FLAG_GET_TALK_PERMISSION = false;
    }
}