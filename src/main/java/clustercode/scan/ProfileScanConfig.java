package clustercode.scan;

import clustercode.scan.matcher.ProfileMatchers;
import clustercode.impl.util.FilesystemProvider;
import clustercode.main.config.Configuration;
import io.vertx.core.json.JsonObject;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ProfileScanConfig {

    private final String fileName;
    private final Path profileDir;

    ProfileScanConfig(JsonObject config) {
        this.fileName = config.getString(Configuration.profile_file_name.key());
        this.profileDir = FilesystemProvider.getInstance().getPath(config.getString(Configuration.profile_dir.key()));
    }

    /**
     * Gets the extension of the profile file name.
     *
     * @return the extension, with leading dot if applicable (e.g. ".ffmpeg"), not null.
     */
    public String profile_file_name_extension() {
        return ".ffmpeg";
    }

    /**
     * Gets the base name of the profile file. This method could be combined with {@link #profile_file_name_extension()}
     * to createDefault a file (e.g. "profile.ffmpeg").
     *
     * @return the file name (e.g. "profile"), not null.
     */
    public String profile_file_name() {
        return this.fileName;
    }

    /**
     * Gets the root directory for profiles.
     *
     * @return the path to the directory, not null.
     */
    public Path profile_base_dir() {
        return this.profileDir;
    }

    /**
     * Gets the base name of the default profile file without extension. This method could be combined with {@link
     * #profile_file_name_extension()}.
     *
     * @return the file name (e.g. "default"), not null.
     */
    public String default_profile_file_name() {
        return "default";
    }

    public List<ProfileMatchers> profile_matchers() {
        return Arrays.asList(ProfileMatchers.COMPANION, ProfileMatchers.DIRECTORY_STRUCTURE, ProfileMatchers.DEFAULT);
    }

}
