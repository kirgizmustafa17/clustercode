package clustercode.scheduling.matcher;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.scheduling.ProfileMatcher;
import clustercode.scheduling.ProfileParser;
import clustercode.scheduling.ProfileScanConfig;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Provides a matcher which looks in the recreated directory structure in the profiles folder based on the path file
 * of the media. For a media file such as {@code 0/movies/subdir/movie.mp4} this matcher will look for a profile in
 * {@code /profiles/0/movies/subdir/}. If it did not find it or on error, the parent will be searched ({@code
 * /profiles/0/movies/}). This matcher stops at the root directory of the input dir, in the example case it is {@code
 * 0/}.
 */
@Slf4j
public class DirectoryStructureMatcher implements ProfileMatcher {

    private final ProfileParser profileParser;
    private final ProfileScanConfig profileScanConfig;

    public DirectoryStructureMatcher(ProfileScanConfig profileScanConfig,
                              ProfileParser profileParser) {
        this.profileParser = profileParser;
        this.profileScanConfig = profileScanConfig;
    }

    @Override
    public Optional<Profile> apply(Media candidate) {
        Path mediaFileParent = candidate.getRelativePathWithPriority().get().getParent();
        Path sisterDir = profileScanConfig.profile_base_dir().resolve(mediaFileParent);
        Path profileFile = sisterDir.resolve(profileScanConfig.profile_file_name() + profileScanConfig
                .profile_file_name_extension());

        Path rootDir = profileScanConfig.profile_base_dir().resolve(mediaFileParent.getName(0));
        return parseRecursive(profileFile, rootDir);
    }

    private Optional<Profile> parseRecursive(Path file, Path root) {
        if (Files.exists(file)) {
            try {
                MDC.put("file", profileScanConfig.profile_base_dir().relativize(file).toString());
                Optional<Profile> result = profileParser.parseFile(file);
                if (result.isPresent()) {
                    log.debug("Found profile: {}", result.get().getLocation());
                    return result;
                } else {
                    return parseRecursive(getProfileFileFromParentDirectory(file,
                            profileScanConfig.profile_file_name() + profileScanConfig.profile_file_name_extension()),
                            root);
                }
            } finally {
                MDC.remove("file");
            }
        } else if (file.getParent().equals(root)) {
            try {
                MDC.put("profile_dir", root.toString());
                log.debug("Did not find a suitable profile in any subdirectory.");
                return Optional.empty();
            } finally {
                MDC.remove("profile_dir");
            }
        } else {
            return parseRecursive(getProfileFileFromParentDirectory(file,
                    profileScanConfig.profile_file_name() + profileScanConfig.profile_file_name_extension()),
                    root);
        }
    }

    private Path getProfileFileFromParentDirectory(Path profileFile, String fileNameOfParent) {
        return profileFile.getParent().getParent().resolve(fileNameOfParent);
    }

}
