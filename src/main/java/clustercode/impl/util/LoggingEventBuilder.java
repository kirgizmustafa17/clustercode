package clustercode.impl.util;

import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Optional;

/**
 * A SLF4J-2.0-like interface for fluent building log with key-value support, but using MDC instead of message prefix.
 * Used until Log4j2 fully supports SLF4J-2.0 interface.
 */
public class LoggingEventBuilder {

    private final Logger logger;
    private final HashMap<String, String> keys;
    private Level level;

    private LoggingEventBuilder(Logger logger) {
        this.keys = new HashMap<>();
        this.logger = logger;
    }

    public static LoggingEventBuilder createFrom(Logger logger) {
        return new LoggingEventBuilder(logger);
    }

    public LoggingEventBuilder atInfo() {
        this.level = Level.INFO;
        return this;
    }

    public LoggingEventBuilder atDebug() {
        this.level = Level.DEBUG;
        return this;
    }

    public LoggingEventBuilder atWarn() {
        this.level = Level.WARN;
        return this;
    }

    public LoggingEventBuilder atError() {
        this.level = Level.ERROR;
        return this;
    }

    public LoggingEventBuilder addKeyValue(String key, Object value) {
        this.keys.put(key, Optional
            .ofNullable(value)
            .map(Object::toString)
            .orElse("null"));
        return this;
    }

    public LoggingEventBuilder removeKey(String key) {
        this.keys.remove(key);
        return this;
    }

    /**
     * Sets the cause, which will be toString()ed and set in the "error" key in MDC. Does not produce a stacktrace!
     *
     * @param cause
     * @return this
     */
    public LoggingEventBuilder setCause(Throwable cause) {
        if (cause != null) this.keys.put("error", cause.toString());
        return this;
    }

    public void log(String message) {
        this.keys.forEach(MDC::put);
        switch (this.level) {
            case ERROR:
                logger.error(message);
                break;
            case DEBUG:
                logger.debug(message);
                break;
            case INFO:
                logger.info(message);
                break;
            case WARN:
                logger.warn(message);
                break;
        }
        this.keys.keySet().forEach(MDC::remove);
    }
}
