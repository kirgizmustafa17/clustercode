package clustercode.scheduling;

import clustercode.impl.util.InvalidConfigurationException;
import clustercode.main.config.Configuration;
import clustercode.scheduling.constraint.ConstraintFactory;
import clustercode.scheduling.constraint.Constraints;
import clustercode.scheduling.matcher.CompanionProfileMatcher;
import clustercode.scheduling.matcher.DefaultProfileMatcher;
import clustercode.scheduling.matcher.DirectoryStructureMatcher;
import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SchedulingVerticle extends AbstractVerticle {

    private SchedulingMessageHandler messageHandler;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        this.checkInterval(config().getInteger(Configuration.scan_interval_minutes.key()));
        var scanConfig = new ProfileScanConfig(config());

        try {

            var constraints = Stream
                .of(config().getString(
                    Configuration.constraint_active.key())
                    .trim()
                    .split(" "))
                .map(String::trim)
                .map(Constraints::valueOf)
                .map(c -> new ConstraintFactory(config()).createFromEnum(c))
                .collect(Collectors.toSet());
            var selectionService = new SelectionServiceImpl(constraints);

            var profileScanService = new ProfileScanServiceImpl(Arrays.asList(
                new CompanionProfileMatcher(scanConfig, new ProfileParserImpl(scanConfig)),
                new DirectoryStructureMatcher(scanConfig, new ProfileParserImpl(scanConfig)),
                new DefaultProfileMatcher(scanConfig, new ProfileParserImpl(scanConfig))
            ));
            var scanService = new MediaScanServiceImpl(new MediaScanConfig(config()), FileScannerImpl::new);

            this.messageHandler = new SchedulingMessageHandler(vertx, scanService, selectionService, profileScanService);

        } catch (IllegalArgumentException ex) {
            MDC.put("error", ex.getMessage());
            log.error("Could not construct the set of enabled constraints. Please check if {} is configured correctly.",
                Configuration.constraint_active.key());
            startFuture.fail(ex);
            MDC.remove("error");
            return;
        }

        log.debug("Verticle started.");
        startFuture.complete();

        Thread.sleep(2000);
        vertx.eventBus().sender(SchedulingMessageHandler.MEDIA_SCAN_START).send(true);
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        messageHandler.cleanup();
        stopFuture.complete();
    }

    private void checkInterval(long scanInterval) {
        if (scanInterval < 1) {
            throw new InvalidConfigurationException("The scan interval must be >= 1.");
        }
    }
}

