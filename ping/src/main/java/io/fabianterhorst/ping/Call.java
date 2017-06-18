package io.fabianterhorst.ping;

import java.io.IOException;

/**
 * Created by fabianterhorst on 18.06.17.
 */

public interface Call {

    void execute(Callback callback) throws IOException;

    void enqueue(Callback callback) throws IOException;

    void cancel();

    boolean isCanceled();
}
