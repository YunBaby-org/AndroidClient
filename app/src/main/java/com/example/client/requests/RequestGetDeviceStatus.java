package com.example.client.requests;

import com.example.client.manager.Managers;

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
    public JSONObject createResponse(Managers managers) {
        try {
            JSONObject result = new JSONObject();
            result.put("TrackerID", managers.getPreferenceManager().getTrackerID());
            result.put("AutoReportGps", managers.getPreferenceManager().getAutoReportGps());
            result.put("ReportIntervalGps", managers.getPreferenceManager().getReportIntervalGps());
            result.put("AutoReportWifi", managers.getPreferenceManager().getAutoReportWifi());
            result.put("ReportIntervalWifi", managers.getPreferenceManager().getReportIntervalWifi());
            result.put("PowerSaving", managers.getPreferenceManager().getPowerSaving());
            return createSuccessResponse(requestName, result);
        } catch (Exception e) {
            e.printStackTrace();
            return createFailedResponse(requestName, "InternalError");
        }
    }
}
