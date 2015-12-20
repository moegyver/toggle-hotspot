package io.confusing.hotspottoggle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CheckReceiver extends BroadcastReceiver {
    public CheckReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("CheckReceiver", "received intent.");
        Intent checkIntent = new Intent(context, ClientCheckIntentService.class);
        context.startService(checkIntent);
    }
}
