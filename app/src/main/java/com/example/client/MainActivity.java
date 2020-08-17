package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.example.client.manager.PositionManager;
import com.example.client.manager.RequestManager;
import com.example.client.services.ForegroundService;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private PositionManager positionManager;
    private RequestManager requestManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        positionManager = new PositionManager(this);
        requestManager = new RequestManager(this);




    }

    @Override
    protected void onStart() {
        super.onStart();
        Thread t = new Thread(){
            @Override
            public void run() {
                super.run();
//                JSONObject wifi_res = positionManager.ScanWifi();
//                requestManager.post("http://140.125.205.78:8080",wifi_res.toString());
                requestManager.createwebsocket("ws://140.125.205.78:8080");
            }
        };
        t.start();
    }

    public void startService(View v){
        Intent intent = new Intent(MainActivity.this, ForegroundService.class);
        startService(intent);
    }
    public void stopService(View v){
        Intent intent = new Intent(MainActivity.this,ForegroundService.class);
        stopService(intent);
    }
    public void test(View v){

    }
}