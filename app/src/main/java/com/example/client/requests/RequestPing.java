package com.example.client.requests;

import com.example.client.manager.Managers;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestPing extends Request {
    @Override
    public void parseFromJSON(JSONObject request) throws InvalidRequestFormatException, JSONException {
        requestName = request.getString("Request");
        if (!requestName.equals("Ping"))
            throw new InvalidRequestFormatException("Failed to parse such request");
    }

    @Override
    public JSONObject createResponse(Managers managers) {
        return createSuccessResponse(requestName, null, true);
    }
}
