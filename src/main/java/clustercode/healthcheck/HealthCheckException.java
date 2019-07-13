package clustercode.healthcheck;

import io.vertx.core.json.JsonObject;
import lombok.Getter;

@Getter
public class HealthCheckException extends Exception {

    private final JsonObject additionalData;
    private Exception cause;

    public HealthCheckException(JsonObject additionalData) {
        this.additionalData = additionalData;
    }

    public HealthCheckException(JsonObject additionalData, Exception cause) {
        this.additionalData = additionalData;
        this.cause = cause;
    }
}
