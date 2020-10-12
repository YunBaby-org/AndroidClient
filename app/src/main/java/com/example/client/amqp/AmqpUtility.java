package com.example.client.amqp;

import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

    /**
     * Get the user's queue by tracker id
     *
     * @param trackerId The id of tracker
     * @return the queue name of specific tracker
     */
    public static String getQueueNameByTrackerId(String trackerId) {
        return String.format("tracker.%s.requests", trackerId);
    }

    public static void sendTrackerResponse(Channel amqpChannel, String trackerId, JSONObject response) throws IOException {
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .contentEncoding("UTF-8")
                .contentType("application/json")
                .deliveryMode(2)
                .build();
        amqpChannel.basicPublish("tracker-event",
                getResponseRoutingKey(trackerId, response),
                properties,
                response.toString().getBytes(StandardCharsets.UTF_8));
    }
}
