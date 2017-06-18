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
    private static final byte OPENING_PARENTHESIS = '(';
    private static final byte CLOSING_PARENTHESIS = ')';
    private static final byte COLON = ':';

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

    private Response response = new Response();

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
            cancel();
        }
    }

    @Override
    public void enqueue(Callback callback) throws IOException {
        canceled = false;
        ping.dispatcher().enqueue(new AsyncCall(callback));
    }

    public void cancel() {
        if (process != null && !canceled) {
            process.destroy();
        }
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
            if (b >= '0' && b <= '9') {
                response.packageSize = source.readDecimalLong();
                source.skip(12); // Skip ' bytes from '
                response.domain = source.readByteString(source.indexOf(COLON));
                source.skip(1);// ':'
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
            readOptions(source, response);

            index = source.indexOf(NEW_LINE);
            source.skip(index + 2);// Skip '\r\n'

            callback.onResponse(this, response);
        } while (index != -1 && !canceled);
        callback.onFinish(this);
    }

    private void readInitializationLine(BufferedSource source, Callback callback) throws IOException {
        source.skip(5); // 'PING '
        String domain = source.readUtf8(source.indexOf(SPACE));
        long index = source.indexOf(OPENING_PARENTHESIS);
        final String ip;
        if (index != -1) {
            source.skip(index + 1);
            ip = source.readUtf8(source.indexOf(CLOSING_PARENTHESIS));
            source.skip(2); // ') '
        } else {
            ip = null;
            source.skip(1); // Skip space
        }
        long packageSize = source.readDecimalLong();
        index = source.indexOf(OPENING_PARENTHESIS);
        final long realPackageSize;
        if (index != -1) {
            source.skip(index + 1);
            realPackageSize = source.readDecimalLong();
        } else {
            realPackageSize = -1;
        }
        source.skip(source.indexOf(NEW_LINE) + 2); // Skip remaining line
        callback.onStart(this, domain, ip, packageSize, realPackageSize);
    }

    private void readOptions(BufferedSource source, Response response) throws IOException {
        int select;
        while ((select = source.select(FIELDS)) != -1) {
            switch (select) {
                case 0: // icmp_seq
                    response.icmpSequence = source.readDecimalLong();
                    break;
                case 1: // ttl
                    response.ttl = source.readDecimalLong();
                    break;
                case 2: // time
                    response.time = SourceDoubleUtils.readDecimalDouble(source); //Todo: add time type (ms)
                    source.skip(3); // Skip ' ms'
                    break;
            }
        }
    }

    final class AsyncCall implements Runnable {

        private final Callback callback;

        private final String name;

        AsyncCall(Callback callback) {
            this.callback = callback;
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
                process = processBuilder.start();
                BufferedSource source = Okio.buffer(Okio.source(process.getInputStream()));
                readProcessStream(source, callback);
                source.close();
            } catch (IOException io) {
                callback.onFailure(RealCall.this, io);
            } finally {
                cancel();
                ping.dispatcher().finished(this);
            }
        }

        public Call get() {
            return RealCall.this;
        }
    }
}
