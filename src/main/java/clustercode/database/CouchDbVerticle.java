package clustercode.database;

import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

public class CouchDbVerticle extends AbstractVerticle {
    @Override
    public void start(Future<Void> startFuture) throws Exception {

        var service = new CouchDbServiceImpl(vertx.getDelegate());
        new ServiceBinder(vertx.getDelegate())
                .setAddress(CouchDbService.SERVICE_ADDRESS)
                .register(CouchDbService.class, service);
        startFuture.complete();

    }
}
