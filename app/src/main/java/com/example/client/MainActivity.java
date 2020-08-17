package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.example.client.services.ForegroundService;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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