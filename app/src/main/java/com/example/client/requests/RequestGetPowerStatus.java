package com.example.client.requests;

import com.example.client.manager.Managers;
import com.example.client.manager.PowerManager;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestGetPowerStatus extends Request {
    @Override
    public void parseFromJSON(JSONObject request) throws JSONException, InvalidRequestFormatException {
        requestName = request.getString("Request");
        if (!requestName.equals("GetPowerStatus"))
            throw new InvalidRequestFormatException("Failed to parse such request");
    }

    @Override
    public JSONObject createResponse(Managers managers) {
        try {

            /* Query power status */
            PowerManager powerManager = managers.getPowerManager();
            JSONObject result = new JSONObject();
            result.put("CapacityLevel", powerManager.getBatteryLevel());

            /* Create the object */
            return Request.createSuccessResponse(requestName, result);

        } catch (Exception e) {
            e.printStackTrace();
            return Request.createFailedResponse(requestName, "Internal Error");
        }
    }
}
