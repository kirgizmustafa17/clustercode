package clustercode.transcoding;

import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;

public class TranscodingVerticle extends AbstractVerticle {

    private TranscodingMessageHandler handler;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        this.handler = new TranscodingMessageHandler(vertx);
        startFuture.complete();
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        this.handler.cleanup();
        stopFuture.complete();
    }
}
