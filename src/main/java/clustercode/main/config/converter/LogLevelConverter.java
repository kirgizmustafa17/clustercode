package clustercode.main.config.converter;

import io.vertx.core.cli.converters.Converter;

public class LogLevelConverter implements Converter<LogLevel> {

    @Override
    public LogLevel fromString(String s) {
        return LogLevel.valueOf(s);
    }
}
