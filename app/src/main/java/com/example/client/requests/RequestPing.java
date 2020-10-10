package com.example.client.requests;

import com.example.client.services.ServiceContext;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestPing extends Request {
    @Override
    public void parseFromJSON(JSONObject request) throws InvalidRequestFormatException, JSONException {
        id = request.getString("id");
        requestName = request.getString("Request");
        if (!requestName.equals("Ping"))
            throw new InvalidRequestFormatException("Failed to parse such request");
    }

    @Override
    public JSONObject createResponse(ServiceContext serviceContext) {
        return createSuccessResponse(requestName, null, true);
    }
}
