package clustercode.main;

import clustercode.database.CouchDbVerticle;
import clustercode.main.config.AnnotatedCli;
import clustercode.main.config.Configuration;
import clustercode.main.config.converter.LogFormat;
import clustercode.main.config.converter.LogLevel;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CLIException;
import io.vertx.core.cli.annotations.CLIConfigurator;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.Vertx;
import org.slf4j.MDC;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.function.Function;

public class Startup {

    private static XLogger log;

    public static void main(String[] args) throws Exception {
        var cli = CLI.create(AnnotatedCli.class);
        var flags = new AnnotatedCli();
        try {
            var parsed = cli.parse(Arrays.asList(args), true);
            CLIConfigurator.inject(parsed, flags);
            if (!parsed.isValid() || flags.isHelp()) {
                printUsageAndExit(cli);
            }

            System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");

            // override default log properties from ENV if given.
            trySetPropertyFromEnv("CC_LOG_LEVEL", "log.level", cli, LogLevel::valueOf);
            trySetPropertyFromEnv("CC_LOG_FORMAT", "log.format", cli, LogFormat::valueOf);

            // override Log ENVs from CLI if given.
            if (flags.getLogLevel() != null) System.setProperty("log.level", flags.getLogLevel().name());
            if (flags.getLogFormat() != null) System.setProperty("log.format", flags.getLogFormat().name());

        } catch (CLIException ex) {
            System.err.println(ex.getMessage());
            printUsageAndExit(cli);
        }

        log = XLoggerFactory.getXLogger(Startup.class);

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            // Normally the expectable exceptions should be caught, but to debug any unexpected ones we log them.
            log.error("Application-wide uncaught exception:", throwable);
            System.exit(2);
        });

        MDC.put("dir", new File("").getAbsolutePath());
        log.debug("Using working dir.");
        MDC.clear();

        ConfigRetriever retriever = ConfigRetriever.create(Vertx.vertx(),
                new ConfigRetrieverOptions()
                        .addStore(new ConfigStoreOptions()
                                .setType("json")
                                .setConfig(Configuration.createFromDefault())
                        )
                        .addStore(new ConfigStoreOptions()
                                .setType("json")
                                .setConfig(Configuration.createFromEnvMap(System.getenv())))
                        .addStore(new ConfigStoreOptions()
                                .setType("json")
                                .setConfig(Configuration.createFromFlags(flags)))
        );

        retriever.getConfig(json -> {
            var config = json.result();
            //  configV.close();
            var v = Vertx.vertx(new VertxOptions().setMetricsOptions(
                    new MicrometerMetricsOptions()
                            .setPrometheusOptions(new VertxPrometheusOptions()
                                    .setEnabled(config.getBoolean(Configuration.prometheus_enabled.key()))
                                    .setPublishQuantiles(config.getBoolean(Configuration.prometheus_publishQuantiles.key()))
                            )
                            .setEnabled(true)
                    )
            );
            v.exceptionHandler(ex -> log.error("Unhandled Vertx exception:", ex));
            v.deployVerticle(new HttpVerticle(), new DeploymentOptions().setConfig(config));
            v.deployVerticle(new CouchDbVerticle(), new DeploymentOptions().setConfig(config));
        });

    }

    private static <T extends Enum> void trySetPropertyFromEnv(
            String key,
            String prop,
            CLI cli,
            Function<String, T> enumSupplier) {
        var value = System.getenv(key);
        try {
            if (value != null) System.setProperty(prop, enumSupplier.apply(value).name());
        } catch (IllegalArgumentException ex) {
            System.err.println(String.format("The value '%1$s' is not accepted by '%2$s'", value, prop));
            printUsageAndExit(cli);
        }
    }

    private static void printUsageAndExit(CLI cli) {
        StringBuilder builder = new StringBuilder();
        cli.usage(builder);
        System.out.print(builder.toString());
        System.exit(1);
    }

}
