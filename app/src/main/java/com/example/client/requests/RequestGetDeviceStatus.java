package com.example.client.requests;

import com.example.client.services.ServiceContext;

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
    public JSONObject createResponse(ServiceContext serviceContext) {
        try {
            JSONObject result = new JSONObject();
            result.put("TrackerID", serviceContext.getPreferenceManager().getTrackerID());
            result.put("AutoReportGps", serviceContext.getPreferenceManager().getAutoReportGps());
            result.put("ReportIntervalGps", serviceContext.getPreferenceManager().getReportIntervalGps());
            result.put("AutoReportWifi", serviceContext.getPreferenceManager().getAutoReportWifi());
            result.put("ReportIntervalWifi", serviceContext.getPreferenceManager().getReportIntervalWifi());
            result.put("PowerSaving", serviceContext.getPreferenceManager().getPowerSaving());
            return createSuccessResponse(requestName, result);
        } catch (Exception e) {
            e.printStackTrace();
            return createFailedResponse(requestName, "InternalError");
        }
    }
}
