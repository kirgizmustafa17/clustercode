package clustercode.transcoding;

import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TranscodingVerticle extends AbstractVerticle {

    private List<MessageConsumer<JsonObject>> messageConsumers = new ArrayList<>();
    private ServiceBinder binder;
    private TranscodingServiceImpl service;

    @Override
    public void start(Future<Void> startFuture) throws Exception {


        this.service = new TranscodingServiceImpl(vertx.getDelegate());
        this.binder = new ServiceBinder(vertx.getDelegate());

        messageConsumers.add(this.binder
            .setAddress(TranscodingService.SERVICE_ADDRESS)
            .register(TranscodingService.class, service));

        log.info("Started.");

        startFuture.complete();
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        this.messageConsumers.forEach(binder::unregister);
        stopFuture.complete();
    }
}
