package com.example.client.requests;

import android.util.Log;

import com.example.client.manager.Managers;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestSetAutoReport extends Request {

    protected boolean Enable;
    protected TargetType Target;

    @Override
    public void parseFromJSON(JSONObject request) throws InvalidRequestFormatException, JSONException {
        id = request.getString("id");
        requestName = request.getString("Request");
        if (!requestName.equals("SetAutoReport"))
            throw new InvalidRequestFormatException("Failed to parse such request");
        Enable = request.getJSONObject("Payload").getBoolean("Enable");
        JSONObject payload = request.getJSONObject("Payload");
        if (payload.isNull("Target"))
            payload.put("Target", "GPS");
        Target = Enum.valueOf(TargetType.class, payload.getString("Target"));
    }

    @Override
    public JSONObject createResponse(Managers managers) {
        try {
            switch (Target) {
                case GPS:
                    managers.getPreferenceManager().setAutoReportGps(Enable);
                    break;
                case WIFI:
                    managers.getPreferenceManager().setAutoReportWifi(Enable);
                    break;
                default:
                    Log.wtf("RequestSetReportInterval", "Unknown target");
            }
            return createSuccessResponse(requestName);
        } catch (Exception e) {
            return createFailedResponse(requestName, "Internal Error");
        }
    }

    public enum TargetType {
        GPS, WIFI
    }
}
