package com.example.client.manager;

import android.util.Log;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class AmqpConnectionManager implements IHealthCheckable {

    private ConnectionFactory factory;
    private Connection connection;

    public AmqpConnectionManager() {
        this.factory = new ConnectionFactory();
    }

    public void applySetting(ConnectionSetting settings) {
        factory.setHost(settings.amqpHostname);
        factory.setPort(settings.amqpPort);
        factory.setVirtualHost(settings.amqpVhost);
        factory.setUsername(settings.amqpUsername);
        factory.setPassword(settings.amqpPassword);
        /* We will handle the recovery */
        factory.setTopologyRecoveryEnabled(false);
        factory.setAutomaticRecoveryEnabled(false);
        factory.setNetworkRecoveryInterval(10000);
        factory.setConnectionTimeout(10000);
    }

    public void start() throws IOException, TimeoutException {
        connection = factory.newConnection();
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

    public Connection getConnection() {
        return connection;
    }

    public void stop() {
        try {
            connection.close(10000);
        } catch (IOException e) {
            connection.abort();
        } catch (AlreadyClosedException e) {
            Log.e("AmqpConnectionManager", "Attempts to close AMQP connection, but server already closed");
        }
        connection = null;
    }

    @Override
    public boolean healthCheck() {
        if (connection == null || !connection.isOpen()) return false;
        return true;
    }
}
