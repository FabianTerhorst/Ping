package io.fabianterhorst.ping;

import java.io.IOException;

/**
 * Created by fabianterhorst on 17.06.17.
 */

public class Ping {

    private Dispatcher dispatcher;

    public Ping() {
        this.dispatcher = new Dispatcher();
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public Call newCall(Request request) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder()
                .command(request.commands())
                .redirectErrorStream(true);
        return new RealCall(this, request, processBuilder);
    }
}
