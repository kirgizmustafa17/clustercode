package clustercode.scan;

import clustercode.api.domain.Media;

import java.util.List;
import java.util.Optional;

public interface SelectionService {

    Optional<Media> selectMedia(List<Media> list);

}
