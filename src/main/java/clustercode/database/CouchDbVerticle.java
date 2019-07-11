package clustercode.database;

import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

import java.util.ArrayList;
import java.util.List;

public class CouchDbVerticle extends AbstractVerticle {

    private ServiceBinder binder;
    private List<MessageConsumer<JsonObject>> messageConsumers = new ArrayList<>();

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        var service = new CouchDbServiceImpl(vertx.getDelegate());
        this.binder = new ServiceBinder(vertx.getDelegate());

        messageConsumers.add(this.binder
            .setAddress(CouchDbService.SERVICE_ADDRESS)
            .register(CouchDbService.class, service));

        startFuture.complete();
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        this.messageConsumers.forEach(binder::unregister);
        stopFuture.complete();
    }
}
