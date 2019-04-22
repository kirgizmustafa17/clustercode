package clustercode.messaging;

import clustercode.api.domain.TaskAddedEvent;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.rabbitmq.RabbitMQConsumer;

public interface RabbitMqService {

    static RabbitMqService create(Vertx vertx) {
        return new RabbitMqServiceImpl(vertx);
    }

    RabbitMqService sendTaskAdded(TaskAddedEvent event, Handler<AsyncResult<Void>> resultHandler);

    RabbitMqService handleTaskCompletedEvents(Handler<AsyncResult<RabbitMQConsumer>> resultHandler);
}
