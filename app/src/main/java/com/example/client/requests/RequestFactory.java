package com.example.client.requests;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

public class RequestFactory {

    /* We cannot do Reflection in Android */
    /* Since all the metadata get stripped during compilning? (maybe) */
    /* So We maintain a list of class instead :( */
    /* https://stackoverflow.com/questions/18377516/find-all-subclasses-of-a-given-class-android possible solution here but too lazy to copy paste it */
    /* TODO: Consider implement Reflection or other approach to get subtype of Request */
    private static Class<? extends Request>[] classRequests = new Class[]{
            RequestGetDeviceStatus.class,
            RequestGetPowerStatus.class,
            RequestGetVersion.class,
            RequestPing.class,
            RequestScanGPS.class,
            RequestScanWifiSignal.class,
            RequestSetAutoReport.class,
            RequestSetPowerSaving.class,
            RequestSetReportInterval.class
    };


    /**
     * attempts to parse the JSON request into suitable request object
     *
     * @param request The request object in JSON format
     * @return the request instance or null if the JSON object cannot be parse into any of these requests
     */
    public static Request parseJSONRequest(JSONObject request) {

        /* Attempts to create every request instance, and check if we can parse the JSON request into its format */
        for (Class<? extends Request> classRequest : classRequests) {
            try {
                Request requestInstance = classRequest.getConstructor().newInstance();
                requestInstance.parseFromJSON(request);
                return requestInstance;
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            } catch (JSONException | Request.InvalidRequestFormatException e) {
                /* Ignore such exception intentionally */
            }
        }

        /* This request is probably invalid because no request can fit in such format */
        return null;
    }
}
