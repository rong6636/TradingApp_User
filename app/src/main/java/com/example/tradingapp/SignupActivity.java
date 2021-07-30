package com.example.tradingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.security.SecureRandom;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {
    private  static final int REQUEST_CODE_SIGNUP = 1;
    private String RESULT_USER, RESULT_PW;

    private EditText edt_inputuser, edt_inputpw, edt_inputcheckpw;
    private TextView txv_signuplog;
    Button btn_signuptoserver;
    String user, password, chkpw, logresult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initialization();
    }

    private void initialization() {
        edt_inputuser = findViewById(R.id.edt_inputuser);
        edt_inputpw = findViewById(R.id.edt_inputpw);
        edt_inputcheckpw = findViewById(R.id.edt_inputcheckpw);
        txv_signuplog = findViewById(R.id.txv_signuplog);
        btn_signuptoserver = findViewById(R.id.btn_signuptoserver);
        user = "";
        password = "";
        chkpw = "";
        RESULT_USER = "";
        RESULT_PW = "";
        txv_signuplog.setText("");
    }

    public void quickSignup(View view) {
        String q_user = randomString(8);
        String q_password = "12345678";
        edt_inputuser.setText(q_user);
        edt_inputpw.setText(q_password);
        edt_inputcheckpw.setText(q_password);
    }

    String randomString(int len){
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        char r_char = AB.charAt(rnd.nextInt(AB.length()));
        while (sb.length()<8){
            if (Math.random()*10<3){
                sb.append(r_char);
            }
            else{
                r_char = AB.charAt(rnd.nextInt(AB.length()));
                sb.append(r_char);
            }
        }
        return sb.toString();
    }

    public void signuptoServer(View view) {
        btn_signuptoserver.setEnabled(false);
        user = edt_inputuser.getText().toString().trim();
        password = edt_inputpw.getText().toString().trim();
        chkpw = edt_inputcheckpw.getText().toString().trim();

        if (user.equals("") || password.equals("") || chkpw.equals(""))
        {
            txv_signuplog.setText("輸入欄位為空! 請再次檢查");
        }
        else if (user.length()<6){
            txv_signuplog.setText("帳戶長度不合!");
        }
        else if (password.length()<8){
            txv_signuplog.setText("密碼長度不合!");
        }
        else if (!password.equals(chkpw)){
            txv_signuplog.setText("密碼檢查錯誤! 請再次確認");
        }
        else{
            txv_signuplog.setText("與伺服器完成確認中...");
            connect_replitThread thread = new connect_replitThread();
            thread.start();
        }

        btn_signuptoserver.setEnabled(true);

    }
    public void connect_replit(){
        String url_replit = "https://tradingappserver.masterrongwu.repl.co/signup";
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
                Log.d("zha","in");
                // 連線成功
                String result = response.body().string();
                if (result.equals("Create Finish")){
                    Log.d("zha","in1");
                    txv_signuplog.setText(result);
                    RESULT_USER = user;
                    RESULT_PW = password;
                    close();
                }
                else if (result.equals("帳號已存在!")){
                    Log.d("zha","in2");
                    txv_signuplog.setText(result);
                    RESULT_USER = user;
                    RESULT_PW = "";
                    close();
                }
                else{
                    txv_signuplog.setText("與伺服器連線失敗!");
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("C","failed");
                // 連線失敗
                Log.d("HKT", e.toString());

                txv_signuplog.setText(e.toString());
            }
        });


    }


    class connect_replitThread extends Thread{
        @Override
        public void run() {
            connect_replit();
        }
    }

    public void close(View view) {
        close();
    }

    public void close() {
        Intent intent = getIntent();
        intent.putExtra("user", RESULT_USER);
        intent.putExtra("pw", RESULT_PW);
        setResult(REQUEST_CODE_SIGNUP, intent);
        finish();
    }

}