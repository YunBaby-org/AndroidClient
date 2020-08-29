package com.example.client.requests;

import android.util.Log;

import com.example.client.manager.Managers;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Request {

    protected String requestName;

    public String getName() {
        return requestName;
    }

    /**
     * This method try to load the properties of this request from specified JSON Object
     *
     * @param request The given JSON Object
     */
    public abstract void parseFromJSON(JSONObject request) throws JSONException, InvalidRequestFormatException;

    /**
     * Handle the request and generate the response
     *
     * @param managers The managers object
     * @return The response JSON object
     */
    public abstract JSONObject createResponse(Managers managers);

    /**
     * Create success response, with no payload field
     *
     * @param responseType Which type of request the response respond to
     * @return The response JSON object
     */
    protected static JSONObject createSuccessResponse(String responseType) {
        return createSuccessResponse(responseType, null, false);
    }

    /**
     * Create success response
     *
     * @param responseType Which type of request the response respond to
     * @param payload      The payload of response
     * @return The response JSON object
     */
    protected static JSONObject createSuccessResponse(String responseType, JSONObject payload) {
        return createSuccessResponse(responseType, payload, false);
    }

    /**
     * Create success response
     *
     * @param responseType  Which type of request the response respond to
     * @param payload       The payload of response
     * @param withTimestamp Add timestamp to response
     * @return The response JSON object
     */
    protected static JSONObject createSuccessResponse(String responseType, JSONObject payload, boolean withTimestamp) {
        JSONObject response = new JSONObject();
        try {
            response.put("Response", responseType);
            response.put("Status", "Success");
            if (payload != null)
                response.put("Result", payload);
            if (withTimestamp)
                response.put("timestamp", (int) (System.currentTimeMillis() / 1000));
        } catch (JSONException e) {
            Log.wtf("Request", "What the hell is going on? this should never happens");
        }
        return response;
    }

    /**
     * Create a failed response
     *
     * @param responseType Which type of request the response respond to
     * @param reason       The reason
     * @return The response JSON object
     */
    protected static JSONObject createFailedResponse(String responseType, String reason) {
        JSONObject response = new JSONObject();
        try {
            response.put("Response", responseType);
            response.put("Status", "Failed");
            response.put("Info", reason);
        } catch (JSONException e) {
            Log.wtf("Request", "What the hell is going on? this should never happens");
        }
        return response;
    }

    class InvalidRequestFormatException extends Exception {
        public InvalidRequestFormatException() {
        }

        public InvalidRequestFormatException(String message) {
            super(message);
        }

        public InvalidRequestFormatException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidRequestFormatException(Throwable cause) {
            super(cause);
        }

        public InvalidRequestFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
