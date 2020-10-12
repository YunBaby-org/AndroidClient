package com.example.client.manager;

public interface IHealthCheckable {
    /**
     * Determine if the object is working in the right condition
     *
     * @return a boolean indicate the health state of specific object
     */
    boolean healthCheck();
}
