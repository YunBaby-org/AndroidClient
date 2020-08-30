package com.example.client.requests;

import android.location.Location;
import android.util.Log;

import com.example.client.manager.GpsLocationManager;
import com.example.client.manager.Managers;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class RequestScanGPS extends Request {

    public RequestScanGPS() {
        this.requestName = "ScanGPS";
    }

    @Override
    public void parseFromJSON(JSONObject request) throws InvalidRequestFormatException, JSONException {
        requestName = request.getString("Request");
        if (!requestName.equals("ScanGPS"))
            throw new InvalidRequestFormatException("Failed to parse such request");
    }

    @Override
    public JSONObject createResponse(Managers managers) {
        return createResponse(managers.getGpsLocationManager());
    }

    public JSONObject createResponse(GpsLocationManager gpsLocationManager) {
        try {
            Location location = gpsLocationManager.getLastLocation();
            JSONObject result = new JSONObject();
            result.put("Longitude", location.getLongitude());
            result.put("Latitude", location.getLatitude());
            return createSuccessResponse(requestName, result);
        } catch (TimeoutException e) {
            e.printStackTrace();
            Log.e("RequestScanGPS", "Location timeout");
            return createFailedResponse(requestName, "Operation Timeout");
        } catch (GpsLocationManager.OutdatedLocationException e) {
            e.printStackTrace();
            Log.e("RequestScanGPS", "Out-dated location");
            return createFailedResponse(requestName, "Service Not Ready");
        } catch (ExecutionException e) {
            e.printStackTrace();
            if (e.getCause() instanceof SecurityException) {
                Log.e("RequestScanGPS", "Require location permission");
                return createFailedResponse(requestName, "Permission Denied");
            } else {
                return createFailedResponse(requestName, "Internal Error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return createFailedResponse(requestName, "Internal Error");
        }
    }
}
