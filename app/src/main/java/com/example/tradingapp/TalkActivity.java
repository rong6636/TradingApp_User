package com.example.tradingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class TalkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);
    }

    public void clickTalk(View view) {

    }

    public void clickMostActive(View view) {

    }


    public void clickClose(View view) {
        finish();
    }
}