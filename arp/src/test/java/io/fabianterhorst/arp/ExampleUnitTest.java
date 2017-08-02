package io.fabianterhorst.arp;

import org.junit.Test;

import okio.Buffer;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        ArpReader reader = new ArpReader();

        Buffer buffer = new Buffer();
        buffer.writeUtf8("TITLE    TITLE2    TITLE3    TITLE4\n");
        buffer.writeUtf8("192.168.178.1    1    2    3  4   5\n");
        buffer.writeUtf8("192.168.178.2    6    7    8   9   10\n");
        reader.read(buffer);
        for (ArpRow arpRow : reader.getArpRows()) {
            System.out.println("row " + arpRow.ipAddress + " " + arpRow.hwType
                    + " " + arpRow.flags + " " + arpRow.hwAddress
                    + " " + arpRow.mask + " " + arpRow.device);
        }
    }
}
