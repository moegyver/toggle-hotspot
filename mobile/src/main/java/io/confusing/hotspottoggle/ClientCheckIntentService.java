package io.confusing.hotspottoggle;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

public class ClientCheckIntentService  extends IntentService {
    private static final String ACTION_CHECK = "io.confusing.hotspottoggle.action.CHECK";
    private int reachableTimeout = 300;
    SharedPreferences prefs;

    public static void startActionCheck(Context context) {
        Log.i("ClientCheckIntentServic", "scheduling");
        Intent intent = new Intent(ACTION_CHECK);
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 42, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 60 * 1000, 60 * 1000, alarmIntent);
    }

    public static void unscheduleActionCheck(Context context) {
        Log.i("ClientCheckIntentServic", "unscheduling");
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ACTION_CHECK);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 42, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.cancel(alarmIntent);
    }

    public ClientCheckIntentService() {
        super("ClientCheckIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("ClientCheckIntentServic", "got intent");
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        handleActionCheck();
    }

    private void handleActionCheck() {
        Log.i("ClientCheckIntentServic", "running check");
        BufferedReader br = null;
        int clients = 0;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");

                if ((splitted != null) && (splitted.length >= 4)) {
                    // Basic sanity check
                    String mac = splitted[3];

                    if (mac.matches("..:..:..:..:..:..")) {
                        boolean isReachable = InetAddress.getByName(splitted[0]).isReachable(reachableTimeout);

                        if (isReachable) {
                            clients++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(this.getClass().toString(), e.toString());
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                Log.e(this.getClass().toString(), e.getMessage());
            }
        }
        if (clients == 0) {
            int tries = prefs.getInt("tries", 0);
            if (tries >= 2 && ToggleIntentService.isWifiApEnabled(getApplicationContext())) {
                Log.i("ClientCheckIntentServic", clients + " clients, " + tries + ", killing AP.");
                ToggleIntentService.startActionToggle(getApplicationContext());
                prefs.edit().putInt("tries", 0).commit();
            } else {
                Log.i("ClientCheckIntentServic", clients + " clients, " + tries + ", rescheduling.");
                prefs.edit().putInt("tries", ++tries).commit();
            }
        } else {
            Log.i("ClientCheckIntentServic", clients + " clients, rescheduling.");
            prefs.edit().putInt("tries", 0).commit();
        }
    }
}
