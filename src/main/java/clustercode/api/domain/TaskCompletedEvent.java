package clustercode.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.util.UUID;

@DataObject(generateConverter = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TaskCompletedEvent extends AbstractJsonObject {

    public TaskCompletedEvent(JsonObject json) {
        TaskCompletedEventConverter.fromJson(json, this);
    }

    @JsonProperty("job_id")
    private UUID jobId;

}
