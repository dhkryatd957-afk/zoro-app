package com.zoro.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.content.ContextCompat;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        Intent s = new Intent(c, ZoroService.class);
        if (Build.VERSION.SDK_INT >= 26) ContextCompat.startForegroundService(c, s);
        else c.startService(s);
    }
}
