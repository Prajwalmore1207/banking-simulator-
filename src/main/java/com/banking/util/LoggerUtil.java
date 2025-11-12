package com.banking.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerUtil {
    private static final Logger logger = LogManager.getLogger(LoggerUtil.class);

    public static Logger getLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }

    public static void logInfo(Class<?> clazz, String message) {
        getLogger(clazz).info(message);
    }

    public static void logError(Class<?> clazz, String message, Throwable throwable) {
        getLogger(clazz).error(message, throwable);
    }

    public static void logWarn(Class<?> clazz, String message) {
        getLogger(clazz).warn(message);
    }

    public static void logDebug(Class<?> clazz, String message) {
        getLogger(clazz).debug(message);
    }
}