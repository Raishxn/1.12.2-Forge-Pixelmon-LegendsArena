package com.envyful.api.concurrency;

import org.apache.logging.log4j.Logger;

public class UtilLogger {

    private static Logger logger;

    public static void setLogger(Logger logger) {
        UtilLogger.logger = logger;
    }

    public static Logger getLogger() {
        return logger;
    }
}
