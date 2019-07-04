package clustercode.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DataObject
@EqualsAndHashCode(callSuper = false)
public class TaskAddedEvent extends AbstractJsonObject {

    @JsonProperty("job_id")
    private UUID jobId;

    private Media media;

    private TaskType type;

    @JsonProperty("slice_size")
    private int sliceSize;

    public TaskAddedEvent(JsonObject json) {
        Optional
                .ofNullable(json.getString("job_id"))
                .map(UUID::fromString)
                .ifPresent(s -> this.jobId = s);
        Optional.ofNullable(json.getJsonObject("media"))
                .ifPresent(o -> this.media = new Media(o));
        Optional.ofNullable(json.getString("type"))
                .map(String::toUpperCase)
                .map(TaskType::valueOf)
                .ifPresent(t -> this.type = t);
        this.sliceSize = json.getInteger("slice_size", 0);
    }
}
