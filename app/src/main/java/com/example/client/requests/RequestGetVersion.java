package com.example.client.requests;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.example.client.services.ServiceContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class RequestGetVersion extends Request {
    @Override
    public void parseFromJSON(JSONObject request) throws JSONException, InvalidRequestFormatException {
        id = request.getString("id");
        requestName = request.getString("Request");
        if (!requestName.equals("GetVersion"))
            throw new InvalidRequestFormatException("Failed to parse such request");
    }

    @Override
    public JSONObject createResponse(ServiceContext serviceContext) {
        try {
            Context context = serviceContext.getContext();
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            JSONObject result = new JSONObject();
            result.put("Version", String.format(Locale.TAIWAN, "%s(%s), %d", info.packageName, info.versionName, info.lastUpdateTime));

            return createSuccessResponse(requestName, result);
        } catch (Exception e) {
            e.printStackTrace();
            return createFailedResponse(requestName, "Internal Error");
        }
    }
}
