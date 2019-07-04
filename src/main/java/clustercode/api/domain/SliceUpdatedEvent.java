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

@DataObject
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode(callSuper = false)
public class SliceUpdatedEvent extends AbstractJsonObject {

    @JsonProperty("job_id")
    private UUID jobId;

    @JsonProperty("slice_nr")
    private int sliceNr;

    @JsonProperty("percentage")
    private double progress;

    @JsonProperty("preamble_lines")
    private List<OutputLine> preambleLines;

    @JsonProperty("postamble_lines")
    private List<OutputLine> postambleLines;

    public SliceUpdatedEvent(JsonObject json) {
        Optional.ofNullable(json.getString("job_id"))
                .ifPresent(s -> this.jobId = UUID.fromString(s));
        this.sliceNr = json.getInteger("slice_nr", 0);
        this.progress= json.getDouble("percentage", 0d);
        this.preambleLines = json
                .getJsonArray("preamble_lines", new JsonArray())
                .stream()
                .map(o -> (JsonObject) o)
                .map(OutputLine::new)
                .collect(Collectors.toList());

        this.postambleLines = json
                .getJsonArray("postamble_lines", new JsonArray())
                .stream()
                .map(o -> (JsonObject) o)
                .map(OutputLine::new)
                .collect(Collectors.toList());
    }

}
