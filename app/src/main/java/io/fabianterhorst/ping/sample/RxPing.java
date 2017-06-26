package io.fabianterhorst.ping.sample;

import java.io.IOException;

import io.fabianterhorst.ping.Call;
import io.fabianterhorst.ping.Callback;
import io.fabianterhorst.ping.Ping;
import io.fabianterhorst.ping.Request;
import io.fabianterhorst.ping.Response;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Cancellable;

/**
 * Created by fabianterhorst on 23.06.17.
 */

public class RxPing {

    private Ping ping;

    public RxPing() {
        this(new Ping());
    }

    public RxPing(Ping ping) {
        this.ping = ping;
    }

    public Single<Response> pingOnce(final String url) {
        return Single.create(new SingleOnSubscribe<Response>() {
            @Override
            public void subscribe(final SingleEmitter<Response> e) throws Exception {
                final Call call = ping.newCall(new Request.Builder()
                        .destination(url)
                        .count(1)
                        .build());
                call.execute(new Callback() {
                    @Override
                    public void onStart(Call call, String domain, String ip, long packageSize, long realPackageSize) {

                    }

                    @Override
                    public void onFinish(Call call, int status, long packagesTransmitted, long packagesReceived,
                                         double packagesLostPercent, double min, double avg, double max, double stdDev) {

                    }

                    @Override
                    public void onFailure(Call call, IOException io) {
                        if (!e.isDisposed()) {
                            e.onError(io);
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!e.isDisposed()) {
                            e.onSuccess(response);
                            call.cancel();
                        }
                    }
                });
                e.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        call.cancel();
                    }
                });
            }
        });
    }
}
