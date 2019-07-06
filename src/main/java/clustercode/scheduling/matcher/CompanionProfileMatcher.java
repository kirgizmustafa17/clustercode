package clustercode.scheduling.matcher;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.scheduling.ProfileMatcher;
import clustercode.scheduling.ProfileParser;
import clustercode.scheduling.ProfileScanConfig;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.nio.file.Files;
import java.util.Optional;

/**
 * Provides a matcher which will search for a file named exactly as the media file, but with an additional extension for
 * the configured transcoder settings defined in {@link ProfileScanConfig}.
 */
@Slf4j
public class CompanionProfileMatcher implements ProfileMatcher {

    private final ProfileScanConfig scanConfig;
    private final ProfileParser profileParser;

    public CompanionProfileMatcher(ProfileScanConfig scanConfig,
                                   ProfileParser profileParser) {
        this.scanConfig = scanConfig;
        this.profileParser = profileParser;
    }

    @Override
    public Optional<Profile> apply(Media candidate) {
        try {
            var substPath = candidate.getSubstitutedPath(scanConfig.profile_base_dir());
            var relPath = candidate.getRelativePath();
            if (substPath.isPresent() && relPath.isPresent()) {
                var profile = substPath.get()
                    .resolveSibling(relPath.get().getFileName() + scanConfig.profile_file_name_extension());

                MDC.put("profile_dir", profile.getParent().toString());
                MDC.put("file", profile.toString());
                if (Files.exists(profile)) {
                    return profileParser.parseFile(profile);
                } else {
                    log.debug("Companion file does not exist.");
                    return Optional.empty();
                }
            } else {
                MDC.put("media", candidate.toString());
                log.warn("media does not have a path configured.");
                return Optional.empty();
            }
        } finally {
            MDC.remove("media");
            MDC.remove("file");
            MDC.remove("profile_dir");
        }
    }
}
