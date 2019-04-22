package clustercode.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobDocument {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("_rev")
    private String revision;

    private List<Slice> slices = new ArrayList<>();

    private List<OutputLine> preambleLines;

    private List<OutputLine> postambleLines;

    private String creationTime;
    private String completionTime;

    private Task task;

    private Media media;

    @JsonIgnore
    public UUID getIdAsUuid() {
        if (this.id == null) return null;
        return UUID.fromString(this.id);
    }

    @JsonIgnore
    public TaskAddedEvent getTaskAddedEvent() {
        if (this.task == null) return null;
        return TaskAddedEvent.builder()
                .id(this.id)
                .build();
    }

    @JsonIgnore
    public LocalDateTime getCreationTimeAsDate() {
        if (this.creationTime == null) return null;
        return LocalDateTime.parse(this.creationTime);
    }

    @JsonIgnore
    public double getProgress() {
        if (this.slices == null) return 0;
        var totalSlices = this.slices.size();
        if (totalSlices == 0) return 0;
        var completed = this.slices.stream()
                .map(Slice::getProgress)
                .filter(p -> p >= 100)
                .count();
        return new BigDecimal(100D / totalSlices * completed)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

}
