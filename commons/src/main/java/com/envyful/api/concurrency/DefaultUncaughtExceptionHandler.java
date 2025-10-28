package com.envyful.api.concurrency;

import org.apache.logging.log4j.Logger;

public class DefaultUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Logger logger;

    public DefaultUncaughtExceptionHandler(Logger p_i48772_1_) {
        this.logger = p_i48772_1_;
    }

    public void uncaughtException(Thread p_uncaughtException_1_, Throwable p_uncaughtException_2_) {
        if (this.logger == null) {
            return;
        }

        this.logger.error("Caught previously unhandled exception :", p_uncaughtException_2_);
    }
}
