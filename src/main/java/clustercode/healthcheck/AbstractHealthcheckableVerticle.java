package clustercode.healthcheck;

import io.vertx.reactivex.core.AbstractVerticle;

import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractHealthcheckableVerticle extends AbstractVerticle {

    private Consumer<Map<String, HealthCheckable>> livenessConsumer;
    private Consumer<Map<String, HealthCheckable>> readinessConsumer;

    public final AbstractHealthcheckableVerticle withLivenessChecks(Consumer<Map<String, HealthCheckable>> consumer) {
        this.livenessConsumer = consumer;
        return this;
    }

    public final AbstractHealthcheckableVerticle withReadinessChecks(Consumer<Map<String, HealthCheckable>> consumer) {
        this.readinessConsumer = consumer;
        return this;
    }

    protected final void registerLivenessChecks(Map<String, HealthCheckable> map) {
        if (livenessConsumer != null) this.livenessConsumer.accept(map);
    }

    protected final void registerReadinessChecks(Map<String, HealthCheckable> map) {
        if (readinessConsumer != null) this.readinessConsumer.accept(map);
    }
}
