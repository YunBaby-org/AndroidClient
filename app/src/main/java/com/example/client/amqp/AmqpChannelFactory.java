package com.example.client.amqp;

import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class AmqpChannelFactory {

    public static final String AMQP_DEFAULT_HOSTNAME = "192.168.0.3";

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
        amqpConnection = null;
    }

    public void start(ConnectionSetting settings) {
        factory.setHost(settings.amqpHostname);
        factory.setPort(settings.amqpPort);
        factory.setVirtualHost(settings.amqpVhost);
        factory.setUsername(settings.amqpUsername);
        factory.setPassword(settings.amqpPassword);
        /* We will handle the recovery */
        factory.setTopologyRecoveryEnabled(false);
        factory.setAutomaticRecoveryEnabled(false);
        factory.setRequestedHeartbeat(10);
        factory.setNetworkRecoveryInterval(5000);
        factory.setConnectionTimeout(5000);
        ensureConnected();
    }

    public void restart_with_password(String password) {
        if (amqpConnection != null) {
            try {
                if (amqpConnection.isOpen())
                    amqpConnection.close(10);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("AmqpChannelFactory", "Connection close timeout, force abort");
                amqpConnection.abort();
            }
            amqpConnection = null;
        }
        factory.setPassword(password);
        ensureConnected();
    }

    public void close() throws IOException {
        /* Guess what, there is no way you can stop the factory from running */
        amqpConnection.close();
        amqpConnection = null;
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

    public static class ConnectionSetting {

        public String amqpHostname;
        public int amqpPort;
        public String amqpVhost;
        public String amqpUsername;
        public String amqpPassword;

        public ConnectionSetting setPort(int port) {
            this.amqpPort = port;
            return this;
        }

        public ConnectionSetting setHostname(String hostname) {
            this.amqpHostname = hostname;
            return this;
        }

        public ConnectionSetting setVhost(String vhost) {
            this.amqpVhost = vhost;
            return this;
        }

        public ConnectionSetting setPassword(String password) {
            this.amqpPassword = password;
            return this;
        }

        public ConnectionSetting setUsername(String username) {
            return setUsername(username, "tracker");
        }

        public ConnectionSetting setUsername(String username, String audience) {
            this.amqpUsername = String.format("%s:%s", username, audience);
            return this;
        }


    }

}
