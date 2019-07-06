package clustercode.messaging;

import clustercode.api.domain.TaskAddedEvent;
import clustercode.main.config.Configuration;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;
import io.vertx.reactivex.rabbitmq.RabbitMQConsumer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class RabbitMqServiceImpl implements RabbitMqService {

    private final Vertx vertx;
    private final RabbitMQClient client;

    public RabbitMqServiceImpl(Vertx vertx) {
        this.vertx = vertx;

        var uriString = config().getString(Configuration.rabbitmq_uri.key());
        URI uri = null;
        try {
            uri = new URI(uriString);
            if (uri.getScheme() == null || uri.getHost() == null || uri.getPort() == 0) {
                logAndExit(null);
            }
        } catch (URISyntaxException e) {
            logAndExit(e);
        }

        this.client = RabbitMQClient.create(vertx,
            new RabbitMQOptions()
                .setUri(uri.toString())
                .setAutomaticRecoveryEnabled(true));

        var strippedUri = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();
        MDC.put("uri", strippedUri);
        MDC.put("help", "Credentials have been removed from URL in the log.");
        client.start(b -> {
            if (b.succeeded()) {
                log.info("Started?");
                setupQueues();

            } else {
                log.error("Failed.");
            }
            log.info("{}", MDC.getCopyOfContextMap());
            MDC.remove("uri");
            MDC.remove("help");
        });

    }

    private void logAndExit(URISyntaxException ex) {
        if (ex != null) MDC.put("error", ex.getMessage());
        MDC.put("help", "Expected format: amqp://host:port/path");
        log.error("Cannot parse RabbitMq URI.");
        System.exit(1);
    }

    private JsonObject config() {
        return this.vertx.getOrCreateContext().config();
    }

    @Override
    public RabbitMqService sendTaskAdded(TaskAddedEvent event, Handler<AsyncResult<Void>> resultHandler) {
        client
            .rxBasicPublish(
                "",
                config().getString(Configuration.rabbitmq_channels_task_added_queue_queueName.key()),
                new JsonObject()
                    .put("properties", new JsonObject()
                        .put("contentType", "application/json"))
                    .put("body", event.toJson()))
            .doFinally(MDC::clear)
            .subscribe(
                () -> {
                    MDC.put("message", event.toJson().toString());
                    log.debug("Sent message.");
                    resultHandler.handle(Future.succeededFuture());
                },
                f -> resultHandler.handle(Future.failedFuture(f)));
        return this;
    }

    @Override
    public RabbitMqService handleTaskCompletedEvents(Handler<AsyncResult<RabbitMQConsumer>> resultHandler) {
        var queueName = config().getString(Configuration.rabbitmq_channels_task_completed_queue_queueName.key());
        client
            .rxBasicConsumer(queueName)
            .doFinally(MDC::clear)
            .subscribe(
                consumer -> {
                    MDC.put("queue", queueName);
                    log.debug("Begin consuming.");
                    resultHandler.handle(Future.succeededFuture(consumer));
                },
                ex -> resultHandler.handle(Future.failedFuture(ex))
            );
        return this;
    }

    private void setupQueues() {
        var taskCompletedQueueName = config().getString(Configuration.rabbitmq_channels_task_completed_queue_queueName.key());
        client.rxQueueDeclare(
            taskCompletedQueueName,
            config().getBoolean(Configuration.rabbitmq_channels_task_completed_queue_durable.key()),
            false,
            false)
            .doFinally(MDC::clear)
            .subscribe(r -> {
                    MDC.put("queue", taskCompletedQueueName);
                    log.info("Queue declared.");
                },
                r -> {
                    log.warn("Could not declare queue.", r);
                }
            );
    }

}
