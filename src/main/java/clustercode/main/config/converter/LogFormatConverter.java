package clustercode.main.config.converter;

import io.vertx.core.cli.converters.Converter;

public class LogFormatConverter implements Converter<LogFormat> {
    @Override
    public LogFormat fromString(String s) {
        return LogFormat.valueOf(s);
    }
}
