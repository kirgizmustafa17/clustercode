package clustercode.transcoding;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.api.domain.TaskAddedEvent;
import clustercode.api.domain.TaskType;
import clustercode.database.CouchDbService;
import clustercode.impl.util.LoggingEventBuilder;
import clustercode.messaging.RabbitMqService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class TranscodingServiceImpl implements TranscodingService {


    private final CouchDbService dbService;
    private final RabbitMqService rabbitMqService;

    TranscodingServiceImpl(Vertx vertx) {

        this.dbService = CouchDbService.createProxy(vertx);
        this.rabbitMqService = RabbitMqService.createProxy(vertx);

    }

    @Override
    public TranscodingService startNewJob(Media media, Profile profile) {

        dbService.createNewJob(media, profile, handler -> {
            if (handler.succeeded()) {
                var jobId = UUID.fromString(handler.result());
                LoggingEventBuilder.createFrom(log).atInfo().addKeyValue("job_id", jobId).log("Succeeded.");
                rabbitMqService.sendTaskAdded(
                    TaskAddedEvent
                        .builder()
                        .jobId(jobId)
                        .media(media)
                        .type(TaskType.SPLIT)
                        .build(), this::onMessageSent);

            } else {
                LoggingEventBuilder
                    .createFrom(log)
                    .atError()
                    .setCause(handler.cause())
                    .log("Could not save the job in database.");
            }
        });

        return this;
    }

    private void onMessageSent(AsyncResult<Void> msgHandler) {
        var jobId = msgHandler.result();
        var logEvent = LoggingEventBuilder.createFrom(log).addKeyValue("job_id", jobId);
        if (msgHandler.succeeded()) {
            logEvent.atInfo().log("Yay!");
        } else {
            logEvent.atError().setCause(msgHandler.cause()).log("Nope");
        }
    }
}
