package com.example.client;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.client.manager.GpsLocationManager;
import com.example.client.manager.PreferenceManager;
import com.example.client.services.ForegroundService;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_FILE_PROVIDER_AUTHORITIES = "com.example.client";
    private static final int TAG_REQUEST_TAKE_QRCODE = 1;
    private File imageFile;
    private String privateImagePath;
    private Button buttonStartRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Hook up views */
        this.buttonStartRegistration = findViewById(R.id.buttonStartRegistration);

        /* Register events */
        this.buttonStartRegistration.setOnClickListener(view -> {
            handleRegistration();
        });


        /* Ensure we have the location permission */
        GpsLocationManager.ensurePermissionGranted(this);

        /* TODO: Check if the location setting is enabled */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                handleRegistration2(result.getContents());
                Log.i("MainActivity", result.getContents());
            } else {
                Log.w("MainActivity", "Registration cancelled");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleRegistration() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("請對準平安服的註冊 QR Code");
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    private void handleRegistration2(String response) {
        /* Send HTTP request to obtain credentials */
        Thread thread = new Thread(() -> {
            try {
                /* e04 e04 e04 專題要做不完了，我還不寫糞 code */
                String authentication_code = (String) new JSONObject(response).get("authentication_code");
                Log.i("MainActivity", authentication_code);
                OkHttpClient client = new OkHttpClient();
                String server_site = getString(R.string.server_site);
                Request request = new Request.Builder()
                        .url(String.format("http://%s/api/v1/mobile/trackers/tokens", server_site))
                        .post(createRequestPayload(authentication_code))
                        .build();
                Response httpResponse = client.newCall(request).execute();
                String result = httpResponse.body().string();
                this.runOnUiThread(() -> {
                    Log.i("MainActivity", "Obtain response: " + result);
                    setRawCredentials(result);
                });
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                Log.e("MainActivity", "Failed to register");
            }
        });
        thread.start();
    }

    private RequestBody createRequestPayload(String authentication_code) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("authentication_code", authentication_code);
        return RequestBody.create(object.toString(), MediaType.parse("application/json; charset=utf-8"));
    }

    private void setRawCredentials(String rawCredentials) {
        try {
            JSONObject object = new JSONObject(rawCredentials);
            PreferenceManager pm = new PreferenceManager(this);
            String refreshToken = object.getJSONObject("payload").getString("refresh_token");
            pm.setRefreshToken(refreshToken);
            pm.setRegistered(true);
            Log.i("MainActivity", "Refresh token set");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startService() {
        Intent intent = new Intent(MainActivity.this, ForegroundService.class);
        startService(intent);
    }

    public void stopService() {
        Intent intent = new Intent(MainActivity.this, ForegroundService.class);
        stopService(intent);
    }

    private class NoCameraActivityException extends Throwable {
    }
}