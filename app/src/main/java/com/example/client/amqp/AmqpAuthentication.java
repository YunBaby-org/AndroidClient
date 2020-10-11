package com.example.client.amqp;

import android.util.Log;

import com.example.client.manager.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AmqpAuthentication {

    /**
     * Obtain a new access token. this is a synchronized operation, so the executing thread will block
     * until the result is back or timeout occurred.
     *
     * @param pm
     * @return
     */
    public static String obtainAccessToken(PreferenceManager pm) throws IOException, JSONException, BadRequestException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .callTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        /* Create a request to obtain new access token */
        Request request = new Request.Builder()
                .url(obtainAccessTokenUrl(pm))
                .patch(obtainAccessTokenPayload(pm))
                .build();

        /* Execute the request and get the response */
        Response httpResponse = client.newCall(request).execute();

        /* Test if the request succeed */
        if (httpResponse.code() != 200)
            throw new BadRequestException();


        /* Return the access token */
        return new JSONObject(Objects.requireNonNull(httpResponse.body()).string())
                .getJSONObject("payload")
                .getString("access_token");
    }

    private static String obtainAccessTokenUrl(PreferenceManager pm) {
        return String.format("http://%s/api/v1/mobile/trackers/tokens", pm.getAmqpHostname());
    }

    private static RequestBody obtainAccessTokenPayload(PreferenceManager pm) {
        JSONObject object = new JSONObject();
        try {
            object.put("refresh_token", pm.getRefreshToken());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.wtf("AmqpAuthentication", "What the fuck is going on. I can't put the token in a new JSON?");
        }
        return RequestBody.create(object.toString(), MediaType.parse("application/json; charset=utf-8"));
    }

    public static class BadRequestException extends Throwable {
    }
}
