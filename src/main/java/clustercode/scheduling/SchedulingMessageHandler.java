package clustercode.scheduling;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.main.config.Configuration;
import clustercode.scheduling.messages.MediaScannedMessage;
import clustercode.scheduling.messages.MediaSelectedMessage;
import clustercode.scheduling.messages.ProfileSelectedMessage;
import io.reactivex.Maybe;
import io.reactivex.disposables.Disposable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageConsumer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SchedulingMessageHandler {

    public static String MEDIA_SCAN_START = "clustercode.scan.Start";
    public static String MEDIA_SCAN_COMPLETE = "clustercode.scan.MediaScanned";
    public static String MEDIA_SELECT_COMPLETE = "clustercode.scan.MediaSelected";
    public static String PROFILE_SELECT_COMPLETE = "clustercode.scan.ProfileSelected";

    private final Vertx vertx;
    private final EventBus eb;

    private MediaScanService mediaScanService;
    private SelectionService selectionService;
    private ProfileScanService profileScanService;


    private List<Disposable> disposables = new ArrayList<>();
    private List<MessageConsumer<JsonObject>> messageConsumers = new ArrayList<>();

    public SchedulingMessageHandler(
        Vertx vertx,
        MediaScanService mediaScanService,
        SelectionService selectionService,
        ProfileScanService profileScanService
    ) {

        this.vertx = vertx;
        this.eb = vertx.eventBus();
        this.selectionService = selectionService;
        this.profileScanService = profileScanService;
        this.mediaScanService = mediaScanService;


        messageConsumers.add(eb.consumer(MEDIA_SCAN_START, m -> {
            onMediaScanRequest();
            m.reply(true);
        }));

        MessageConsumer<JsonObject> mediaSelectConsumer = eb.consumer(MEDIA_SCAN_COMPLETE);
        messageConsumers.add(mediaSelectConsumer.handler(this::onMediaScanned));

        MessageConsumer<JsonObject> mediaSelectCompleteConsumer = eb.consumer(MEDIA_SELECT_COMPLETE);
        messageConsumers.add(mediaSelectCompleteConsumer.handler(this::onMediaSelected));

        MessageConsumer<JsonObject> profileSelectCompleteConsumer = eb.consumer(PROFILE_SELECT_COMPLETE);
        messageConsumers.add(profileSelectCompleteConsumer.handler(this::onProfileSelected));

    }

    void onMediaScanRequest() {

        Maybe<List<Media>> rx = vertx.rxExecuteBlocking(h -> h.complete(mediaScanService.retrieveFilesAsList()));
        disposables.add(rx
            .doFinally(() -> MDC.remove("count"))
            .map(l -> MediaScannedMessage
                .builder()
                .mediaList(l)
                .build())
            .subscribe(
                m -> {
                    MDC.put("count", String.valueOf(m.getMediaList().size()));
                    log.info("Media files scanned.");
                    eb
                        .sender(MEDIA_SCAN_COMPLETE)
                        .send(m.toJson());
                },
                ex -> {
                    MDC.put("error", ex.getMessage());
                    log.error("Cannot scan for media files, exiting.");
                    System.exit(1);
                }));
    }

    void onMediaScanned(Message<JsonObject> payload) {
        var msg = new MediaScannedMessage(payload.body());
        if (msg.listHasEntries()) {
            disposables.add(vertx
                .<Media>rxExecuteBlocking(h ->
                    selectionService.selectMedia(msg.getMediaList())
                        .ifPresentOrElse(
                            h::complete,
                            () -> h.fail("Did not found a media item that satisfies all constraints.")
                        ))
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
                        log.warn(ex.getMessage());
                        scheduleNextRun();
                    }));
        } else {
            log.debug("No media items found.");
            scheduleNextRun();
        }
    }

    private void onMediaSelected(Message<JsonObject> payload) {
        var msg = new MediaSelectedMessage(payload.body());
        if (msg.isSelected()) {
            disposables.add(vertx
                .<Profile>rxExecuteBlocking(h ->
                    profileScanService
                        .selectProfile(msg.getMedia())
                        .ifPresentOrElse(
                            h::complete,
                            () -> h.fail("Could not find a suitable profile.")
                        ))
                .doFinally(() -> {
                    MDC.remove("profile");
                    MDC.remove("media");
                })
                .subscribe(
                    p -> {
                        MDC.put("profile", p.toString());
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
                    }));
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

    private JsonObject config() {
        return vertx.getOrCreateContext().config();
    }

    private void onTimeOut(Long timerId) {
        eb
            .sender(MEDIA_SCAN_START)
            .send(true);
    }

    void cleanup() {
        this.disposables.forEach(Disposable::dispose);
        this.messageConsumers.forEach(MessageConsumer::unregister);
    }
}
