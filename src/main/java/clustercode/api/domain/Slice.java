package clustercode.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Slice {

    private int nr;

    private double progress;

    private Instant begin;

    private Instant end;

    private List<OutputLine> preambleLines;

    private List<OutputLine> postambleLines;

}
