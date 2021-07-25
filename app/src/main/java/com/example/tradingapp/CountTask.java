package com.example.tradingapp;

import android.os.AsyncTask;
import android.os.SystemClock;

class CountTask extends AsyncTask<String, Integer, String> {

    public OnTimerCount onTimerCount;

    /*要在背景執行緒做的事*/
    /*return的值將會在onPostExecute顯現*/
    @Override
    protected String doInBackground(String... strings) {
        for (int i = 1; i < 6; i++) {
            /*上傳執行進度，此進度會顯示在onProgressUpdate*/
            publishProgress(i);
            SystemClock.sleep(1000);
        }
        return "";
    }
    /*取得執行進度*/
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        onTimerCount.onCountering(values[0]);
    }
    /*完成進度後要做的事會在這邊做處理*/
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        onTimerCount.onCounterFinish(s);
    }

    interface OnTimerCount{
        void onCountering(int progress);
        void onCounterFinish(String text);

    }
}