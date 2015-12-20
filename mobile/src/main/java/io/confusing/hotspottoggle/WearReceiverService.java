package io.confusing.hotspottoggle;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WearReceiverService extends WearableListenerService {
    private static final String TOGGLE_AP = "/toggle-ap";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(TOGGLE_AP)) {
            ToggleIntentService.startActionToggle(getApplicationContext());
        }

    }
}
