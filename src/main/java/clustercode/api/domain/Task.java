package clustercode.api.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@DataObject
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Task extends AbstractJsonObject{

    public Task(JsonObject json) {
        Optional.ofNullable(json.getString("file"))
                .ifPresent(s -> this.file = URI.create(s));
        this.args = json
                .getJsonArray("args", new JsonArray())
                .stream().map(o -> (String) o)
                .collect(Collectors.toList());
    }

    private URI file;

    private List<String> args;

}
