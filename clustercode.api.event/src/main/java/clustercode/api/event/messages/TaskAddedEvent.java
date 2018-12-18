package clustercode.api.event.messages;

import com.owlike.genson.annotation.JsonProperty;
import lombok.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class TaskAddedEvent {

    @Builder.Default
    private List<String> args = new ArrayList<>();

    private File file;

    @JsonProperty(value = "file_hash")
    @Builder.Default
    private String fileHash = "";

    @JsonProperty(value = "job_id")
    private UUID jobID;

    private int priority;

    @JsonProperty(value = "slice_nr")
    private int sliceSize;

}
