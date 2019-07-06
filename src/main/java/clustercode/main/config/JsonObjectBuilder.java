package clustercode.main.config;

import io.vertx.core.json.JsonObject;

import java.net.URI;
import java.net.URL;
import java.util.Optional;

class JsonObjectBuilder {
    private JsonObject json;

    JsonObjectBuilder(JsonObject json) {
        this.json = json;
    }

    JsonObjectBuilder addStringProperty(String key, String value) {
        Optional.ofNullable(value).ifPresent(v -> json.put(key, v));
        return this;
    }

    JsonObjectBuilder addStringProperty(String key, URI value) {
        Optional.ofNullable(value).ifPresent(v -> json.put(key, v.toString()));
        return this;
    }

    JsonObjectBuilder addIntProperty(String key, Integer value) {
        Optional.ofNullable(value).ifPresent(v -> json.put(key, v));
        return this;
    }

    JsonObject build() {
        return json;
    }
}
