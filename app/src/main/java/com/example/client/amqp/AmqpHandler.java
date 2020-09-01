package com.example.client.amqp;

import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/* This class create a AMQP consumer */
/* It must run on different thread */
public class AmqpHandler {
    private final static int AMQP_CONSUMER_PREFETCH_LIMIT = 2;

    public Channel getAmqpChannel() {
        return amqpChannel;
    }

    private Channel amqpChannel;
    private String trackerId;

    public AmqpHandler(String trackerId) throws Exception {
        this.trackerId = trackerId;

        /* Ensure the channel is setup, if it failed, it will retry a couple of times */
        initializeChannel(30, 1000);

        /* Assert queue */
        amqpChannel.queueDeclare(AmqpHandler.getQueueNameByTrackerId(trackerId), true, false, false, null);
    }

    /**
     * Try to initialize the AMQP channel gracefully
     *
     * @param retries      How many times to retry if the operation failed
     * @param baseInterval The interval of each retry, notice that the value will vary between [interval, interval *3] to prevent retry burst on server side.
     * @throws Exception Unable to setup channel after retries.
     */
    private void initializeChannel(int retries, int baseInterval) throws Exception {

        Exception lastError = null;

        for (int i = 0, interval = baseInterval; i < retries; i++) {
            try {
                AmqpChannelFactory.getInstance().ensureConnected();
                amqpChannel = AmqpChannelFactory.getInstance().createChannel();
                return;
            } catch (Exception e) {
                Log.w("AmqpHandler", "Connect to AMQP Broker failed, retry :" + i);
                lastError = e;
            }
            /* The retry interval will be [interval, interval*6], this could avoid a retry burst if our server fuck up for a moment */
            Thread.sleep((int) (interval * (1 + 5 * Math.random())));
            /* Increase the interval by a factor of 1.25, maximum will be 10 second */
            interval = Math.min((int) (interval * 1.25), 6_000);
        }
        throw new Exception("Unable to setup channel", lastError);
    }

    /**
     * Start consuming message from the tracker's queue.
     *
     * @param callback the callback to invoke when message consumed
     * @throws IOException Something fuck up
     */
    public void consume(OnConsumedCallback callback) throws IOException {
        amqpChannel.basicQos(AmqpHandler.AMQP_CONSUMER_PREFETCH_LIMIT);
        amqpChannel.basicConsume(AmqpHandler.getQueueNameByTrackerId(trackerId), false, getDeliverCallback(callback), getCancelCallback());
    }

    /**
     * Return a new delivery callback object, which deal with the consuming process
     *
     * @param callback The callback to invoke when message is consumed
     * @return the new delivery callback instance
     */
    @NotNull
    private DeliverCallback getDeliverCallback(final OnConsumedCallback callback) {
        return new DeliverCallback() {
            @Override
            public void handle(String consumerTag, Delivery message) throws IOException {

                Log.d("AmqpHandler", "Consumer message with thread " + Thread.currentThread().getName());

                /* Attempts to call the callback if it is given */
                if (callback != null) {
                    try {
                        callback.onConsumed(message);
                    } catch (AmqpOperationFailedException e) {
                        e.printStackTrace();
                        /* Sad as fuck we cannot throw custom error, so we throw this one instead */
                        amqpChannel.basicNack(message.getEnvelope().getDeliveryTag(), false, true);
                        throw new IOException(e);
                    }
                }

                /* Confirm the message, which delete it from the RabbitMQ queue as we say we finish it */
                amqpChannel.basicAck(message.getEnvelope().getDeliveryTag(), false);

            }
        };
    }

    /**
     * Return a new cancel callback, to be honest I have no idea what that is. Let's ignore it at this moment
     *
     * @return the useless cancel callback
     */
    @NotNull
    private CancelCallback getCancelCallback() {
        return new CancelCallback() {
            @Override
            public void handle(String consumerTag) {
            }
        };
    }

    public void publishMessage(String exchange, String routingKey, JSONObject message) throws IOException {
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .contentEncoding("UTF8")
                .contentType("application/json")
                .deliveryMode(2) /* Persistent */
                .build();
        publishMessage(exchange, routingKey, message, properties);
    }

    public void publishMessage(String exchange, String routingKey, JSONObject message, AMQP.BasicProperties messageProperty) throws IOException {
        if (this.amqpChannel.isOpen())
            this.amqpChannel.basicPublish(exchange, routingKey, messageProperty, message.toString().getBytes(StandardCharsets.UTF_8));
        else
            throw new IOException("Disconnected");
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

    public interface OnConsumedCallback {
        void onConsumed(Delivery message) throws AmqpOperationFailedException;
    }
}

