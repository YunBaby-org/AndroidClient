package com.example.client.requests;

import com.example.client.manager.Managers;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestSetReportInterval extends Request {

    protected int interval;

    @Override
    public void parseFromJSON(JSONObject request) throws InvalidRequestFormatException, JSONException {
        requestName = request.getString("Request");
        if (!requestName.equals("SetReportInterval"))
            throw new InvalidRequestFormatException("Failed to parse such request");
        interval = request.getJSONObject("Payload").getInt("Interval");
    }

    @Override
    public JSONObject createResponse(Managers managers) {
        try {
            managers.getPreferenceManager().setReportInterval(interval);
            return createSuccessResponse(requestName);
        } catch (ArithmeticException e) {
            return createFailedResponse(requestName, "Invalid Payload");
        } catch (Exception e) {
            return createFailedResponse(requestName, "Internal Error");
        }
    }
}
