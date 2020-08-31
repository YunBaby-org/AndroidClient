package com.example.client.amqp;

import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class AmqpChannelFactory {

    /* The singleton instance */
    private static AmqpChannelFactory instance;

    /* Get the singleton instance */
    public static AmqpChannelFactory getInstance() {
        if (instance == null) {
            synchronized (AmqpChannelFactory.class) {
                if (instance == null)
                    instance = new AmqpChannelFactory();
            }
        }
        return instance;
    }

    /* The class part */
    private ConnectionFactory factory;
    private Connection amqpConnection;

    /* Constructor */
    private AmqpChannelFactory() {
        factory = new ConnectionFactory();
        factory.setHost("192.168.0.3");
        factory.setVirtualHost("/");
        factory.setRequestedHeartbeat(10);
        factory.setNetworkRecoveryInterval(5000);
        factory.setConnectionTimeout(5000);
        factory.setUsername("guest");
        factory.setPassword("guest");
        /* We will handle the recovery */
        factory.setTopologyRecoveryEnabled(false);
        factory.setAutomaticRecoveryEnabled(false);
    }

    public synchronized boolean ensureConnected() {
        /* If the connection already opened, return true */
        if (amqpConnection != null && amqpConnection.isOpen()) return true;

        /* Otherwise reopen the connection */
        try {
            amqpConnection = factory.newConnection();
            return true;
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* Get a channel from existing connection */
    public Channel createChannel() throws IOException {
        Log.d("AmqpChannelFactory", "Create new channel");
        return amqpConnection.createChannel();
    }

}
