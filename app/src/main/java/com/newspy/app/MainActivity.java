package com.newspy.app;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.OutputStream;
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
        b1.setText("1) رفع الجهاز");
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { upload(); }
        });
        ll.addView(b1);

        setContentView(sv);
    }

    private String getDeviceId() {
        String id = getSharedPreferences("x", MODE_PRIVATE).getString("did", null);
        if (id == null) {
            id = UUID.randomUUID().toString();
            getSharedPreferences("x", MODE_PRIVATE).edit().putString("did", id).apply();
        }
        return id;
    }

    private void append(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() { log.append("\n" + s); }
        });
    }

    private void upload() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject o = new JSONObject();
                    o.put("device_id", getDeviceId());
                    o.put("model", Build.MODEL);
                    o.put("manufacturer", Build.MANUFACTURER);
                    o.put("android_version", Build.VERSION.RELEASE);
                    o.put("status", "online");

                    boolean ok = post("devices", o);
                    append(ok ? "OK: uploaded" : "FAILED");
                } catch (Exception e) {
                    append("ERR: " + e.toString());
                }
            }
        }).start();
    }

    private boolean post(String table, JSONObject data) {
        try {
            URL url = new URL(SB_URL + "/rest/v1/" + table);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
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
            append("POST err: " + e.toString());
            return false;
        }
    }
}
