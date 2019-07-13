package clustercode.database;

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
public class CouchDbVerticle extends AbstractHealthcheckableVerticle {

    private ServiceBinder binder;
    private List<MessageConsumer<JsonObject>> messageConsumers = new ArrayList<>();
    private CouchDbServiceImpl service;

    @Override
    public void start() throws Exception {

        this.service = new CouchDbServiceImpl(vertx.getDelegate());
        this.binder = new ServiceBinder(vertx.getDelegate());

        messageConsumers.add(this.binder
            .setAddress(CouchDbService.SERVICE_ADDRESS)
            .register(CouchDbService.class, service));

        registerLivenessChecks(Collections.singletonMap("database", service));
        registerReadinessChecks(Collections.singletonMap("database", service));
        log.info("Started.");
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        this.messageConsumers.forEach(binder::unregister);
        stopFuture.complete();
    }

}
