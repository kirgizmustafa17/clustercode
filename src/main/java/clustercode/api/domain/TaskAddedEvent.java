package clustercode.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DataObject(generateConverter = true)
@EqualsAndHashCode(callSuper = false)
public class TaskAddedEvent extends AbstractJsonObject {

    /**
     * CouchDB revision
     */
    @JsonProperty("_rev")
    private String revision;

    @JsonProperty("_id")
    private String id;

    private URI file;

    private List<String> args;

    private String fileHash;

    public TaskAddedEvent(JsonObject json) {
        Optional
                .ofNullable(json.getString("_id"))
                .ifPresent(s -> this.id = s);
    }

}
