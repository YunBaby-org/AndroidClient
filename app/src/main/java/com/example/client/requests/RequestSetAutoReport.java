package com.example.client.requests;

import com.example.client.manager.Managers;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestSetAutoReport extends Request {

    protected boolean Enable;

    @Override
    public void parseFromJSON(JSONObject request) throws InvalidRequestFormatException, JSONException {
        requestName = request.getString("Request");
        if (!requestName.equals("SetAutoReport"))
            throw new InvalidRequestFormatException("Failed to parse such request");
        Enable = request.getJSONObject("Payload").getBoolean("Enable");
    }

    @Override
    public JSONObject createResponse(Managers managers) {
        try {
            /* TODO: Let the response effect immediately */
            managers.getPreferenceManager().setAutoReport(Enable);
            return createSuccessResponse(requestName);
        } catch (Exception e) {
            return createFailedResponse(requestName, "Internal Error");
        }
    }
}
