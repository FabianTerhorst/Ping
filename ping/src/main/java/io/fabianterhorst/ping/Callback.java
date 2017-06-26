package io.fabianterhorst.ping;

import java.io.IOException;

/**
 * Created by fabianterhorst on 18.06.17.
 */

public interface Callback {

    void onStart(Call call, String domain, String ip, long packageSize, long realPackageSize);

    void onFinish(Call call, int status, long packagesTransmitted, long packagesReceived,
                  double packagesLostPercent, double min, double avg, double max, double stdDev);

    void onFailure(Call call, IOException e);

    void onResponse(Call call, Response response) throws IOException;
}
