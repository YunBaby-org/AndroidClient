package com.example.client.requests;

import com.example.client.manager.Managers;

import org.json.JSONException;
import org.json.JSONObject;

@RequestClass
public class RequestGetDeviceStatus extends Request {
    @Override
    public void parseFromJSON(JSONObject request) throws InvalidRequestFormatException, JSONException {
        requestName = request.getString("Request");
        if (!requestName.equals("GetDeviceStatus"))
            throw new InvalidRequestFormatException("Failed to parse such request");
    }

    @Override
    public JSONObject createResponse(Managers managers) {
        try {
            JSONObject result = new JSONObject();
            result.put("TrackerID", managers.getPreferenceManager().getTrackerID());
            result.put("AutoReport", managers.getPreferenceManager().getAutoReport());
            result.put("ReportInterval", managers.getPreferenceManager().getReportInterval());
            result.put("PowerSaving", managers.getPreferenceManager().getPowerSaving());
            return Request.createSuccessResponse(requestName, result);
        } catch (Exception e) {
            e.printStackTrace();
            return Request.createFailedResponse(requestName, "InternalError");
        }
    }
}
