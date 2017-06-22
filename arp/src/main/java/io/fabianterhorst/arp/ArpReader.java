package io.fabianterhorst.arp;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;

/**
 * Created by fabianterhorst on 22.06.17.
 */

public class ArpReader {

    private final ByteString END = ByteString.encodeUtf8(" \n");

    private long lastModified = -1;

    private List<ArpRow> arpRows = new LinkedList<>();

    public List<ArpRow> read() throws IOException {
        File file = new File("/proc/net/arp");
        long lastModified = file.lastModified();
        if (this.lastModified < lastModified) {
            BufferedSource source = Okio.buffer(Okio.source(file));
            read(source);
            this.lastModified = lastModified;
        }
        return arpRows;
    }

    public void read(BufferedSource source) throws IOException {
        Buffer buffer = source.buffer();
        long p = 0;
        long index;
        long column = 0;
        // Skip first row
        source.skip(source.indexOf((byte) '\n') + 1);
        arpRows.clear();
        ArpRow row = null;
        while (source.request(p + 1)) {
            int c = buffer.getByte(p++);
            if (c == ' ') continue;
            if (c == '\n') {
                if (row != null)
                    arpRows.add(row);
                column = 0;
                continue;
            }
            buffer.skip(p - 1);
            index = source.indexOfElement(END);
            if (source.request(index + 2)
                    && buffer.getByte(index) != '\n'
                    && buffer.getByte(index + 1) != ' ') {
                index = source.indexOf((byte) ' ', index + 2);//Was index +1 before maybe just indexOfElement as well?
            }
            p = 0;
            if (column == 0) {
                row = new ArpRow();
                row.ipAddress = source.readByteString(index);
            } else if (column == 1) {
                row.hwType = source.readByteString(index);
            } else if (column == 2) {
                row.flags = source.readByteString(index);
            } else if (column == 3) {
                row.hwAddress = source.readByteString(index);
            } else if (column == 4) {
                row.mask = source.readByteString(index);
            } else if (column == 5) {
                row.device = source.readByteString(index);
            }
            column++;
        }
    }

    public List<ArpRow> getArpRows() {
        return arpRows;
    }
}
