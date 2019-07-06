package clustercode.scheduling;

import clustercode.impl.util.FilesystemProvider;
import clustercode.main.config.Configuration;
import io.vertx.core.json.JsonObject;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MediaScanConfig {

    private final Path baseInputDir;
    private final List<String> allowedExtensions;

    public MediaScanConfig(JsonObject config) {
        this(config, FilesystemProvider.getInstance());
    }

    MediaScanConfig(JsonObject config, FileSystem fs) {
        this.baseInputDir = fs.getPath(config.getString(Configuration.input_dir.key()));
        this.allowedExtensions = Stream
            .of(config.getString(
                Configuration.scan_allowed_extensions.key())
                .trim()
                .split(","))
            .map(String::trim)
            .collect(Collectors.toList());
    }

    /**
     * Gets the root directory for scanning.
     *
     * @return the root dir, not null.
     */
    Path base_input_dir() {
        return this.baseInputDir;
    }

    /**
     * Gets the list of file name extensions. An entry can be ".txt" or "txt".
     *
     * @return the list of included file extensions. May be empty, not null.
     */
    List<String> allowed_extensions() {
        return this.allowedExtensions;
    }

}
