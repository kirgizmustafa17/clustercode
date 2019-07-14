package clustercode.messaging;

import clustercode.healthcheck.HealthCheckable;
import clustercode.api.domain.TaskAddedEvent;
import clustercode.impl.util.LoggingEventBuilder;
import clustercode.impl.util.UriUtil;
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
public class RabbitMqServiceImpl implements RabbitMqService, HealthCheckable {

    private final Vertx vertx;
    private RabbitMQClient client;

    public RabbitMqServiceImpl(io.vertx.core.Vertx vertx) {
        this.vertx = Vertx.newInstance(vertx);
    }

    void start() throws Exception {
        var uriString = config().getString(Configuration.rabbitmq_uri.key());
        try {
            var uri = new URI(uriString);
            if (uri.getScheme() == null || uri.getHost() == null || uri.getPort() == 0) {
                throw new URISyntaxException(uriString, "Scheme, Host and Port parts cannot be null");
            }
            this.client = RabbitMQClient.create(this.vertx,
                new RabbitMQOptions()
                    .setUri(uri.toString())
                    .setAutomaticRecoveryEnabled(true));

            client.start(b -> {
                var strippedUri = UriUtil.stripCredentialFromUri(uri);
                var logEvent = LoggingEventBuilder.createFrom(log)
                    .addKeyValue("uri", strippedUri)
                    .addKeyValue("help", "Credentials have been removed from URL in the log.");
                if (b.succeeded()) {
                    logEvent.atInfo().log("Connected to RabbitMq.");
                    setupQueues();
                } else {
                    logEvent.setCause(b.cause()).atError().log("Failed to connect.");
                }
            });

        } catch (URISyntaxException e) {
            LoggingEventBuilder.createFrom(log).atError()
                .setCause(e)
                .addKeyValue("help", "Expected format: amqp://host:port/path")
                .log("Cannot parse RabbitMq URI.");
            throw e;
        }

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

    //@Override
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

    @Override
    public JsonObject checkLiveness() throws Exception {
        return new JsonObject().put("connected", client.isConnected());
    }

    @Override
    public JsonObject checkReadiness() throws Exception {
        if (client.isConnected()) return checkLiveness();
        else throw new RuntimeException("RabbitMq not ready");
    }
}
