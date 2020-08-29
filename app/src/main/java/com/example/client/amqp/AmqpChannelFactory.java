package com.example.client.amqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

class AmqpChannelFactory {

    /* The singleton instance */
    private static AmqpChannelFactory instance;

    /* Get the singleton instance */
    static AmqpChannelFactory getInstance() throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException, IOException, TimeoutException {
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
        factory.setUsername("guest");
        factory.setPassword("guest");
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
    Channel createChannel() throws IOException {
        return amqpConnection.createChannel();
    }

}
