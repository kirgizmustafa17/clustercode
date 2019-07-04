package clustercode.scheduling;

import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;

public class SchedulingVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {


        startFuture.complete();


    }
}
