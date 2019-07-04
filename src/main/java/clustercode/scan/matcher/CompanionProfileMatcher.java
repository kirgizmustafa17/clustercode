package clustercode.scan.matcher;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.scan.ProfileMatcher;
import clustercode.scan.ProfileParser;
import clustercode.scan.ProfileScanConfig;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import javax.inject.Inject;
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
        var profile = candidate.getSubstitutedPath(scanConfig.profile_base_dir()).get().resolveSibling(
                candidate.getRelativePath().get().getFileName() + scanConfig.profile_file_name_extension());
        try {
            MDC.put("profile_dir", profile.getParent().toString());
            MDC.put("file", profile.toString());
            if (Files.exists(profile)) {
                return profileParser.parseFile(profile);
            } else {
                log.debug("Companion file does not exist.");
                return Optional.empty();
            }
        } finally {
            MDC.remove("file");
            MDC.remove("profile_dir");
        }
    }
}
