package io.fabianterhorst.arp;

import okio.ByteString;

/**
 * Created by fabianterhorst on 19.06.17.
 */

public class ArpRow {

    ByteString ipAddress;

    ByteString hwType;

    ByteString flags;

    ByteString hwAddress;

    ByteString mask;

    ByteString device;

    public ByteString ipAddress() {
        return ipAddress;
    }

    public ByteString hwType() {
        return hwType;
    }

    public ByteString flags() {
        return flags;
    }

    public ByteString hwAddress() {
        return hwAddress;
    }

    public ByteString mask() {
        return mask;
    }

    public ByteString device() {
        return device;
    }
}
