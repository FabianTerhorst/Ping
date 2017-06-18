package io.fabianterhorst.ping;

/**
 * Created by fabianterhorst on 18.06.17.
 */

public final class Response {

    long packageSize;

    String domain;

    String ip;

    long icmpSequence;

    long ttl;

    double time;

    public long packageSize() {
        return packageSize;
    }

    public String domain() {
        return domain;
    }

    public String ip() {
        return ip;
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
        return packageSize != -1 && domain != null && ip != null
                && ttl != -1 && time != -1;
    }
}
