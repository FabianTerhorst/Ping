package okio;

import java.io.IOException;

/**
 * Created by fabianterhorst on 18.06.17.
 */

public class SourceDoubleUtils {

    public static boolean isNextNumber(BufferedSource source) throws IOException {
        source.require(1);
        Buffer buffer = source.buffer();
        for (int pos = 0; source.request(pos + 1); pos++) {
            byte b = buffer.getByte(pos);
            if ((b < '0' || b > '9') && (pos != 0 || b != '-')) {
                // Non-digit, or non-leading negative sign.
                if (pos == 0) {
                    return false;
                }
                break;
            }
        }
        return true;
    }

    public static double readDecimalDouble(BufferedSource source) throws IOException {

        Buffer sourceBuffer = source.buffer();

        source.require(1);

        boolean floating = false;
        for (int pos = 0; source.request(pos + 1); pos++) {
            byte b = sourceBuffer.getByte(pos);
            if ((b < '0' || b > '9') && (pos != 0 || b != '-') && b != '.') {
                // Non-digit, or non-leading negative sign.
                if (pos == 0) {
                    throw new NumberFormatException(String.format(
                            "Expected leading [0-9] or '-' character but was %#x", b));
                }
                break;
            }
            if (b == '.') {
                if (floating) {
                    break;
                }
                floating = true;
            }
        }
        if (sourceBuffer.size == 0) throw new IllegalStateException("size == 0");

        // This value is always built negatively in order to accommodate Long.MIN_VALUE.
        double value = 0;
        int seen = 0;
        boolean negative = false;
        floating = false;
        boolean done = false;

        long floatingDigit = 1;

        do {
            Segment segment = sourceBuffer.head;

            byte[] data = segment.data;
            int pos = segment.pos;
            int limit = segment.limit;

            for (; pos < limit; pos++, seen++) {
                byte b = data[pos];
                if (b >= '0' && b <= '9') {
                    double digit = '0' - b;
                    if (floating) {
                        digit /= floatingDigit *= 10;
                    } else {
                        value *= 10;
                    }
                    value += digit;
                } else if (b == '-' && seen == 0) {
                    negative = true;
                } else if (b == '.' && !floating) {
                    floating = true;
                } else {
                    if (seen == 0) {
                        throw new NumberFormatException(
                                "Expected leading [0-9] or '-' character but was 0x" + Integer.toHexString(b));
                    }
                    // Set a flag to stop iteration. We still need to run through segment updating below.
                    done = true;
                    break;
                }
            }

            if (pos == limit) {
                sourceBuffer.head = segment.pop();
                SegmentPool.recycle(segment);
            } else {
                segment.pos = pos;
            }
        } while (!done && sourceBuffer.head != null);

        sourceBuffer.size -= seen;
        value = (double)Math.round(value * floatingDigit) / floatingDigit;
        return negative ? value : -value;
    }
}
