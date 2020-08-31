package com.example.client.manager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Deprecated
public class PositionManager {
    private Context MyContext;
    private WifiManager wifiManager;
    private BroadcastReceiver wifiScanReceiver;
    private TelephonyManager telephonyManager;

    public PositionManager(Context context) {

        this.MyContext = context;
        this.wifiManager = (WifiManager) MyContext.getSystemService(Context.WIFI_SERVICE);
        this.telephonyManager = (TelephonyManager) MyContext.getSystemService(Context.TELEPHONY_SERVICE);

        /* Register a broadcast receiver for Wifi result */
        this.wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
            }
        };

    }

    public JSONObject ScanWifi() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        MyContext.registerReceiver(wifiScanReceiver, intentFilter);
        boolean success = wifiManager.startScan();
        if (success) {
            return ScanSuccess();
        } else {
            return ScanFailure();
        }
    }

    private JSONObject ScanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();
        JSONObject scan_result = CreateWifiJson(results);
        Log.d("scanwifi",CreateWifiJson(results).toString());

        return scan_result;
    }

    private JSONObject ScanFailure() {
        Log.d("scanwifi", "ScanFailure");
        JSONObject scan_rsult = new JSONObject();
        try {
            scan_rsult.put("error", "scan failure");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return scan_rsult;

    }


    private JSONObject CreateWifiJson(List<ScanResult> results) {
        JSONObject json = new JSONObject();
        Log.d("scanwif", "creating wifi json ");
        try {

            String mccmnc = telephonyManager.getNetworkOperator();
            // mccmnc 通常會得到該裝置的 MobileCountryCode 和 MobileNetworkCode
            // 但是有一些裝置(如平板，他沒有 SIM 卡因此他不會返回某些數值，這時我們會需要偽造他
            if (mccmnc.length() == 5) {
                json.put("homeMobileCountryCode", mccmnc.substring(0, 3));
                json.put("homeMobileNetworkCode", mccmnc.substring(3));
            } else {
                json.put("homeMobileCountryCode", "466");
                json.put("homeMobileNetworkCode", "11");
            }

            if (ActivityCompat.checkSelfPermission(MyContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // here to request the missing permissions, and then overriding public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation for ActivityCompat#requestPermissions for more details.
            }

            json.put("radioType", toNetworkTypeString(telephonyManager.getDataNetworkType()));
            json.put("carrier", telephonyManager.getSimOperatorName());
            json.put("considerIp", false);
            json.put("cellTowers", new JSONArray());
            JSONArray json_wifi = new JSONArray();
            for (ScanResult sr : results) {
                JSONObject wifi = new JSONObject();
                wifi.put("macAddress", sr.BSSID);
                wifi.put("signalStrength", sr.level);
                wifi.put("age", SystemClock.elapsedRealtime() - sr.timestamp / 1000);
                wifi.put("channel", frequencyToChannel(sr.frequency));
                wifi.put("signalToNoiseRatio", 0);
                json_wifi.put(wifi);
            }
            json.put("wifiAccessPoints", json_wifi);
        } catch (Exception ex) {
            Log.d("test", ex + "get json error");
        }

        return json;
    }

    private String toNetworkTypeString(int value) {
        return value == TelephonyManager.NETWORK_TYPE_LTE ? "lte" :
                value == TelephonyManager.NETWORK_TYPE_GSM ? "gsm" :
                        value == TelephonyManager.NETWORK_TYPE_CDMA ? "cdma" : "lte";
    }

    private double frequencyToChannel(int f) throws Exception {
        switch (f) {
            case 2401:
                return 1;
            case 2406:
                return 2;
            case 2411:
                return 3;
            case 2416:
                return 4;
            case 2421:
                return 5;
            case 2426:
                return 6;
            case 2431:
                return 7;
            case 2436:
                return 8;
            case 2441:
                return 9;
            case 2446:
                return 10;
            case 2451:
                return 11;
            case 2456:
                return 12;
            case 2461:
                return 13;
            case 2473:
                return 14;
            case 2412:
                return 1;
            case 2417:
                return 2;
            case 2422:
                return 3;
            case 2427:
                return 4;
            case 2432:
                return 5;
            case 2437:
                return 6;
            case 2442:
                return 7;
            case 2447:
                return 8;
            case 2452:
                return 9;
            case 2457:
                return 10;
            case 2462:
                return 11;
            case 2467:
                return 12;
            case 2472:
                return 13;
            case 2484:
                return 14;
            case 2423:
                return 1;
            case 2428:
                return 2;
            case 2433:
                return 3;
            case 2438:
                return 4;
            case 2443:
                return 5;
            case 2448:
                return 6;
            case 2453:
                return 7;
            case 2458:
                return 8;
            case 2463:
                return 9;
            case 2468:
                return 10;
            case 2478:
                return 12;
            case 2483:
                return 13;
            case 2495:
                return 14;
            case 5180:
                return 36;
            case 5200:
                return 40;
            case 5220:
                return 44;
            case 5240:
                return 48;
            case 5260:
                return 52;
            case 5280:
                return 56;
            case 5300:
                return 60;
            case 5320:
                return 64;
            case 5500:
                return 100;
            case 5520:
                return 104;
            case 5540:
                return 108;
            case 5560:
                return 112;
            case 5580:
                return 116;
            case 5600:
                return 120;
            case 5620:
                return 124;
            case 5640:
                return 128;
            case 5660:
                return 132;
            case 5680:
                return 136;
            case 5700:
                return 140;
            case 5745:
                return 149;
            case 5765:
                return 153;
            case 5785:
                return 157;
            case 5805:
                return 161;
            case 5825:
                return 165;
        }
        throw new Exception("Unknow frequency " + f);
    }
}

