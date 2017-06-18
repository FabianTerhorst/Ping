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
        buffer.writeUtf8("PING google.com (216.58.207.174) 56(84) bytes of data.\n");
        buffer.writeUtf8("64 bytes from muc11s04-in-f14.1e100.net (216.58.207.174): icmp_seq=0 ttl=57 time=25.4 ms\n");
        buffer.writeUtf8("64 bytes from muc11s04-in-f14.1e100.net (216.58.207.174): icmp_seq=1 ttl=57 time=24.4 ms\n");
        buffer.writeUtf8("64 bytes from muc11s04-in-f14.1e100.net (216.58.207.174): icmp_seq=2 ttl=57 time=24.4 ms\n");
        buffer.writeUtf8("64 bytes from muc11s04-in-f14.1e100.net (216.58.207.174): icmp_seq=3 ttl=57 time=24.4 ms\n");
        buffer.writeUtf8("Request timeout for icmp_seq=4\n");

        Request request = new Request.Builder()
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
        });

        //readProcessStream(buffer);
    }
}
