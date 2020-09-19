package com.example.client.amqp;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class AmqpUtility {

    public static String getResponseRoutingKey(String trackerId, JSONObject response) {
        try {
            return String.format("tracker.%s.event.respond.%s", trackerId, response.getString("Response"));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.wtf("AmqpUtility", "This should never happened, check if the response has Response header");
            Log.d("AmqpUtility", response.toString());
            return String.format("tracker.%s.event.respond.unknown", trackerId);
        }
    }
}
