package clustercode.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.json.JsonObject;

public abstract class AbstractJsonObject {

    @JsonIgnore
    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

}
