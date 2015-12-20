package io.confusing.hotspottoggle;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;

public class ToggleIntentService extends IntentService {
    private static final String ACTION_TOGGLE = "io.confusing.hotspottoggle.action.TOGGLE";
    public enum WIFI_AP_STATE {
        WIFI_AP_STATE_DISABLING, WIFI_AP_STATE_DISABLED, WIFI_AP_STATE_ENABLING,  WIFI_AP_STATE_ENABLED, WIFI_AP_STATE_FAILED
    }

    public static boolean isWifiApEnabled(Context context) {
        return getWifiApState(context) == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED;
    }

    private static WIFI_AP_STATE getWifiApState(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        Method getWifiApState;
        int tmp = 0;
        try {
            getWifiApState = wifiManager.getClass().getMethod("getWifiApState", null);
            tmp = (Integer) getWifiApState.invoke(wifiManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Fix for Android 4
        if (tmp > 10) {
            tmp = tmp - 10;
        }
        return WIFI_AP_STATE.class.getEnumConstants()[tmp];
    }

    public static void startActionToggle(Context context) {
        Intent intent = new Intent(context, ToggleIntentService.class);
        intent.setAction(ACTION_TOGGLE);
        context.startService(intent);
    }

    public ToggleIntentService() {
        super("ToggleIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TOGGLE.equals(action)) {
                handleActionToggle();
            }
        }
    }

    @SuppressWarnings("NullArgumentToVariableArgMethod")
    private void handleActionToggle() {
        boolean apEnabled = !isWifiApEnabled(getApplicationContext());
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (apEnabled && wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED || wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            wifiManager.setWifiEnabled(false);
        }
        try {
            Method getWifiConfig = wifiManager.getClass().getMethod("getWifiApConfiguration", null);
            WifiConfiguration wifiConfiguration = (WifiConfiguration) getWifiConfig.invoke(wifiManager, null);
            Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();
            for (Method method : wmMethods) {
                if (method.getName().equals("setWifiApEnabled")) {
                    method.invoke(wifiManager, wifiConfiguration, apEnabled);
                    Log.i("ToggleIntentService", "enabling AP:" + apEnabled);
                    break;
                }
            }
            if (apEnabled) {
                ClientCheckIntentService.startActionCheck(getApplicationContext());
            } else {
                ClientCheckIntentService.unscheduleActionCheck(getApplicationContext());
                wifiManager.setWifiEnabled(true);
            }
            sendUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendUpdate() {

    }
}