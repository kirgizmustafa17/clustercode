package clustercode.healthcheck;

import io.vertx.core.json.JsonObject;

public interface HealthCheckable {

    JsonObject checkLiveness() throws HealthCheckException;

    default JsonObject checkReadiness() throws HealthCheckException {
        return checkLiveness();
    }
}
