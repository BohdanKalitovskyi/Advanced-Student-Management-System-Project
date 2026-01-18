package core;

import java.lang.Thread.UncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalExceptionHandler implements UncaughtExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error("Uncaught exception in thread {}", t.getName(), e);
    }
}
