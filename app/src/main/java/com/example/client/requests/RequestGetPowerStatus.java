package com.example.client.requests;

import com.example.client.manager.PowerManager;
import com.example.client.services.ServiceContext;

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
    public JSONObject createResponse(ServiceContext serviceContext) {
        try {

            /* Query power status */
            PowerManager powerManager = serviceContext.getPowerManager();
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
