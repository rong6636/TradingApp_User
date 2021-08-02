package com.example.tradingapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AccountActivity extends AppCompatActivity {
    private TextView txv_log, edt_userName;
    private String user, password, activityFrom;
    Button btn_rename;
    boolean flag_btn_rename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        iniTextView();
        initIntent();
        connectRelitLogin();
    }

    private void iniTextView() {
        btn_rename = findViewById(R.id.btn_rename);
        btn_rename.setText("E");
        flag_btn_rename = true;
        edt_userName = findViewById(R.id.edt_userName);
        txv_log = findViewById(R.id.txv_log);
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

    public void connectRelitLogin() {
        Log.d("zha","connectRelitLogin");
        String url_replit = "https://tradingappserver.masterrongwu.repl.co/get_account_detail";
        url_replit+="?&user="+user+"&password="+password;
        // 建立 OkHttpClient
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        // 建立 Request，設定連線資訊
        Request request = new Request.Builder()
                .url(url_replit)
                .build();
        // 建立 Call
        Call call = client.newCall(request);

        // 執行 Call 連線
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();
                    Log.d("zha", myResponse);
                    AccountActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = new JSONObject(myResponse);
                                String Name = data.getString("Name");
                                String SigninTimes = data.getString("SigninTimes");
                                String lastSinginTime = data.getString("lastSinginTime");

                                edt_userName.setText(Name);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("zha", "failed myResponse");
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                txv_log.setText("與伺服器連線失敗，若手機網路沒問題，請稍等片刻");
            }
        });
    }



    public void connectRelitReName(){
        Log.d("zha","connectRelitLogin");
        String url_replit = "https://tradingappserver.masterrongwu.repl.co/renameName";
        url_replit+="?&user="+user+"&password="+password+"&name="+edt_userName.getText().toString();
        // 建立 OkHttpClient
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        // 建立 Request，設定連線資訊
        Request request = new Request.Builder()
                .url(url_replit)
                .build();
        // 建立 Call
        Call call = client.newCall(request);

        // 執行 Call 連線
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();
                    Log.d("zha", myResponse);
                    AccountActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("zha", "run");
                            String content;
                            if (myResponse.equals("密碼錯誤"))
                                content="密碼錯誤";
                            else if (myResponse.equals("使用者不存在"))
                                content="使用者不存在";
                            else if(myResponse.indexOf("Successful")>=0){
                                content = "伺服器確認完成，名稱更改成功! 若想看到新名稱請重新登入。";
                            }
                            else {
                                content = "出現意外錯誤!";
                            }
                            new AlertDialog.Builder(AccountActivity.this)
                                    .setTitle(myResponse)
                                    .setMessage(content)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) { }
                                    }).show();
                        }
                    });
                }
                else{
                    AccountActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(AccountActivity.this)
                                    .setTitle("與伺服器連線錯誤")
                                    .setMessage("無法與伺服器連線! 目前伺服器離線中。\n若網路檢查正常，請稍後操作。")
                                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) { }
                                    }).show();
                        }
                    });
                }

            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("zha", "eeeee");
                AccountActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(AccountActivity.this)
                                .setTitle("與伺服器連線錯誤")
                                .setMessage("無法連線至伺服器，請檢查網路狀況，若網路檢查正常，請稍後操作。")
                                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) { }
                                }).show();
                    }
                });

            }
        });
    }













    public void clickClose(View view) {
        Intent intent = getIntent();
        setResult(8, intent);
        intent.putExtra("logout", "no");
        finish();
    }

    public void clickLogout(View view) {
        Intent intent = getIntent();
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        intent.putExtra("logout", "logout");
        setResult(8, intent);
        finish();
    }

    public void clickRename(View view) {
        if (flag_btn_rename){
            btn_rename.setText("R");
            flag_btn_rename = false;
            edt_userName.setFocusableInTouchMode(true);
            edt_userName.setFocusable(true);
            edt_userName.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
        else{
            btn_rename.setText("E");
            flag_btn_rename = true;
            edt_userName.setFocusableInTouchMode(false);
            edt_userName.setFocusable(false);
            String content = "若確定更改名稱，請按下\"OK\"，伺服器確認沒問題後，重新登入就可以更改成功。";
            new AlertDialog.Builder(AccountActivity.this)
                    .setTitle("確定改名")
                    .setMessage(content)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            connectRelitReName();
                        }
                    }).show();

        }
    }
}