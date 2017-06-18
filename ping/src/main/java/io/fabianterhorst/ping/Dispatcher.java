/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabianterhorst.ping;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by fabianterhorst on 18.06.17.
 */

public class Dispatcher {

    private ExecutorService executorService;

    /**
     * Running asynchronous calls. Includes canceled calls that haven't finished yet.
     */
    private final Deque<RealCall.AsyncCall> runningAsyncCalls = new ArrayDeque<>();

    public synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), threadFactory("Ping Dispatcher", false));
        }
        return executorService;
    }

    static ThreadFactory threadFactory(final String name, final boolean daemon) {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread result = new Thread(runnable, name);
                result.setDaemon(daemon);
                return result;
            }
        };
    }

    synchronized void enqueue(RealCall.AsyncCall call) {
        runningAsyncCalls.add(call);
        executorService().execute(call);
    }

    synchronized void finished(RealCall.AsyncCall call) {
        synchronized (this) {
            runningAsyncCalls.remove(call);
        }
    }

    synchronized void cancelAll() {
        for (RealCall.AsyncCall call : runningAsyncCalls) {
            call.get().cancel();
        }
    }
}
