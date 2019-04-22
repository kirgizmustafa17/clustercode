package clustercode.main.config;

import clustercode.main.config.converter.LogFormat;
import clustercode.main.config.converter.LogLevel;
import clustercode.main.config.converter.PathConverter;
import io.vertx.core.cli.annotations.*;
import lombok.Getter;

import java.net.URL;
import java.nio.file.Path;

@Name("master")
@Summary("Backend API and scheduler for clustercode.")
@Getter
public class AnnotatedCli {

    private Path configFile;
    private boolean help;
    private LogLevel logLevel;
    private LogFormat logFormat;
    private URL rabbitMqUrl;
    private Integer httpPort;

    @Option(shortName = "c",
            longName = "config")
    @Description("Config file location in .properties format.")
    @ConvertedBy(PathConverter.class)
    public void setConfigFile(Path configFile) {
        this.configFile = configFile;
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
            choices = {"debug", "info", "warn", "error", "fatal"})
    @Description("Log level")
    //@ConvertedBy(LogLevelConverter.class)
    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    @Option(longName = "log.format",
            choices = {"text", "json"})
    @Description("Log format")
    //@ConvertedBy(LogFormatConverter.class)
    public void setLogFormat(LogFormat logLevel) {
        this.logFormat = logLevel;
    }


    @Option(longName = "rabbitmq.url")
    @Description("Expects a full amqp URL in the format 'amqp://host:port/'")
    public void setRabbitMqUrl(URL rabbitMqUrl) {
        this.rabbitMqUrl = rabbitMqUrl;
    }

    @Option(longName = "api.http.port")
    @Description("HTTP API server port to listen to.")
    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }
}
