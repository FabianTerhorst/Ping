package io.fabianterhorst.ping;

import java.io.IOException;

import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;
import okio.Options;
import okio.SourceDoubleUtils;

/**
 * Created by fabianterhorst on 18.06.17.
 */

public class RealCall implements Call {

    private static final byte SPACE = ' ';
    private static final byte NEW_LINE = '\n';
    private static final byte CLOSING_PARENTHESIS = ')';

    private static final Options FIELDS = Options.of(
            ByteString.encodeUtf8(" icmp_seq="),
            ByteString.encodeUtf8(" ttl="),
            ByteString.encodeUtf8(" time=")
    );

    private static final ByteString REQUEST_TIMEOUT = ByteString.encodeUtf8("Request timeout");

    private ProcessBuilder processBuilder;

    private Process process;

    private boolean canceled;

    private Request originalRequest;

    private Ping ping;

    RealCall(Ping ping, Request originalRequest, ProcessBuilder processBuilder) {
        this.ping = ping;
        this.originalRequest = originalRequest;
        this.processBuilder = processBuilder;
    }

    @Override
    public void execute(Callback callback) throws IOException {
        process = processBuilder.start();
        canceled = false;
        try {
            BufferedSource source = Okio.buffer(Okio.source(process.getInputStream()));
            readProcessStream(source, callback);
            source.close();
        } catch (IOException io) {
            callback.onFailure(this, io);
        } finally {
            if (!canceled) {
                process.destroy();
            }
        }
    }

    @Override
    public void enqueue(Callback callback) throws IOException {
        process = processBuilder.start();
        canceled = false;
        ping.dispatcher().enqueue(new AsyncCall(callback));
    }

    public void cancel() {
        process.destroy();
        canceled = true;
    }

    public boolean isCanceled() {
        return canceled;
    }

    //Todo: maybe prevent option reading for unknown response
    private void readProcessStream(BufferedSource source, Callback callback) throws IOException {
        readInitializationLine(source, callback);
        long index;
        do {
            if (!source.request(1)) break;
            byte b = source.buffer().getByte(0);
            Response.Builder responseBuilder = new Response.Builder();
            if (b >= '0' && b <= '9') {
                responseBuilder.packageSize(source.readDecimalLong());
                source.skip(12); // Skip ' bytes from '
                responseBuilder.domain(source.readUtf8(source.indexOf(SPACE)));
                source.skip(2); //Skip ' ('
                responseBuilder.ip(source.readUtf8(source.indexOf(CLOSING_PARENTHESIS)));
                source.skip(2); // Skip '):
            } else if (b == '-') {
                //Todo: build summary
                break;
            } else {
                index = source.indexOf(REQUEST_TIMEOUT);
                if (index != -1) {
                    // Timeout Request
                    source.skip(index + REQUEST_TIMEOUT.size() + 4); // Skip 'Request timeout for'
                } else {
                    // Unknown Response
                }
            }
            readOptions(source, responseBuilder);

            index = source.indexOf(NEW_LINE);
            source.skip(index + 2);// Skip '\r\n'

            callback.onResponse(this, responseBuilder.build());
        } while (index != -1 && !canceled);
        callback.onFinish(this);
    }

    private void readInitializationLine(BufferedSource source, Callback callback) throws IOException {
        source.skip(5); // 'PING '
        String domain = source.readUtf8(source.indexOf(SPACE));
        source.skip(2); // ' ('
        String ip = source.readUtf8(source.indexOf(CLOSING_PARENTHESIS));
        source.skip(2); // ') '
        long packageSize = source.readDecimalLong();
        source.skip(1); // '('
        long realPackageSize = source.readDecimalLong();
        source.skip(source.indexOf(NEW_LINE) + 2); // Skip remaining line
        callback.onStart(this, domain, ip, packageSize, realPackageSize);
    }

    private void readOptions(BufferedSource source, Response.Builder builder) throws IOException {
        int select;
        while ((select = source.select(FIELDS)) != -1) {
            switch (select) {
                case 0: // icmp_seq
                    builder.icmpSequence(source.readDecimalLong());
                    break;
                case 1: // ttl
                    builder.ttl(source.readDecimalLong());
                    break;
                case 2: // time
                    builder.time(SourceDoubleUtils.readDecimalDouble(source)); //Todo: add time type (ms)
                    source.skip(3); // Skip ' ms'
                    break;
            }
        }
    }

    final class AsyncCall implements Runnable {

        private final Callback callback;

        private final BufferedSource source;

        private final String name;

        AsyncCall(Callback callback) {
            this.callback = callback;
            this.source = Okio.buffer(Okio.source(process.getInputStream()));
            this.name = "ping-" + originalRequest.destination;
        }

        @Override
        public final void run() {
            String oldName = Thread.currentThread().getName();
            Thread.currentThread().setName(name);
            try {
                execute();
            } finally {
                Thread.currentThread().setName(oldName);
            }
        }

        private void execute() {
            try {
                readProcessStream(source, callback);
                source.close();
            } catch (IOException io) {
                callback.onFailure(RealCall.this, io);
            } finally {
                if (!canceled) {
                    process.destroy();
                }
                ping.dispatcher().finished(this);
            }
        }

        public Call get() {
            return RealCall.this;
        }
    }
}
