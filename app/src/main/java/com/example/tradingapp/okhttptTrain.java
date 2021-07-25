package com.example.tradingapp;


//<TextView
//        android:layout_width="match_parent"
//                android:layout_height="0dp"
//                android:layout_weight="1"
//                android:text="Test to Connect replit.com"
//                android:background="#234"
//                android:layout_margin="5dp"
//                android:textSize="22dp"
//                android:textColor="#FFF"
//                android:gravity="center"
//                android:onClick="connect_replit"
//                />
//<TextView
//        android:id="@+id/result_text"
//                android:layout_width="match_parent"
//                android:layout_height="0dp"
//                android:layout_weight="12"
//                android:text="content.."
//                android:background="#234"
//                android:layout_margin="5dp"
//                android:textSize="22dp"
//                android:textColor="#FFF"
//                android:gravity="center_horizontal"
//                />

import android.view.View;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class okhttptTrain {
//
//    public void connect_replit(View view) {
//        mTextViewResult = findViewById(R.id.result_text);
//        mTextViewResult.setText("Pleace Wait...");
//
//        OkHttpClient client = new OkHttpClient();
//        String url = "https://tradingAppServer.masterrongwu.repl.co";
//
//        Request req = new Request.Builder()
//                .url(url)
//                .build();
//        client.newCall(req).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()){
//                    String  myResponse = response.body().string();
//
//                    MainActivity.this.runOnUiThread((new Runnable() {
//                        @Override
//                        public void run() {
//                            mTextViewResult.setText(myResponse);
//                        }
//                    }));
//                }
//            }
//        });
//    }
}
