package clustercode.scan;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.scan.matcher.ProfileMatchers;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class ProfileScanServiceImpl implements ProfileScanService {

    private final Map<ProfileMatchers, ProfileMatcher> profileMatcherMap;

    @Inject
    ProfileScanServiceImpl(Map<ProfileMatchers, ProfileMatcher> profileMatcherMap) {
        this.profileMatcherMap = profileMatcherMap;
    }

    @Override
    public Optional<Profile> selectProfile(Media candidate) {
        try {
            MDC.put("media", candidate.toString());
            log.debug("Selecting a profile...");
            for (ProfileMatcher profileMatcher : profileMatcherMap.values()) {
                Optional<Profile> result = profileMatcher.apply(candidate);
                if (result.isPresent()) {
                    MDC.put("file", result.get().getLocation().toString());
                    log.info("Selected profile.");
                    return result;
                }
            }
            log.info("Could not find a suitable profile.");
            return Optional.empty();
        } finally {
            MDC.remove("media");
            MDC.remove("file");
        }
    }

}
