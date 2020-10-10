package com.example.client.requests;

import android.util.Log;

import com.example.client.services.ForegroundService;
import com.example.client.services.ServiceContext;

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
    public JSONObject createResponse(ServiceContext serviceContext) {
        try {
            switch (Target) {
                case GPS:
                    serviceContext.getPreferenceManager().setAutoReportGps(Enable);
                    ForegroundService.emitEvent(ForegroundService.EventLevel.Info, "GPS自動回報 " + (Enable ? "開啟" : "關閉"));
                    break;
                case WIFI:
                    serviceContext.getPreferenceManager().setAutoReportWifi(Enable);
                    ForegroundService.emitEvent(ForegroundService.EventLevel.Info, "Wifi自動回報" + (Enable ? "開啟" : "關閉"));
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
