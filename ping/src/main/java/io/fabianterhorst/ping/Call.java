package io.fabianterhorst.ping;

/**
 * Created by fabianterhorst on 18.06.17.
 */

public interface Call {

    void execute(Callback callback);

    void enqueue(Callback callback);

    void cancel();

    boolean isCanceled();
}
