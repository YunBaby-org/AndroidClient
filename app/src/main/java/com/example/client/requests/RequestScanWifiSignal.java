package com.example.client.requests;

import android.net.wifi.ScanResult;
import android.util.Log;

import com.example.client.manager.Managers;
import com.example.client.manager.WirelessSignalManager;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class RequestScanWifiSignal extends Request {

    public RequestScanWifiSignal() {
        this.requestName = "ScanWifiSignal";
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public void parseFromJSON(JSONObject request) throws InvalidRequestFormatException, JSONException {
        id = request.getString("id");
        requestName = request.getString("Request");
        if (!requestName.equals("ScanWifiSignal"))
            throw new InvalidRequestFormatException("Failed to parse such request");
    }

    @Override
    public JSONObject createResponse(Managers managers) {
        return createResponse(managers.getWirelessSignalManager(), null);
    }

    public JSONObject createResponse(WirelessSignalManager wirelessSignalManager, @Nullable WirelessSignalManager.WirelessScanResult pre_result) {
        try {
            WirelessSignalManager.WirelessScanResult wirelessScanResult = pre_result != null ? pre_result : wirelessSignalManager.getWirelessScanResult();

            /* Create JSON result */
            JSONObject result = new JSONObject();
            /* Create wifi array */
            JSONArray wifis = new JSONArray();

            /* Pack result into wifi array */
            for (ScanResult scan : wirelessScanResult.scanResults) {
                JSONObject entry = new JSONObject();
                entry.put("macAddress", scan.BSSID);
                entry.put("signalStrength", scan.level);
                entry.put("timestamp", wirelessScanResult.timestamp);
                entry.put("channel", WirelessSignalManager.frequencyToChannel(scan.frequency));
                wifis.put(entry);
            }

            result.put("Wifis", wifis);
            return createSuccessResponse(requestName, result);
        } catch (WirelessSignalManager.OutdatedWirelessResult e) {
            e.printStackTrace();
            return createFailedResponse(requestName, "Outdated Result");
        } catch (WirelessSignalManager.UnknownFrequencyException e) {
            e.printStackTrace();
            Log.e("RequestScanWifiSignal", "Unknown wireless frequency");
            return createFailedResponse(requestName, "Internal Failure");
        } catch (WirelessSignalManager.WirelessScanFailure | Exception e) {
            e.printStackTrace();
            return createFailedResponse(requestName, "Internal Failure");
        }
    }
}
