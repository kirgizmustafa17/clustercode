package clustercode.scan.matcher;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.scan.ProfileMatcher;
import clustercode.scan.ProfileParser;
import clustercode.scan.ProfileScanConfig;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Provides a matcher which looks for the global default profile.
 */
@Slf4j
public class DefaultProfileMatcher implements ProfileMatcher {

    private final ProfileParser parser;
    private final ProfileScanConfig profileScanConfig;

    public DefaultProfileMatcher(ProfileScanConfig profileScanConfig,
                          ProfileParser parser) {
        this.parser = parser;
        this.profileScanConfig = profileScanConfig;
    }

    @Override
    public Optional<Profile> apply(Media candidate) {
        Path profileFile = profileScanConfig.profile_base_dir().resolve(
                profileScanConfig.default_profile_file_name() + profileScanConfig.profile_file_name_extension());
        try {
            MDC.put("profile_dir", profileScanConfig.profile_base_dir().toString());
            MDC.put("file", profileFile.toString());
            if (Files.exists(profileFile)) {
                return parser.parseFile(profileFile);
            } else {
                log.warn("Default profile file does not exist. This may result in jobs not being scheduled." +
                        "Either fix the configuration or create a default profile.");
                return Optional.empty();
            }
        } finally {
            MDC.remove("file");
            MDC.remove("profile_dir");
        }
    }

}
