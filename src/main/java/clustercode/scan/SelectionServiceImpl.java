package clustercode.scan;

import clustercode.api.domain.Media;
import clustercode.scheduling.Constraint;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class SelectionServiceImpl implements SelectionService {

    private final Set<Constraint> constraints;

    SelectionServiceImpl(Set<Constraint> constraints) {
        this.constraints = constraints;
    }

    @Override
    public Optional<Media> selectMedia(List<Media> list) {
        log.debug("Selecting a suitable media for scheduling.");
        return list
            .stream()
            .sorted(Comparator
                .comparingInt((Media media) ->
                    media
                        .getPriority()
                        .orElse(0))
                .reversed())
            .filter(this::checkConstraints)
            .findFirst();
    }

    /**
     * Checks whether the given media candidate fulfills all constraints. May not evaluate all constraints if one
     * declines the given media.
     *
     * @param media the media. Not null.
     * @return true if all constraints are accepted, false if one declines.
     */
    boolean checkConstraints(Media media) {
        return constraints.stream().allMatch(filter -> filter.accept(media));
    }

}
