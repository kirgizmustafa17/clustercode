package clustercode.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
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

    private static FileSystem fs = FileSystems.getDefault();

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

    @SneakyThrows
    public static Media fromPath(Path basedir, Path relativePathWithoutPriority, int priority) {
        var media = new Media();
        media.setPath(new URI(
                "clustercode",
                null,
                basedir.toString(),
                priority,
                "/" + relativePathWithoutPriority.toString(),
                null,
                null));
        return media;
    }

    public static void setFileSystem(FileSystem fs) {
        Media.fs = fs;
    }

    @JsonIgnore
    public Optional<Integer> getPriority() {
        if (this.path == null) return Optional.empty();
        return Optional.of(path.getPort());
    }

    @JsonIgnore
    public Optional<Path> getRelativePath() {
        if (this.path == null) return Optional.empty();
        return Optional.of(Paths.get(path.getPath()));
    }

    @JsonIgnore
    public Optional<Path> getFullPath() {
        if (this.path == null) return Optional.empty();
        return Optional.of(fs.getPath(path.getHost(), String.valueOf(path.getPort()), path.getPath()));
    }

    @JsonIgnore
    public Optional<Path> getBasePath() {
        if (this.path == null) return Optional.empty();
        return Optional.of(fs.getPath(path.getHost()));
    }

    @JsonIgnore
    public Optional<Path> getRelativePathWithPriority() {
        if (this.path == null) return Optional.empty();
        return Optional.of(fs.getPath(String.valueOf(path.getPort()), path.getPath()));
    }

    @JsonIgnore
    public Optional<Path> getSubstitutedPath(Path basePath) {
        if (this.path == null) return Optional.empty();
        return Optional.of(basePath.resolve(String.valueOf(path.getPort())).resolve(path.getPath()));
    }
}
