package com.jamierf.evohome;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.jamierf.evohome.model.State;
import com.jamierf.evohome.model.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskRunner {
    private static final int TASK_STATUS_CHECK_DELAY_SECONDS = 2;
    private static final int TASK_STATUS_CHECK_TIMEOUT_SECONDS = 60;

    private final ListeningExecutorService executor;
    private final EvohomeClient client;

    public TaskRunner(final ExecutorService executor, final EvohomeClient client) {
        this.executor = MoreExecutors.listeningDecorator(executor);
        this.client = client;
    }

    public ListenableFuture<State> toFuture(final Task task) {
        return executor.submit(() -> await(task, TASK_STATUS_CHECK_DELAY_SECONDS, TASK_STATUS_CHECK_TIMEOUT_SECONDS));
    }

    private State await(final Task task, final int delay, final int timeout) throws InterruptedException {
        final long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeout);

        State state = null;
        while (System.currentTimeMillis() < endTime) {
            state = client.getTaskStatus(task).getState();
            if (state.isComplete()) {
                break;
            }

            TimeUnit.SECONDS.sleep(delay);
        }

        return state;
    }
}
