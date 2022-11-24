package io.wispforest.gadget.decompile.handle;

import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GadgetFernflowerLogger extends IFernflowerLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("gadget/Quiltflower");

    @Override
    public void writeMessage(String message, Severity severity) {
        if (severity == Severity.TRACE) {
            LOGGER.trace(message);
        } else if (severity == Severity.INFO) {
            LOGGER.info(message);
        } else if (severity == Severity.WARN) {
            LOGGER.warn(message);
        } else if (severity == Severity.ERROR) {
            LOGGER.error(message);
        }
    }

    @Override
    public void writeMessage(String message, Severity severity, Throwable t) {
        if (severity == Severity.TRACE) {
            LOGGER.trace(message, t);
        } else if (severity == Severity.INFO) {
            LOGGER.info(message, t);
        } else if (severity == Severity.WARN) {
            LOGGER.warn(message, t);
        } else if (severity == Severity.ERROR) {
            LOGGER.error(message, t);
        }
    }
}
