package clustercode.main;

import clustercode.main.config.Configuration;
import io.vertx.core.Future;
import io.vertx.ext.healthchecks.Status;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.healthchecks.HealthCheckHandler;
import io.vertx.reactivex.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HealthCheckVerticle extends AbstractVerticle {

    private final Router router;

    HealthCheckVerticle(Router router){
        this.router = router;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        var hs = HealthCheckHandler.create(vertx);

        log.info("Registering health checks");
        hs.register("database", 5000, r -> {
            r.complete(Status.OK());
        });

        var route = config().getString(Configuration.api_http_healthUri.key());
        log.debug(route);
        router.route(route).handler(hs);

        startFuture.complete();
    }
}
