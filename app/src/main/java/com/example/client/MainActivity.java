package com.example.client;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.client.services.ForegroundService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Start the service */
        startService();

        /* Exit the program immediate, we are not yet provide more advance feature right now */
        finishAffinity();
    }

    public void startService() {
        Intent intent = new Intent(MainActivity.this, ForegroundService.class);
        startService(intent);
    }

    public void stopService() {
        Intent intent = new Intent(MainActivity.this, ForegroundService.class);
        stopService(intent);
    }
}