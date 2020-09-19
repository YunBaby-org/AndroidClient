package com.example.client.requests;

import android.util.Log;

import com.example.client.manager.Managers;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestSetReportInterval extends Request {

    protected int interval;
    protected TargetType Target;

    @Override
    public void parseFromJSON(JSONObject request) throws InvalidRequestFormatException, JSONException {
        id = request.getString("id");
        requestName = request.getString("Request");
        if (!requestName.equals("SetReportInterval"))
            throw new InvalidRequestFormatException("Failed to parse such request");
        interval = request.getJSONObject("Payload").getInt("Interval");
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
                    managers.getPreferenceManager().setReportIntervalGps(interval);
                    break;
                case WIFI:
                    managers.getPreferenceManager().setReportIntervalWifi(interval);
                    break;
                default:
                    Log.wtf("RequestSetReportInterval", "Unknown target");
            }
            return createSuccessResponse(requestName);
        } catch (ArithmeticException e) {
            return createFailedResponse(requestName, "Invalid Payload");
        } catch (Exception e) {
            return createFailedResponse(requestName, "Internal Error");
        }
    }

    public enum TargetType {
        GPS, WIFI
    }
}
