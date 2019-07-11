package clustercode.main;

import clustercode.main.config.Configuration;
import io.vertx.core.Future;
import io.vertx.ext.healthchecks.Status;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.healthchecks.HealthCheckHandler;
import io.vertx.reactivex.ext.web.Router;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class HealthCheckVerticle extends AbstractVerticle {

    private final Router router;

    HealthCheckVerticle(Router router){
        this.router = router;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        var hs = HealthCheckHandler.create(vertx);

        var readyUri = config().getString(Configuration.api_http_readynessUri.key());

        MDC.put("checks", "database");
        MDC.put("uri", readyUri);
        log.info("Registering readyness checks");
        hs.register("database", 5000, r -> {
            r.complete(Status.OK());
        });

        router.route(readyUri).handler(hs);

        startFuture.complete();
    }
}
