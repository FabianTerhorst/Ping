package io.fabianterhorst.ping.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import io.fabianterhorst.ping.Call;
import io.fabianterhorst.ping.Callback;
import io.fabianterhorst.ping.Ping;
import io.fabianterhorst.ping.Request;
import io.fabianterhorst.ping.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            Request request = new Request.Builder()
                    .destination("google.com")
                    .count(10)
                    //Todo: retryOnError bool when line is invalid
                    .build();
            Ping ping = new Ping();
            ping.newCall(request).enqueue(new Callback() {
                @Override
                public void onStart(Call call, String domain, String ip, long packageSize, long realPackageSize) {
                    Log.d("start", domain + " " + ip + " " + packageSize + " " + realPackageSize);
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("ping", response.time() + " " + response.icmpSequence());
                }

                @Override
                public void onFinish(Call call) {

                }
            });
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}
