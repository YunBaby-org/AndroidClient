package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.TransactionTooLargeException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.example.client.manager.PositionManager;
import com.example.client.manager.RequestManager;
import com.example.client.services.ForegroundService;

import org.json.JSONException;
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

    /*
        if you want to start foreground service
        call this function
    * */
    public void startService(View v){
        Intent intent = new Intent(MainActivity.this, ForegroundService.class);
        startService(intent);
    }
    public void stopService(View v){
        Intent intent = new Intent(MainActivity.this,ForegroundService.class);
        stopService(intent);
    }
    public void login(View v){
        JSONObject json = new JSONObject();
        try {
            json.put("email","toby5500@kimo.com");
            json.put("password","asfasgag");
            requestManager.post("http://140.125.205.78:8080/","login",json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("error","login json error");
        }


    }
    public void whoami(View v){
        requestManager.get("http://140.125.205.78:8080/","whoami");
    }
    public void websocket(View v){
        //when we enter server, it will auto update the session expire time
        //requestManager.get("http://140.125.205.78:8080/","any");
        requestManager.createwebsocket("ws://140.125.205.78:8080");
    }
}