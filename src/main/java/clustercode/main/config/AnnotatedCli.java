package clustercode.main.config;

import clustercode.main.config.converter.LogFormat;
import clustercode.main.config.converter.LogLevel;
import clustercode.main.config.converter.PathConverter;
import io.vertx.core.cli.annotations.*;
import lombok.Getter;

import java.net.URI;
import java.nio.file.Path;

@Name("master")
@Summary("Backend API and scheduler for clustercode.")
@Getter
public class AnnotatedCli {

    private boolean help;
    private Path configFile;

    private Path logConfig;
    private LogLevel logLevel;
    private LogFormat logFormat;

    private URI rabbitMqUri;
    private Integer httpPort;


    @Option(shortName = "c",
        longName = "config")
    @Description("Config file path in .properties format.")
    @ConvertedBy(PathConverter.class)
    public void setConfigFile(Path configFile) {
        this.configFile = configFile;
    }

    @Option(longName = "log.config")
    @Description("Log4j2 configuration path. May invalidate other log options.")
    @ConvertedBy(PathConverter.class)
    public void setLogConfigfile(Path configFile) {
        this.logConfig = configFile;
    }

    @Option(shortName = "h",
        longName = "help",
        help = true,
        flag = true)
    @Description("Displays this help and exits.")
    public void setHelp(boolean help) {
        this.help = help;
    }

    @Option(shortName = "l",
        longName = "log.level",
        choices = {"debug", "info", "warn", "error"})
    @Description("Log level")
    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    @Option(longName = "log.format",
        choices = {"text", "json"})
    @Description("Log format")
    public void setLogFormat(LogFormat logLevel) {
        this.logFormat = logLevel;
    }


    @Option(longName = "rabbitmq.uri")
    @Description("Expects a full amqp URL in the format 'amqp://host:port/'")
    public void setRabbitMqUri(URI rabbitMqUri) {
        this.rabbitMqUri = rabbitMqUri;
    }

    @Option(longName = "api.http.port")
    @Description("HTTP API server port to listen to.")
    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }
}
