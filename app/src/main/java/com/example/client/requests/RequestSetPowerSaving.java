package com.example.client.requests;

import com.example.client.services.ServiceContext;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestSetPowerSaving extends Request {

    protected boolean Enable;

    @Override
    public void parseFromJSON(JSONObject request) throws InvalidRequestFormatException, JSONException {
        id = request.getString("id");
        requestName = request.getString("Request");
        if (!requestName.equals("SetPowerSaving"))
            throw new InvalidRequestFormatException("Failed to parse such request");
        Enable = request.getJSONObject("Payload").getBoolean("Enable");
    }

    @Override
    public JSONObject createResponse(ServiceContext serviceContext) {
        serviceContext.getPreferenceManager().setPowerSaving(Enable);
        /* TODO: Implement this method if you like */
        return createFailedResponse(requestName, "Request Not Supported");
    }

}
