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

//Todo: change NEW_LINE to \r\n element
public class RealCall implements Call {

    private static final byte SPACE = ' ';
    private static final byte NEW_LINE = '\r';
    private static final byte OPENING_PARENTHESIS = '(';
    private static final byte CLOSING_PARENTHESIS = ')';
    private static final byte COLON = ':';

    private static final Options INIT_FIELDS = Options.of(
            ByteString.encodeUtf8("PING"),
            ByteString.encodeUtf8("unknown host")
    );

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
    public void execute(Callback callback) {
        try {
            process = processBuilder.start();
            canceled = false;
            BufferedSource source = Okio.buffer(Okio.source(process.getInputStream()));
            readProcessStream(source, callback);
            source.buffer().clear();//Todo: check, process will close all streams automatically
            //source.close();
        } catch (IOException io) {
            callback.onFailure(this, io);
        } finally {
            cancel();
        }
    }

    @Override
    public void enqueue(Callback callback) {
        ping.dispatcher().enqueue(new AsyncCall(callback));
    }

    public void cancel() {
        if (process != null && !canceled) {
            process.destroy();
            canceled = true;
        }
    }

    public boolean isCanceled() {
        return canceled;
    }

    //Todo: maybe prevent option reading for unknown response
    void readProcessStream(BufferedSource source, Callback callback) throws IOException {
        int status = readInitializationLine(source, callback);
        if (status == Response.OK) {
            long index;
            do {
                if (!source.request(1)) break;
                Response response = new Response();
                byte b = source.buffer().getByte(0);
                if (b >= '0' && b <= '9') {
                    response.packageSize = source.readDecimalLong();
                    source.skip(12); // Skip ' bytes from '
                    response.domain = source.readByteString(source.indexOf(COLON));
                    source.skip(1);// ':'
                    response.status = Response.OK;
                } else if (b == '-') {
                    source.skip(source.indexOf(NEW_LINE) + 2);
                    if (!source.request(1)) break;
                    b = source.buffer().getByte(0);
                    if (b >= '0' && b <= '9') {
                        long packagesTransmitted = source.readDecimalLong();
                        source.skip(22); //Skip ' packets transmitted, '
                        long packagesReceived = source.readDecimalLong();
                        source.skip(19); //Skip ' packets received, '
                        double packagesLostPercent = SourceDoubleUtils.readDecimalDouble(source);
                        source.skip(source.indexOf(NEW_LINE) + 2);
                        source.skip(32);//Skip 'round-trip min/avg/max/stddev = '
                        double min = SourceDoubleUtils.readDecimalDouble(source);
                        source.skip(1);// Skip '/'
                        double avg = SourceDoubleUtils.readDecimalDouble(source);
                        source.skip(1);// Skip '/'
                        double max = SourceDoubleUtils.readDecimalDouble(source);
                        source.skip(1);// Skip '/'
                        double stdDev = SourceDoubleUtils.readDecimalDouble(source);
                        source.skip(3); // Skip ' ms'
                        //Todo: check the time type, normally 'ms'
                        callback.onFinish(this, Response.OK, packagesTransmitted, packagesReceived,
                                packagesLostPercent, min, avg, max, stdDev);
                    } else {
                        callback.onFinish(this, Response.INVALID, 0, 0,
                                0, 0, 0, 0, 0);
                    }
                    return;
                } else {
                    index = source.indexOf(REQUEST_TIMEOUT);
                    if (index != -1) {
                        // Timeout Request
                        response.status = Response.TIMEOUT;
                        source.skip(index + REQUEST_TIMEOUT.size() + 4); // Skip 'Request timeout for'
                    } else {
                        // Unknown Response
                        response.status = Response.UNKNOWN;
                    }
                }
                readOptions(source, response);

                index = source.indexOf(NEW_LINE);
                source.skip(index + 2);// Skip '\r\n'

                callback.onResponse(this, response);
            } while (index != -1 && !canceled);
        }
        callback.onFinish(this, status == Response.OK ? Response.CANCELED : status,
                0, 0, 0, 0, 0, 0, 0);
    }

    private int readInitializationLine(BufferedSource source, Callback callback) throws IOException {
        int initField = source.select(INIT_FIELDS);
        if (initField == 0) {
            source.skip(1); // Skip space
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
        } else if (initField == 1) {
            return Response.UNKNOWN_HOST;
        } else {
            return Response.UNKNOWN;
        }
        return Response.OK;
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
            RealCall.this.execute(callback);
            ping.dispatcher().finished(this);
        }

        public Call get() {
            return RealCall.this;
        }
    }
}
