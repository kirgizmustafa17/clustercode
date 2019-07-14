package clustercode.messaging;

import clustercode.healthcheck.AbstractHealthcheckableVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class RabbitMqVerticle extends AbstractHealthcheckableVerticle {

    private RabbitMqServiceImpl service;
    private ServiceBinder binder;
    private List<MessageConsumer<JsonObject>> messageConsumers = new ArrayList<>();

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        this.service = new RabbitMqServiceImpl(vertx.getDelegate());
        this.binder = new ServiceBinder(vertx.getDelegate());

        messageConsumers.add(this.binder
            .setAddress(RabbitMqService.SERVICE_ADDRESS)
            .register(RabbitMqService.class, service));

        try {
            service.start();
            registerLivenessChecks(Collections.singletonMap("messaging", service));
            registerReadinessChecks(Collections.singletonMap("messaging", service));
            log.info("Started.");
            startFuture.complete();
        } catch (Exception ex) {
            startFuture.fail(ex);

        }
    }


}
