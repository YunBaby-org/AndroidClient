package com.example.client.requests;

import android.util.Log;

import com.example.client.services.ForegroundService;
import com.example.client.services.ServiceState;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.client.services.ServiceEventLogger.Event;

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
    public JSONObject createResponse(ServiceState serviceState) {
        try {
            switch (Target) {
                case GPS:
                    serviceState.getPreferenceManager().setReportIntervalGps(interval);
                    ForegroundService.emitEvent(Event.Info("更新 GPS 回報間隔為 " + interval));
                    break;
                case WIFI:
                    serviceState.getPreferenceManager().setReportIntervalWifi(interval);
                    ForegroundService.emitEvent(Event.Info("更新 Wifi 回報間隔為 " + interval));
                    break;
                default:
                    Log.wtf("RequestSetReportInterval", "Unknown target");
                    throw new InvalidPayloadException();
            }
            return createSuccessResponse(requestName);
        } catch (ArithmeticException | InvalidPayloadException e) {
            return createFailedResponse(requestName, "Invalid Payload");
        } catch (Exception e) {
            return createFailedResponse(requestName, "Internal Error");
        }
    }

    public enum TargetType {
        GPS, WIFI
    }

    private class InvalidPayloadException extends Throwable {
    }
}
