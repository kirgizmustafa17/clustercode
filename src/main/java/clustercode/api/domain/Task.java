package clustercode.api.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@DataObject
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Task extends AbstractJsonObject {

    private Media media;

    private TaskType type;

    private int amount;

    private Instant begin;

    private Instant end;

    @Builder.Default
    private List<OutputLine> lines = new ArrayList<>();

    public Task(JsonObject json) {
        this.amount = json.getInteger("amount", 0);
        this.lines = json
                .getJsonArray("args", new JsonArray())
                .stream()
                .map(o -> (JsonObject) o)
                .map(OutputLine::new)
                .collect(Collectors.toList());
        Optional.ofNullable(json.getJsonObject("media"))
                .ifPresent(o -> this.media = new Media(o));
        Optional.ofNullable(json.getString("type"))
                .map(String::toUpperCase)
                .map(TaskType::valueOf)
                .ifPresent(t -> this.type = t);
    }

}
