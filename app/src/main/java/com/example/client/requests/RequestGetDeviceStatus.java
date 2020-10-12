package com.example.client.requests;

import com.example.client.services.ServiceState;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestGetDeviceStatus extends Request {
    @Override
    public void parseFromJSON(JSONObject request) throws InvalidRequestFormatException, JSONException {
        id = request.getString("id");
        requestName = request.getString("Request");
        if (!requestName.equals("GetDeviceStatus"))
            throw new InvalidRequestFormatException("Failed to parse such request");
    }

    @Override
    public JSONObject createResponse(ServiceState serviceState) {
        try {
            JSONObject result = new JSONObject();
            result.put("TrackerID", serviceState.getPreferenceManager().getTrackerID());
            result.put("AutoReportGps", serviceState.getPreferenceManager().getAutoReportGps());
            result.put("ReportIntervalGps", serviceState.getPreferenceManager().getReportIntervalGps());
            result.put("AutoReportWifi", serviceState.getPreferenceManager().getAutoReportWifi());
            result.put("ReportIntervalWifi", serviceState.getPreferenceManager().getReportIntervalWifi());
            result.put("PowerSaving", serviceState.getPreferenceManager().getPowerSaving());
            return createSuccessResponse(requestName, result);
        } catch (Exception e) {
            e.printStackTrace();
            return createFailedResponse(requestName, "InternalError");
        }
    }
}
