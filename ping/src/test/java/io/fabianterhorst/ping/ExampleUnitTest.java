package io.fabianterhorst.ping;

import org.junit.Test;

import java.io.IOException;

import okio.Buffer;

/**
 * //https://gist.github.com/kbaribeau/4495181
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        Buffer buffer = new Buffer();
        buffer.writeUtf8("PING google.com (216.58.207.174) 56(84) bytes of data.\r\n");
        buffer.writeUtf8("64 bytes from muc11s04-in-f14.1e100.net (216.58.207.174): icmp_seq=0 ttl=57 time=25.4 ms\r\n");
        buffer.writeUtf8("64 bytes from muc11s04-in-f14.1e100.net (216.58.207.174): icmp_seq=1 ttl=57 time=24.4 ms\r\n");
        buffer.writeUtf8("64 bytes from muc11s04-in-f14.1e100.net (216.58.207.174): icmp_seq=2 ttl=57 time=24.4 ms\r\n");
        buffer.writeUtf8("64 bytes from muc11s04-in-f14.1e100.net (216.58.207.174): icmp_seq=3 ttl=57 time=24.4 ms\r\n");
        buffer.writeUtf8("Request timeout for icmp_seq=4\r\n");
        buffer.writeUtf8("test\r\n");
        buffer.writeUtf8("\r\n");
        buffer.writeUtf8("Request timeout for icmp_seq=6\r\n");
        buffer.writeUtf8("--- google.com ping statistics ---\r\n");
        buffer.writeUtf8("2 packets transmitted, 2 packets received, 0.0% packet loss\r\n");
        buffer.writeUtf8("round-trip min/avg/max/stddev = 20.040/20.926/21.813/0.887 ms\r\n");

        Ping ping = new Ping();
        RealCall realCall= new RealCall(ping, null, null);

        Callback callback = new Callback() {
            @Override
            public void onStart(Call call, String domain, String ip, long packageSize, long realPackageSize) {
                System.out.println(domain + " " + ip + " " + packageSize + " " + realPackageSize);
            }

            @Override
            public void onFinish(Call call, int status, long packagesTransmitted, long packagesReceived, double packagesLostPercent, double min, double avg, double max, double stdDev) {

            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.domain() + " " + response.icmpSequence() + " " + response.status() + " " + response.packageSize());
            }
        };

        realCall.readProcessStream(buffer, callback);


        /*Request request = new Request.Builder()
                .destination("google.com")
                .count(1)
                .build();

        new Ping().newCall(request).enqueue(new Callback() {

            @Override
            public void onStart(Call call, String domain, String ip, long packageSize, long realPackageSize) {

            }

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }

            @Override
            public void onFinish(Call call) {

            }
        });*/

        //readProcessStream(buffer);
    }
}
