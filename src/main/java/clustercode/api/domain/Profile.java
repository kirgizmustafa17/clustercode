package clustercode.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DataObject
public class Profile extends AbstractJsonObject {

    /**
     * The location of the profile file.
     */
    @JsonProperty("location")
    private Path location;

    /**
     * The arguments that are parsed from the file.
     */
    @JsonProperty("arguments")
    private List<String> arguments;

    /**
     * Any additional fields read during parsing.
     */
    @JsonProperty("fields")
    private Map<String, String> fields;

    public Profile(JsonObject json) {

    }

}
