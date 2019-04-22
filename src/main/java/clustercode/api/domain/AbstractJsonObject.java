package clustercode.api.domain;

import io.vertx.core.json.JsonObject;

public abstract class AbstractJsonObject {

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

}
