package io.fabianterhorst.ping;

/**
 * Created by fabianterhorst on 18.06.17.
 */

public final class Response {

    final long packageSize;

    final String domain;

    final String ip;

    final long icmpSequence;

    final long ttl;

    final double time;

    Response(Builder builder) {
        this.packageSize = builder.packageSize;
        this.domain = builder.domain;
        this.ip = builder.ip;
        this.icmpSequence = builder.icmpSequence;
        this.ttl = builder.ttl;
        this.time = builder.time;
    }

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

    public static class Builder {

        long packageSize;

        String domain;

        String ip;

        long icmpSequence;

        long ttl;

        double time;

        public Builder() {
            packageSize = -1;
            icmpSequence = -1;
            ttl = -1;
            time = -1;
        }

        public Builder packageSize(long packageSize) {
            this.packageSize = packageSize;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder icmpSequence(long icmpSequence) {
            this.icmpSequence = icmpSequence;
            return this;
        }

        public Builder ttl(long ttl) {
            this.ttl = ttl;
            return this;
        }

        public Builder time(double time) {
            this.time = time;
            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }
}
