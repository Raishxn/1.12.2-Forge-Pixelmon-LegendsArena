package com.envyful.api.concurrency;

import java.util.concurrent.TimeUnit;

/**
 *
 * Builder class for repeating tasks on Forge
 *
 */
public class AsyncTaskBuilder {

    private long delayMillis = 0;
    private long intervalMillis = 10L;

    private Runnable task;

    public AsyncTaskBuilder() {}

    /**
     *
     * The delay in millis before the task should begin
     *
     * @param delayMillis time in millis before the task starts
     * @return The builder
     */
    public AsyncTaskBuilder delay(long delayMillis) {
        this.delayMillis = delayMillis;
        return this;
    }

    /**
     *
     * The interval between each execution
     *
     * @param intervalMillis The milliseconds between each execution
     * @return The builder
     */
    public AsyncTaskBuilder interval(long intervalMillis) {
        this.intervalMillis = intervalMillis;
        return this;
    }


    /**
     *
     * Sets the task to be executed
     *
     * @param task The task
     * @return The builder
     */
    public AsyncTaskBuilder task(Runnable task) {
        this.task = task;
        return this;
    }

    /**
     *
     * Runs the task
     *
     */
    public void start() {
        if (this.task == null) {
            throw new RuntimeException("Task cannot be null");
        }

        UtilConcurrency.SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate
                (this.task, this.delayMillis, this.intervalMillis, TimeUnit.MILLISECONDS);
    }
}
