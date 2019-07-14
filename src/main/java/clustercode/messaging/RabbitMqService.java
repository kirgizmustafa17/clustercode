package clustercode.messaging;

import clustercode.api.domain.TaskAddedEvent;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface RabbitMqService {

    String SERVICE_ADDRESS = "rabbitmq.clustercode.svc";

    static RabbitMqService create(Vertx vertx) {
        return new RabbitMqServiceImpl(vertx);
    }

    static RabbitMqService createProxy(Vertx vertx) {
        return new RabbitMqServiceVertxEBProxy(vertx, SERVICE_ADDRESS);
    }

    @Fluent
    RabbitMqService sendTaskAdded(TaskAddedEvent event, Handler<AsyncResult<Void>> resultHandler);

//    @Fluent
//    RabbitMqService handleTaskCompletedEvents(Handler<AsyncResult<RabbitMQConsumer>> resultHandler);
}
