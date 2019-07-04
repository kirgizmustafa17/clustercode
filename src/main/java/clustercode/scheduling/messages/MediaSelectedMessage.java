package clustercode.scheduling.messages;

import clustercode.api.domain.AbstractJsonObject;
import clustercode.api.domain.Media;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DataObject
public class MediaSelectedMessage extends AbstractJsonObject {

    @JsonProperty("media")
    private Media media;

    public MediaSelectedMessage(JsonObject json) {
        Optional.ofNullable(json.getJsonObject("media")).ifPresent(o -> this.media = o.mapTo(Media.class));
    }

    @JsonIgnore
    public boolean isSelected() {
        return media != null;
    }

    @JsonIgnore
    public boolean isNotSelected() {
        return media == null;
    }

}
