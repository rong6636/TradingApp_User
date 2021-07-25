package com.example.tradingapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.TokenWatcher;
import android.util.Log;
import android.view.View;
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
        String u = edt_singin_inputuser.getText().toString();
        String p = edt_singin_inputpw.getText().toString();

        if (u.length()>0 && p.length()>0){
            connectRelitLogin(u, p);
        }
        else{
            txv_loginlog.setText("輸入帳號密碼!");
        }

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
                if(result.equals("Ok!")){
                    txv_loginlog.setText("OK");
                    openHomePage(user, password);
                }
                else{
                    txv_loginlog.setText(result);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // 連線失敗
                Log.d("HKT", e.toString());

            }
        });
    }
    public void openHomePage(String user, String password) {
        Intent intenthome = new Intent(MainActivity.this, HomePage.class);
        intenthome.putExtra("user", user);
        intenthome.putExtra("password", password);
        startActivityForResult(intenthome, REQUEST_CODE_SIGNUP);
        finish();
    }
}