package com.example.client.runners;

import android.util.Log;

import com.example.client.amqp.AmqpHandler;
import com.example.client.amqp.AmqpOperationFailedException;
import com.example.client.manager.Managers;
import com.example.client.requests.Request;
import com.example.client.requests.RequestFactory;
import com.rabbitmq.client.Delivery;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AmqpConsumerRunner implements Runnable {

    private Managers managers;
    private AmqpHandler amqpHandler;
    private String trackerId;

    public AmqpConsumerRunner(Managers managers, String trackerId) {
        this.managers = managers;
        this.trackerId = trackerId;
    }

    @Override
    public void run() {
        try {
            amqpHandler = new AmqpHandler(trackerId);
            amqpHandler.consume(getCallback());
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: When the runner crash, restart the connection and channel gracefully.
            Log.e("AMQP Consumer", "Amqp Consumer crashes");
        }
    }

    @NotNull
    private AmqpHandler.OnConsumedCallback getCallback() {
        return new AmqpHandler.OnConsumedCallback() {
            @Override
            public void onConsumed(Delivery message) throws AmqpOperationFailedException {

                /* Decode the bytes array by UTF8 charset */
                String content = new String(message.getBody(), StandardCharsets.UTF_8);

                /* Display the result */
                Log.i("Consumer", "Consumer consume a message");
                Log.d("Consumer", content);

                try {

                    /* Parse the content of message into JSON */
                    JSONObject jsonRequest = new JSONObject(content);

                    /* Create request based on the content */
                    Request request = RequestFactory.parseJSONRequest(jsonRequest);
                    if (request == null)
                        throw new UnknownRequestFormat("The given JSON object doesn't fit in any format of request");
                    Log.i("Consumer", "Consumer receive a " + request.getName() + " request");

                    /* Execute the request with the help of all these managers. */
                    JSONObject response = request.createResponse(managers);
                    Log.i("Consumer", "Consumer create response");
                    Log.d("Consumer", response.toString());

                    /* Try to send the response with retries */
                    sendResponse(response, 3);

                } catch (JSONException | UnknownRequestFormat e) {
                    e.printStackTrace();
                    Log.e("ForegroundRunner", "Failed to parse or send JSON Object");
                    Log.d("ForegroundRunner", content);
                }

            }
        };
    }

    private void sendResponse(JSONObject response, int retries) throws AmqpOperationFailedException {
        for (int i = 0; i < retries; i++) {

            try {
                amqpHandler.publishMessage("tracker-event", getResponseRoutingKey(response), response);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("ForegroundRunner", "Attempts to send message but failed, retry :" + i);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.wtf("ForegroundRunner", "Did you given me a response object with no field 'Response' ?");
            }

            /* Sleep for a while */
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.w("ForegroundRunner", "Thread wake up by interrupt");
            }
        }
        throw new AmqpOperationFailedException();
    }

    private String getResponseRoutingKey(JSONObject response) throws JSONException {
        return String.format("tracker.%s.event.respond.%s", trackerId, response.getString("Response"));
    }

    private static class UnknownRequestFormat extends Throwable {
        public UnknownRequestFormat(String s) {
            super(s);
        }
    }
}
