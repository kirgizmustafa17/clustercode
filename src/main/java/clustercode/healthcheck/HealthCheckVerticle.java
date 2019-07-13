package clustercode.healthcheck;

import clustercode.impl.util.LoggingEventBuilder;
import clustercode.main.config.Configuration;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.Status;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.healthchecks.HealthCheckHandler;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.serviceproxy.ServiceException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class HealthCheckVerticle extends AbstractVerticle {

    private final Router router;
    private HealthCheckHandler livenessHandler;
    private HealthCheckHandler readinessHandler;

    public HealthCheckVerticle(Router router) {
        this.router = router;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        this.livenessHandler = HealthCheckHandler.create(vertx);
        this.readinessHandler = HealthCheckHandler.create(vertx);

        router.route(getLivenessUri()).handler(livenessHandler);
        router.route(getReadinessUri()).handler(readinessHandler);

        startFuture.complete();
    }

    private String getReadinessUri() {
        return config().getString(Configuration.api_http_readinessUri.key());
    }

    private String getLivenessUri() {
        return config().getString(Configuration.api_http_livenessUri.key());
    }

    public void registerLivenesschecks(Map<String, HealthCheckable> map) {
        map.forEach((key, value) -> {
            LoggingEventBuilder.createFrom(log).atInfo()
                .addKeyValue("checks", String.join(",", map.keySet()))
                .addKeyValue("uri", getLivenessUri())
                .log("Registering liveness checks.");
            registerHandler(key, livenessHandler, value::checkLiveness);
        });
    }

    public void registerReadinessChecks(Map<String, HealthCheckable> map) {
        map.forEach((key, value) -> {
            LoggingEventBuilder.createFrom(log).atInfo()
                .addKeyValue("checks", String.join(",", map.keySet()))
                .addKeyValue("uri", getReadinessUri())
                .log("Registering readiness checks.");
            registerHandler(key, readinessHandler, value::checkReadiness);
        });
    }

    private void registerHandler(String key, HealthCheckHandler handler, ThrowingSupplier<JsonObject> supplier) {
        handler.register(key, 1000, r -> {
            try {
                var result = supplier.get();
                r.complete(Status.OK(result));
            } catch (Exception e) {
                r.complete(Status.KO(new JsonObject().put("error", e.getCause().toString())));
            }
        });
    }

    private interface ThrowingSupplier<V> {
        V get() throws Exception;
    }
}
