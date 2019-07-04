package clustercode.scheduling.messages;

import clustercode.api.domain.AbstractJsonObject;
import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DataObject
public class ProfileSelectedMessage extends AbstractJsonObject {

    @NonNull
    @JsonProperty("media")
    private Media media;

    @JsonProperty("profile")
    private Profile profile;

    public ProfileSelectedMessage(JsonObject json) {

    }

    public boolean isSelected() {
        return profile != null;
    }

    public boolean isNotSelected() {
        return profile == null;
    }
}
