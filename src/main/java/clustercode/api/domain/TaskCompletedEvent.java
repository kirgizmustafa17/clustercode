package clustercode.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.util.Optional;
import java.util.UUID;

@DataObject
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TaskCompletedEvent extends AbstractJsonObject {

    @JsonProperty("job_id")
    private UUID jobId;

    private int amount;

    private TaskType type;

    public TaskCompletedEvent(JsonObject json) {
        this.amount = json.getInteger("amount", 0);
        Optional.ofNullable(json.getString("job_id"))
                .ifPresent(s -> this.jobId = UUID.fromString(s));
        Optional.ofNullable(json.getString("type"))
                .map(String::toUpperCase)
                .map(TaskType::valueOf)
                .ifPresent(t -> this.type = t);
    }

}
