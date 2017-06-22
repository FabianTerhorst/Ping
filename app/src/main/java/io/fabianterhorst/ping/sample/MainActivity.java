package io.fabianterhorst.ping.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import io.fabianterhorst.ping.Call;
import io.fabianterhorst.ping.Callback;
import io.fabianterhorst.ping.Ping;
import io.fabianterhorst.ping.Request;
import io.fabianterhorst.ping.Response;
import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
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
                    Log.d("ping", response.time() + " " + response.icmpSequence() + " " + response.domain().utf8());
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

    ByteString END = ByteString.encodeUtf8(" \n");

    //Todo : use the first row to determinate the size that is needed and create an two dimensional array with it

    //Todo: maybe first new line check could be more efficient
    //Todo: make more stable
    private void mac() throws IOException {
        long time = System.nanoTime();
        getMacFromArpCache("192.168.178.133");
        Log.d("time", String.valueOf(System.nanoTime() - time));
        BufferedSource source = Okio.buffer(Okio.source(new File("/proc/net/arp")));
        Buffer buffer = source.buffer();
        time = System.nanoTime();
        long p = 0;
        long index;
        long column = 0;
        // Skip first row
        source.skip(source.indexOf((byte)'\n') + 1);
        List<ArpRow> rows = new LinkedList<>();
        ArpRow row = null;
        while (source.request(p + 1)) {
            int c = buffer.getByte(p++);
            if (c == ' ') continue;
            if (c == '\n') {
                rows.add(row);
                column = 0;
                continue;
            }
            buffer.skip(p - 1);
            index = source.indexOfElement(END);
            if (source.request(index + 2)
                    && buffer.getByte(index) != '\n'
                    && buffer.getByte(index + 1) != ' ') {
                index = source.indexOf((byte) ' ', index + 2);//Was index +1 before maybe just indexOfElement as well?
            }
            p = 0;
            if (column == 0) {
                row = new ArpRow();
                row.ipAddress = source.readByteString(index);
            } else if (column == 1) {
                row.hwType = source.readByteString(index);
            } else if (column == 2) {
                row.flags = source.readByteString(index);
            } else if (column == 3) {
                row.hwAddress = source.readByteString(index);
            }else if (column == 4) {
                row.mask = source.readByteString(index);
            } else if (column == 5) {
                row.device = source.readByteString(index);
            }
            column++;
        }
        Log.d("time", String.valueOf(System.nanoTime() - time));
        for (ArpRow arpRow : rows) {
            Log.d("row", arpRow.ipAddress + " " + arpRow.hwType
                    + " " + arpRow.flags + " " + arpRow.hwAddress
                    + " " + arpRow.mask + " " + arpRow.device);
        }
    }
}
