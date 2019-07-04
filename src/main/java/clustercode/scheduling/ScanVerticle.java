package clustercode.scheduling;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.impl.util.InvalidConfigurationException;
import clustercode.main.config.Configuration;
import clustercode.scheduling.matcher.CompanionProfileMatcher;
import clustercode.scheduling.matcher.DefaultProfileMatcher;
import clustercode.scheduling.matcher.DirectoryStructureMatcher;
import clustercode.scheduling.messages.MediaScannedMessage;
import clustercode.scheduling.messages.MediaSelectedMessage;
import clustercode.scheduling.messages.ProfileSelectedMessage;
import clustercode.scheduling.constraint.ConstraintFactory;
import clustercode.scheduling.constraint.Constraints;
import io.reactivex.Maybe;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageConsumer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ScanVerticle extends AbstractVerticle {

    public static String MEDIA_SCAN_START = "clustercode.scan.Start";
    public static String MEDIA_SCAN_COMPLETE = "clustercode.scan.MediaScanned";
    public static String MEDIA_SELECT_COMPLETE = "clustercode.scan.MediaSelected";
    public static String PROFILE_SELECT_COMPLETE = "clustercode.scan.ProfileSelected";

    private EventBus eb;
    private MediaScanService scanService;
    private SelectionService selectionService;
    private ProfileScanService profileScanService;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        this.checkInterval(config().getInteger(Configuration.scan_interval_minutes.key()));
        var scanConfig = new ProfileScanConfig(config());

        this.profileScanService = new ProfileScanServiceImpl(Arrays.asList(
            new CompanionProfileMatcher(scanConfig, new ProfileParserImpl()),
            new DirectoryStructureMatcher(scanConfig, new ProfileParserImpl()),
            new DefaultProfileMatcher(scanConfig, new ProfileParserImpl())
        ));
        this.scanService = new MediaScanServiceImpl(new MediaScanConfig(config()), FileScannerImpl::new);

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
            this.selectionService = new SelectionServiceImpl(constraints);
        } catch (IllegalArgumentException ex) {
            MDC.put("error", ex.getMessage());
            log.error("Could not construct the set of enabled constraints. Please check if {} is configured correctly.",
                Configuration.constraint_active.key());
            startFuture.fail(ex);
            MDC.remove("error");
            return;
        }

        this.eb = vertx.eventBus();

        eb.consumer(MEDIA_SCAN_START, m -> {
            onMediaScanRequest();
            m.reply(true);
        });

        MessageConsumer<JsonObject> mediaSelectConsumer = eb.consumer(MEDIA_SCAN_COMPLETE);
        mediaSelectConsumer.handler(this::onMediaScanned);

        MessageConsumer<JsonObject> mediaSelectCompleteConsumer = eb.consumer(MEDIA_SELECT_COMPLETE);
        mediaSelectCompleteConsumer.handler(this::onMediaSelected);

        MessageConsumer<JsonObject> profileSelectCompleteConsumer = eb.consumer(PROFILE_SELECT_COMPLETE);
        profileSelectCompleteConsumer.handler(this::onProfileSelected);

        log.info("Scanner started.");
        startFuture.complete();

        eb.sender(MEDIA_SCAN_START).send(true);
    }

    void onMediaScanRequest() {

        Maybe<List<Media>> rx = vertx.rxExecuteBlocking(h -> h.complete(scanService.retrieveFilesAsList()));
        rx
            .doFinally(() -> MDC.remove("count"))
            .map(l -> MediaScannedMessage
                .builder()
                .mediaList(l)
                .build())
            .subscribe(m -> {
                MDC.put("count", String.valueOf(m.getMediaList().size()));
                log.info("Media files scanned.");
                eb
                    .sender(MEDIA_SCAN_COMPLETE)
                    .send(m.toJson());
            });
    }

    void onMediaScanned(Message<JsonObject> payload) {
        var msg = new MediaScannedMessage(payload.body());
        if (msg.listHasEntries()) {
            vertx
                .rxExecuteBlocking(h -> {
                    selectionService.selectMedia(msg.getMediaList())
                        .ifPresentOrElse(
                            h::complete,
                            () -> h.fail("Did not found a media item that satisfies all constraints.")
                        );
                })
                .cast(Media.class)
                .doFinally(() -> {
                    MDC.remove("media");
                })
                .subscribe(
                    m -> {
                        MDC.put("media", m.toString());
                        log.info("Media file selected.");
                        eb
                            .sender(MEDIA_SELECT_COMPLETE)
                            .send(MediaSelectedMessage
                                .builder()
                                .media(m)
                                .build()
                                .toJson());
                    },
                    ex -> {
                        log.info(ex.getMessage());
                        scheduleNextRun();
                    });
        } else {
            log.debug("No media items found.");
            scheduleNextRun();
        }
    }

    private void onMediaSelected(Message<JsonObject> payload) {
        var msg = new MediaSelectedMessage(payload.body());
        if (msg.isSelected()) {
            vertx
                .rxExecuteBlocking(h ->
                    profileScanService
                        .selectProfile(msg.getMedia())
                        .ifPresentOrElse(
                            h::complete,
                            () -> h.fail("Could not find a suitable profile.")
                        ))
                .cast(Profile.class)
                .doFinally(() -> {
                    MDC.remove("profile");
                    MDC.remove("media");
                })
                .subscribe(
                    p -> {
                        MDC.put("profile", p.getLocation().toString());
                        MDC.put("media", msg.getMedia().toString());
                        log.info("Profile selected.");
                        eb
                            .sender(PROFILE_SELECT_COMPLETE)
                            .send(ProfileSelectedMessage
                                .builder()
                                .media(msg.getMedia())
                                .profile(p)
                                .build()
                                .toJson());
                    },
                    ex -> {
                        log.info(ex.getMessage());
                        scheduleNextRun();
                    });
        }
    }

    private void onProfileSelected(Message<JsonObject> payload) {
        var msg = new ProfileSelectedMessage(payload.body());
        if (msg.isSelected()) {
            log.info("would transcode");
        }
    }


    void scheduleNextRun() {
        var millis = TimeUnit.MINUTES.toMillis(config().getInteger(Configuration.scan_interval_minutes.key()));
        MDC.put("minutes", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(millis)));
        log.info("Scheduling next run.");
        vertx.setTimer(millis, this::onTimeOut);
        MDC.remove("minutes");
    }

    private void onTimeOut(Long timerId) {
        eb
            .sender(MEDIA_SCAN_START)
            .send(true);
    }

    private void checkInterval(long scanInterval) {
        if (scanInterval < 1) {
            throw new InvalidConfigurationException("The scan interval must be >= 1.");
        }
    }
}

