package com.example.tradingapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.TokenWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private  static final int REQUEST_CODE_SIGNUP = 1;
    private  static final int TRANSFER_CODE_HOMEPAGE = 2;
    private EditText edt_singin_inputuser, edt_singin_inputpw ;
    private TextView txv_loginlog;
    private Button btn_MianLogin;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initalization();

    }

    private void initalization() {
        edt_singin_inputuser = findViewById(R.id.edt_singin_inputuser);
        edt_singin_inputpw = findViewById(R.id.edt_singin_inputpw);
        txv_loginlog = findViewById(R.id.txv_loginlog);
        pref = getSharedPreferences("user_password", MODE_PRIVATE);
        btn_MianLogin = findViewById(R.id.btn_MianLogin);
        takeUserAndPassword();
    }

    public void signup(View view) {
        Intent intent = new Intent(MainActivity.this, SignupActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SIGNUP);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGNUP:
                String user = data.getStringExtra("user");
                String pw = data.getStringExtra("pw");
                if (!user.equals("") && !pw.equals("")) {
                    edt_singin_inputuser.setText(user);
                    edt_singin_inputpw.setText(pw);
                }
                break;
        }
    }

    public void login(View view) {
        btn_MianLogin.setEnabled(false);
        String u = edt_singin_inputuser.getText().toString();
        String p = edt_singin_inputpw.getText().toString();

        if (u.length()>0 && p.length()>0){
            txv_loginlog.setText("與伺服器連線中...");
            connectRelitLogin(u, p);
        }
        else{
            txv_loginlog.setText("輸入帳號密碼!");
        }
        btn_MianLogin.setEnabled(true);

    }

    public void connectRelitLogin(String user, String password) {
        String url_replit = "https://tradingappserver.masterrongwu.repl.co/login";
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
                // 連線成功
                String result = response.body().string();
                   try {
                       if(result.equals("Ok!")){
                           txv_loginlog.setText("OK");
                           openHomePage(user, password);
                       }
                       else if(result.indexOf("Hmmmm....")>0){
                           txv_loginlog.setText("無法與伺服器連線!");
                       }
                       else{
                           txv_loginlog.setText(result);
                       }
                   } catch (Exception e) {
                       txv_loginlog.setText("ERROR _MainActivity");
                   }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txv_loginlog.setText("與伺服器連線失敗，請檢查網路狀態\n若手機網路沒問題，請稍等片刻");
                    }
                });
            }
        });
    }

    public void openHomePage(String user, String password) {
        Intent intenthome = new Intent(MainActivity.this, HomePage.class);
        intenthome.putExtra("user", user);
        intenthome.putExtra("password", password);
        startActivity(intenthome);
        finish();
    }

    public void forgetPassword(View view) {
        takeUserAndPassword();
    }

    public void takeUserAndPassword() {
        String user = getSharedPreferences("user_password", MODE_PRIVATE)
                .getString("user", "");
        String password = getSharedPreferences("user_password", MODE_PRIVATE)
                .getString("password", "");
        edt_singin_inputuser.setText(user);
        edt_singin_inputpw.setText(password);
    }

    public void rememberPassword(View view) {
        String user = edt_singin_inputuser.getText().toString();
        String password = edt_singin_inputpw.getText().toString();
        if (user.equals("") || password.equals("")){
            txv_loginlog.setText("user password 未輸入");
        }
        else {
            pref.edit()
                    .putString("user", edt_singin_inputuser.getText().toString())
                    .putString("password", edt_singin_inputpw.getText().toString())
                    .commit();
        }
    }
}