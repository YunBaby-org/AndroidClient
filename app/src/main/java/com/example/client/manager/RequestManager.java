package com.example.client.manager;

import android.util.Log;

import com.example.client.amqp.AmqpUtility;
import com.example.client.requests.Request;
import com.example.client.requests.RequestFactory;
import com.example.client.room.entity.Event;
import com.example.client.services.ServiceState;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RequestManager implements ShutdownListener, DeliverCallback, CancelCallback, IHealthCheckable {

    private ServiceState serviceState;
    private Channel amqpChannel;
    private String trackerId;
    private boolean isAlive;

    public RequestManager(String trackerId, Channel amqpChannel, ServiceState serviceState) {
        try {
            this.isAlive = true;
            this.trackerId = trackerId;
            this.serviceState = serviceState;
            this.amqpChannel = amqpChannel;
            this.amqpChannel.addShutdownListener(this);
            this.amqpChannel.queueDeclare(AmqpUtility.getQueueNameByTrackerId(trackerId), true, false, false, null);
            this.amqpChannel.basicQos(3);
            this.amqpChannel.basicConsume(RequestManager.getQueueNameByTrackerId(trackerId), this, this);
        } catch (IOException e) {
            fuckUp(e);
        }
    }

    public void stop() {
        closeChannel();
    }

    @Override
    public void handle(String consumerTag, Delivery message) throws IOException {
        try {
            onConsumeMessage(message);
            amqpChannel.basicAck(message.getEnvelope().getDeliveryTag(), false);
        } catch (Exception e) {
            Log.e("RequestManager", "Cannot handle message, consider drop");
            Log.d("RequestManager", message.getBody().toString());
            amqpChannel.basicNack(message.getEnvelope().getDeliveryTag(), false, false);
        }
    }

    @Override
    public void handle(String consumerTag) throws IOException {
        /* This method handle cancelled message */
    }

    @Override
    public void shutdownCompleted(ShutdownSignalException cause) {
        cause.printStackTrace();
        Log.e("RequestManager", "Channel dead");
    }

    @Override
    public boolean healthCheck() {
        if (!isAlive) return false;
        if (!amqpChannel.isOpen()) return false;
        return true;
    }

    private void onConsumeMessage(Delivery message) {
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

            com.example.client.room.entity.Event event = Event.request(Event.ID.RECEIVE_REQUEST, request.getName());
            serviceState.getAppDatabase().eventDao().insertAll(event);

            /* Execute the request with the help of all these serviceState. */
            JSONObject response = request.createResponse(serviceState);
            Log.i("Consumer", "Consumer create response");
            Log.d("Consumer", response.toString());

            /* Try to send the response with retries */
            AmqpUtility.sendTrackerResponse(amqpChannel, trackerId, response);

        } catch (JSONException | UnknownRequestFormat | IOException e) {
            e.printStackTrace();
            Log.e("ForegroundRunner", "Failed to parse or send JSON Object");
            Log.d("ForegroundRunner", content);
        }
    }


    public boolean stillAlive() {
        return this.isAlive;
    }

    private void closeChannel() {
        if (this.amqpChannel != null && this.amqpChannel.isOpen()) {
            try {
                try {
                    this.amqpChannel.close();
                } catch (TimeoutException ex) {
                    this.amqpChannel.abort();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                Log.e("RequestManager", "Attempts to close channel: IOException");
            }
        }
        this.amqpChannel = null;
    }

    private void fuckUp(Throwable e) {
        e.printStackTrace();
        isAlive = false;
        closeChannel();
    }

    public static String getQueueNameByTrackerId(String trackerId) {
        return String.format("tracker.%s.requests", trackerId);
    }


    private class UnknownRequestFormat extends Exception {
        public UnknownRequestFormat(String message) {
            super(message);
        }
    }
}
