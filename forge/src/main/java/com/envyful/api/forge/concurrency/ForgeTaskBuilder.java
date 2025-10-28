package com.envyful.api.forge.concurrency;

import com.envyful.api.concurrency.UtilConcurrency;

/**
 *
 * Builder class for repeating tasks on Forge
 *
 */
public class ForgeTaskBuilder {

    private boolean async = true;
    private long delayTicks = 0;
    private long intervalTicks = 10L;

    private Runnable task;
    private boolean started = false;

    public ForgeTaskBuilder() {}

    public static ForgeTaskBuilder builder() {
        return new ForgeTaskBuilder();
    }

    /**
     *
     * Sets if the task should be run async
     *
     * @param async if the task should run on the server thread or not
     * @return The builder
     */
    public ForgeTaskBuilder async(boolean async) {
        this.async = async;
        return this;
    }

    /**
     *
     * The delay ticks before the task should begin
     *
     * @param delayTicks number of ticks before the task starts
     * @return The builder
     */
    public ForgeTaskBuilder delay(long delayTicks) {
        this.delayTicks = delayTicks;
        return this;
    }

    /**
     *
     * The interval between each execution
     *
     * @param intervalTicks The ticks between each execution
     * @return The builder
     */
    public ForgeTaskBuilder interval(long intervalTicks) {
        this.intervalTicks = intervalTicks;
        return this;
    }


    /**
     *
     * Sets the task to be executed
     *
     * @param task The task
     * @return The builder
     */
    public ForgeTaskBuilder task(Runnable task) {
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

        if (this.started) {
            return;
        }

        this.started = true;

        RepeatedRunnable runnable = new RepeatedRunnable(this);
        UtilConcurrency.runRepeatingTask(runnable, 50L, 50L);
    }

    private final class RepeatedRunnable implements Runnable {

        private final ForgeTaskBuilder taskBuilder;

        private int delayTicks = 0;
        private int ticks = 0;
        private int lastRunTicks = 0;

        private RepeatedRunnable(ForgeTaskBuilder taskBuilder) {
            this.taskBuilder = taskBuilder;
        }

        @Override
        public void run() {
            ++this.delayTicks;

            if (this.delayTicks <= this.taskBuilder.delayTicks) {
                return;
            }

            ++this.ticks;

            if (this.lastRunTicks == 0 || (this.ticks - this.lastRunTicks) == this.taskBuilder.intervalTicks) {
                this.lastRunTicks = this.ticks;

                if (this.taskBuilder.async) {
                    UtilConcurrency.runAsync(() -> this.taskBuilder.task.run());
                } else {
                    UtilForgeConcurrency.runSync(this.taskBuilder.task);
                }
            }
        }
    }
}
