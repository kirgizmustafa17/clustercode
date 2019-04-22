package clustercode.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@DataObject
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SliceAddedEvent extends AbstractJsonObject {

    @JsonProperty("job_id")
    private UUID jobId;

    @JsonProperty("slice_nr")
    private int sliceNr;

    private List<String> args;

    public SliceAddedEvent(JsonObject json) {
        Optional.ofNullable(json.getString("job_id"))
                .ifPresent(s -> this.jobId = UUID.fromString(s));
        Optional.ofNullable(json.getInteger("slice_nr"))
                .ifPresent(i -> this.sliceNr = i);
        this.args = json
                .getJsonArray("args", new JsonArray())
                .stream().map(o -> (String) o)
                .collect(Collectors.toList());
    }

}
