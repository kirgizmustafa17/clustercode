package clustercode.scan;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.List;
import java.util.Optional;

@Slf4j
public class ProfileScanServiceImpl implements ProfileScanService {

    private final List<ProfileMatcher> profileMatchers;

    ProfileScanServiceImpl(List<ProfileMatcher> profileMatchers) {
        this.profileMatchers = profileMatchers;
    }

    @Override
    public Optional<Profile> selectProfile(Media candidate) {
        try {
            MDC.put("media", candidate.toString());
            log.debug("Selecting a profile...");
            for (ProfileMatcher profileMatcher : profileMatchers) {
                Optional<Profile> result = profileMatcher.apply(candidate);
                if (result.isPresent()) {
                    return result;
                }
            }
            return Optional.empty();
        } finally {
            MDC.remove("media");
        }
    }

}
