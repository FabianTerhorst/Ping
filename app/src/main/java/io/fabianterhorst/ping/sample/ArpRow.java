package io.fabianterhorst.ping.sample;

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


}
