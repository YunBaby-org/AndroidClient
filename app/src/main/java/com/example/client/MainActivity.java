package com.example.client;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.client.amqp.AmqpChannelFactory;
import com.example.client.manager.GpsLocationManager;
import com.example.client.manager.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
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
    private View snackBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializePreferences();

        /* Hook up views */
        this.snackBar = findViewById(R.id.main_activity_snackbar);
        this.buttonStartRegistration = findViewById(R.id.buttonStartRegistration);

        /* Register events */
        this.buttonStartRegistration.setOnClickListener(view -> {
            handleRegistration();
        });

        /* Ensure we have the location permission */
        GpsLocationManager.ensurePermissionGranted(this);

        /* TODO: Check if the location setting is enabled */
    }

    public void initializePreferences() {
        PreferenceManager pm = new PreferenceManager(this);
        if (!pm.getSharedPreferences().contains(PreferenceManager.tagAmqpHostname))
            pm.setAmqpHostname(AmqpChannelFactory.AMQP_DEFAULT_HOSTNAME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PreferenceManager.isRegistered(this))
            gotoTrackerDashboard();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                Log.i("MainActivity", result.getContents());
                Snackbar.make(snackBar, R.string.main_activity_tracker_registartion_please_wait, Snackbar.LENGTH_LONG).show();
                handleRegistration2(result.getContents());
            } else {
                Log.w("MainActivity", "Registration cancelled");
                Snackbar.make(snackBar, R.string.main_activity_tracker_registration_cancelled, Snackbar.LENGTH_LONG).show();
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
        PreferenceManager pm = new PreferenceManager(this);
        Thread thread = new Thread(() -> {
            try {
                Log.i("MainActivity", response);
                OkHttpClient client = new OkHttpClient();
                String server_site = pm.getAmqpHostname();
                Request request = new Request.Builder()
                        .url(String.format("http://%s/api/v1/mobile/trackers/tokens", server_site))
                        .post(createRequestPayload(response))
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
                this.runOnUiThread(() -> {
                    Snackbar.make(snackBar, "無法取得憑證", Snackbar.LENGTH_LONG).show();
                });
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
            extractJWT(object.getJSONObject("payload").getString("access_token"));
            pm.setRefreshToken(refreshToken);
            pm.setRegistered(true);
            Log.i("MainActivity", "Refresh token set");
            gotoTrackerDashboard(getString(R.string.main_activity_tracker_registration_successful));
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(snackBar, "無法設定憑證資料", Snackbar.LENGTH_LONG).show();
        }
    }

    private void extractJWT(String jwt) {
        DecodedJWT decoded = JWT.decode(jwt);
        Log.i("JWT", "sub: " + decoded.getSubject());
        Log.i("JWT", "aud: " + decoded.getAudience());
        Log.i("JWT", "iat: " + decoded.getIssuedAt());
        Log.i("JWT", "exp: " + decoded.getExpiresAt());
        Log.i("JWT", "jti: " + decoded.getId());
        PreferenceManager pm = new PreferenceManager(this);
        Log.i("MainActivity", "Set trackerId as " + decoded.getSubject());
        pm.setTrackerID(decoded.getSubject());
    }

    private void gotoTrackerDashboard() {
        gotoTrackerDashboard("");
    }

    private void gotoTrackerDashboard(String displayText) {
        Intent intent = new Intent(this, TrackerDashboardActivity.class);
        intent.putExtra(TrackerDashboardActivity.INTENT_DISPLAY_CONTENT, displayText);
        finish();
        startActivity(intent);
    }

    private class NoCameraActivityException extends Throwable {
    }
}