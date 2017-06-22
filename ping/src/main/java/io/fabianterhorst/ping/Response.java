package io.fabianterhorst.ping;

import okio.ByteString;

/**
 * Created by fabianterhorst on 18.06.17.
 */

public final class Response {

    public static final int UNKNOWN = -1;
    public static final int TIMEOUT = 0;
    public static final int OK = 1;
    public static final int UNKNOWN_HOST = 2;

    int status;

    long packageSize;

    ByteString domain;

    long icmpSequence;

    long ttl;

    double time;

    public int status() {
        return status;
    }

    public long packageSize() {
        return packageSize;
    }

    public ByteString domain() {
        return domain;
    }

    public long icmpSequence() {
        return icmpSequence;
    }

    public long ttl() {
        return ttl;
    }

    public double time() {
        return time;
    }

    //Todo: doc missing icmp because timeout contains one
    public boolean isSuccessful() {
        return packageSize != -1 && domain != null && ttl != -1 && time != -1;
    }
}
