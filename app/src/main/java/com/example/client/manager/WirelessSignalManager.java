package com.example.client.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WirelessSignalManager {

    private Context context;
    private WifiManager wifiManager;
    private static final long WIRELESS_SCANNING_REFRESH_THRESHOLD = 60 * 1000;
    private long lastWirelessScanning = 0;
    private BroadcastReceiver broadcastReceiver;

    public WirelessSignalManager(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public void setupWifiScanReceiver(@Nullable BroadcastReceiver innerCallback) {
        /* We need to manage the logic of callback */
        BroadcastReceiver callback = getWirelessScanResultReceiver(innerCallback);

        /* Register receiver */
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(callback, intentFilter);
    }

    public void stop() {
        context.unregisterReceiver(getWirelessScanResultReceiver(null));
    }

    @NotNull
    private BroadcastReceiver getWirelessScanResultReceiver(@Nullable BroadcastReceiver innerCallback) {
        /* TODO: Fix the potential race condition here */
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                    if (success) {
                        lastWirelessScanning = System.currentTimeMillis();
                        if (innerCallback != null)
                            innerCallback.onReceive(context, intent);
                    } else {
                        Log.e("WirelessScanning", "Failed to scan requests");
                    }
                }
            };
        }
        return broadcastReceiver;
    }

    public WirelessScanResult getWirelessScanResult() throws OutdatedWirelessResult, WirelessScanFailure {
        if (!wifiManager.startScan())
            throw new WirelessScanFailure();

        /* Sleep for a while hope we got the result */
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* Only accept result within the specific window time */
        long lastScanTime = lastWirelessScanning;
        if ((System.currentTimeMillis() - lastScanTime) <= WIRELESS_SCANNING_REFRESH_THRESHOLD) {
            return new WirelessScanResult(wifiManager.getScanResults(), lastScanTime);
        }

        throw new OutdatedWirelessResult();
    }

    public static class WirelessScanResult {
        public List<ScanResult> scanResults;

        public WirelessScanResult(List<ScanResult> scanResults, long timestamp) {
            this.scanResults = scanResults;
            this.timestamp = timestamp;
        }

        public long timestamp;
    }

    public static class OutdatedWirelessResult extends Throwable {
        public OutdatedWirelessResult() {
        }
    }

    public static class WirelessScanFailure extends Throwable {
        public WirelessScanFailure() {
        }
    }

    public static int frequencyToChannel(int f) throws UnknownFrequencyException {
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
        throw new UnknownFrequencyException("Unknow frequency " + f);
    }

    public static class UnknownFrequencyException extends Throwable {
        public UnknownFrequencyException(String s) {
            super(s);
        }
    }
}
