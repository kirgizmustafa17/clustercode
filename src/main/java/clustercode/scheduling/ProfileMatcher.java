package clustercode.scheduling;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.impl.util.OptionalFunction;

public interface ProfileMatcher extends OptionalFunction<Media, Profile> {

}
