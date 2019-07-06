package clustercode.api.domain;

import clustercode.impl.util.FilesystemProvider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@DataObject
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Media extends AbstractJsonObject {

    private FileSystem fs = FilesystemProvider.getInstance();

    @Setter(AccessLevel.PACKAGE)
    private URI path;

    @JsonProperty("file_hash")
    private String fileHash;

    public Media(JsonObject json) {
        Optional.ofNullable(json.getString("path"))
            .map(URI::create)
            .ifPresent(u -> this.path = u);
        this.fileHash = json.getString("file_hash");
    }

    public Media(int priority, Path relativePathWithoutPriority) {
        this.path = constructMediaUri("input", relativePathWithoutPriority, priority);
    }

    public Media setFileSystem(FileSystem fs) {
        this.fs = fs;
        return this;
    }

    @SneakyThrows
    public static Media fromPath(String basedir, int priority, Path relativePathWithoutPriority) {
        var media = new Media();
        media.setPath(constructMediaUri(basedir, relativePathWithoutPriority, priority));
        return media;
    }

    @SneakyThrows
    public static Media fromPath(int priority, Path relativePathWithoutPriority) {
        var media = new Media();
        media.setPath(constructMediaUri("input", relativePathWithoutPriority, priority));
        return media;
    }

    @SneakyThrows
    public static URI constructMediaUri(String baseDir, Path relativePathWithoutPriority, int priority) {
        return new URI(
            "clustercode",
            null,
            baseDir,
            priority,
            "/" + relativePathWithoutPriority.toString(),
            null,
            null);
    }

    @JsonIgnore
    public Optional<Integer> getPriority() {
        if (this.path == null) return Optional.empty();
        return Optional.of(path.getPort());
    }

    @JsonIgnore
    public Optional<Path> getRelativePath() {
        if (this.path == null) return Optional.empty();
        return Optional.of(Paths.get(path.getPath().replaceFirst("/*", "")));
    }

    @JsonIgnore
    public Optional<String> getBase() {
        if (this.path == null) return Optional.empty();
        return Optional.of(path.getHost());
    }

    @JsonIgnore
    public Optional<Path> getRelativePathWithPriority() {
        if (this.path == null) return Optional.empty();
        return Optional.of(fs.getPath(String.valueOf(path.getPort()), path.getPath()));
    }

    /**
     * Gets the relative path, but with the given basePath as root.
     *
     * @param basePath the base path as resolved against this' media relative path.
     * @return the new path, if this media has a path.
     */
    @JsonIgnore
    public Optional<Path> getSubstitutedPath(Path basePath) {
        if (this.path == null) return Optional.empty();
        return Optional.of(basePath
            .resolve(String.valueOf(path.getPort()))
            .resolve(path.getPath().replaceFirst("/*", "")));
    }
}
