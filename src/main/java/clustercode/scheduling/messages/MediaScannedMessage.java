package clustercode.scheduling.messages;

import clustercode.api.domain.AbstractJsonObject;
import clustercode.api.domain.Media;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DataObject
public class MediaScannedMessage extends AbstractJsonObject {

    @NonNull
    @JsonProperty("medias")
    private List<Media> mediaList;

    public boolean listIsEmpty() {
        return mediaList.isEmpty();
    }

    public boolean listHasEntries() {
        return !mediaList.isEmpty();
    }

    public MediaScannedMessage(JsonObject json) {
        this.mediaList = json.getJsonArray("medias", new JsonArray())
                .stream()
                .map(o -> new Media((JsonObject) o))
                .collect(Collectors.toList());
    }

}
