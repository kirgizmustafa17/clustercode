package clustercode.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.util.Optional;

@Data
@DataObject
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class OutputLine extends AbstractJsonObject {

    @JsonProperty("l")
    private String line;

    @JsonProperty(defaultValue = "1")
    private int fd;

    public OutputLine(JsonObject json) {
        this.fd = Optional
                .ofNullable(json.getInteger("fd"))
                .orElse(1);
        this.line = json.getString("l");
    }
}
