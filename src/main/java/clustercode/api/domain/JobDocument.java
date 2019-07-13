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

    @Builder.Default
    private List<Slice> slices = new ArrayList<>();

    @JsonProperty("creation_time")
    private String creationTime;
    @JsonProperty("completion_time")
    private String completionTime;

    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @JsonProperty("source_media")
    private Media media;

    @JsonProperty("profile")
    private Profile profile;

    @JsonIgnore
    public UUID getIdAsUuid() {
        if (this.id == null) return null;
        return UUID.fromString(this.id);
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
