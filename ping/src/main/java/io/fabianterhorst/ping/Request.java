package io.fabianterhorst.ping;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by fabianterhorst on 18.06.17.
 */

public final class Request {

    final String destination;

    final int count;

    final boolean broadcast;

    final float interval;

    final int ttl;

    final int packetSize;

    final int deadline;

    final int timeout;

    Request(Builder builder) {
        this.destination = builder.destination;
        this.count = builder.count;
        this.broadcast = builder.broadcast;
        this.interval = builder.interval;
        this.ttl = builder.ttl;
        this.packetSize = builder.packetSize;
        this.deadline = builder.deadline;
        this.timeout = builder.timeout;
    }

    public List<String> commands() {
        List<String> commands = new LinkedList<>();
        commands.add("/system/bin/ping");
        if (count != -1) {
            commands.add("-c " + count);
        }
        if (broadcast) {
            commands.add("-b");
        }
        if (interval != -1) {
            commands.add("-i " + interval);
        }
        if (ttl != -1) {
            commands.add("-t " + ttl);
        }
        if (packetSize != -1) {
            commands.add("-s " + ttl);
        }
        if (deadline != -1) {
            commands.add("-w " + deadline);
        }
        if (timeout != -1) {
            commands.add("-W " + timeout);
        }
        commands.add(destination);
        return commands;
    }

    public static class Builder {

        String destination;

        int count;

        boolean broadcast;

        int interval;

        int ttl;

        int packetSize;

        int deadline;

        int timeout;

        public Builder() {
            count = -1;
            broadcast = false;
            interval = -1;
            ttl = -1;
            packetSize = -1;
            deadline = -1;
            timeout = -1;
        }

        /**
         * Ping destination address.
         */
        public Builder destination(String destination) {
            if (destination == null) throw new NullPointerException("destination == null");
            this.destination = destination;
            return this;
        }

        /**
         * Stop after sending count ECHO_REQUEST packets.
         * With deadline option, ping waits for count ECHO_REPLY packets, until the timeout expires.
         */
        public Builder count(int count) {
            if (count < 1) throw new IllegalArgumentException("count < 1");
            this.count = count;
            return this;
        }

        /**
         * Allow pinging a broadcast address.
         */
        public Builder broadcast(boolean broadcast) {
            this.broadcast = broadcast;
            return this;
        }

        /**
         * Wait interval seconds between sending each packet.
         * The default is to wait for one second between each packet normally, or not to wait in flood mode.
         * Only super-user may set interval to values less 0.2 seconds.
         */
        public Builder interval(int interval) {
            if (interval < 0.2) throw new IllegalArgumentException("interval < 0.2 seconds");
            this.interval = interval;
            return this;
        }

        /**
         * Set the IP Time to Live.
         */
        public Builder ttl(int ttl) {
            if (ttl < 1) throw new IllegalArgumentException("ttl < 1");
            this.ttl = ttl;
            return this;
        }

        /**
         * Specifies the number of data bytes to be sent.
         * The default is 56, which translates into 64 ICMP data bytes when combined with the 8 bytes of ICMP header data.
         */
        public Builder packetSize(int packetSize) {
            if (packetSize < 1) throw new IllegalArgumentException("packetSize < 1");
            this.packetSize = packetSize;
            return this;
        }

        /**
         * Specify a timeout, in seconds, before ping exits regardless of how many packets have been sent or received.
         * In this case ping does not stop after count packet are sent,
         * it waits either for deadline expire or until count probes are answered or for some error notification from network.
         */
        public Builder deadline(int deadline) {
            if (deadline < 1) throw new IllegalArgumentException("deadline < 1");
            this.deadline = deadline;
            return this;
        }

        /**
         * Time to wait for a response, in seconds.
         * The option affects only timeout in absense of any responses, otherwise ping waits for two RTTs.
         */
        public Builder timeout(int timeout) {
            if (timeout < 1) throw new IllegalArgumentException("timeout < 1");
            this.timeout = timeout;
            return this;
        }

        public Request build() {
            if (destination == null) throw new IllegalStateException("ip == null");
            return new Request(this);
        }
    }
}
