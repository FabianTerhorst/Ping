package io.fabianterhorst.ping.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import io.fabianterhorst.ping.Call;
import io.fabianterhorst.ping.Callback;
import io.fabianterhorst.ping.Ping;
import io.fabianterhorst.ping.Request;
import io.fabianterhorst.ping.Response;
import okio.BufferedSource;
import okio.Okio;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            mac();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Ping ping = new Ping();
        try {
            Request request = new Request.Builder()
                    .destination("google.com")
                    .count(10)
                    //Todo: retryOnError bool when line is invalid
                    .build();
            ping.newCall(request).enqueue(new Callback() {
                @Override
                public void onStart(Call call, String domain, String ip, long packageSize, long realPackageSize) {
                    Log.d("ping", "start " + domain + " " + ip + " " + packageSize + " " + realPackageSize);
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("ping", response.time() + " " + response.icmpSequence()  + " " + response.domain().utf8());
                }

                @Override
                public void onFinish(Call call) {
                    Log.d("ping", "finish");
                }
            });
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    @Nullable
    static String getMacFromArpCache(String ip) {
        if (ip == null)
            return null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted.length >= 4 && ip.equals(splitted[0])) {
                    // Basic sanity check
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        return mac;
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void mac() throws IOException {
        BufferedSource source = Okio.buffer(Okio.source(new File("/proc/net/arp")));
        Log.d("bla", source.readUtf8());

    }
}
