package com.example.client.requests;

import com.example.client.manager.PowerManager;
import com.example.client.services.ServiceState;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestGetPowerStatus extends Request {
    @Override
    public void parseFromJSON(JSONObject request) throws JSONException, InvalidRequestFormatException {
        id = request.getString("id");
        requestName = request.getString("Request");
        if (!requestName.equals("GetPowerStatus"))
            throw new InvalidRequestFormatException("Failed to parse such request");
    }

    @Override
    public JSONObject createResponse(ServiceState serviceState) {
        try {

            /* Query power status */
            PowerManager powerManager = serviceState.getPowerManager();
            JSONObject result = new JSONObject();
            result.put("CapacityLevel", powerManager.getBatteryLevel());

            /* Create the object */
            return createSuccessResponse(requestName, result);

        } catch (Exception e) {
            e.printStackTrace();
            return createFailedResponse(requestName, "Internal Error");
        }
    }
}
