package io.fabianterhorst.ping;

import java.io.IOException;

/**
 * Created by fabianterhorst on 18.06.17.
 */

public interface Callback {

    void onStart(Call call, String domain, String ip, long packageSize, long realPackageSize);

    void onFinish(Call call, int status /*TODO: add summary*/);

    void onFailure(Call call, IOException e);

    void onResponse(Call call, Response response) throws IOException;
}
