package clustercode.api.domain;

import clustercode.impl.util.FilesystemProvider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DataObject
public class Profile extends AbstractJsonObject {

    @JsonIgnore
    @Setter(AccessLevel.PACKAGE)
    @Builder.Default
    private FileSystem fs = FilesystemProvider.getInstance();

    /**
     * The path of the profile file.
     */
    @JsonProperty("path")
    @Setter(AccessLevel.PACKAGE)
    private URI path;

    /**
     * The arguments that are parsed from the file.
     */
    @JsonProperty("arguments")
    private List<String> arguments;

    /**
     * Any additional fields read during parsing.
     */
    @JsonProperty("fields")
    private Map<String, String> fields;

    public Profile(JsonObject json) {
        Optional.ofNullable(json.getString("path"))
            .map(URI::create)
            .ifPresent(u -> this.path = u);
        this.arguments = json
            .getJsonArray("arguments", new JsonArray())
            .stream()
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .collect(Collectors.toList());
    }

    @SneakyThrows
    public static Profile fromPath(Path relativePath) {
        var profile = new Profile();
        profile.setPath(constructProfileURI(relativePath));
        return profile;
    }

    @SneakyThrows
    public static URI constructProfileURI(Path relativePath) {
        return new URI(
            "clustercode",
            "profile",
            "/" + relativePath.toString(),
            null);
    }

    @JsonIgnore
    public Optional<Path> getRelativePath() {
        if (this.path == null) return Optional.empty();
        return Optional.of(Paths.get(path.getPath()));
    }

    @JsonIgnore
    public Optional<Path> getFullPath() {
        if (this.path == null) return Optional.empty();
        return Optional.of(fs.getPath(path.getHost(), path.getPath()));
    }

    @JsonIgnore
    public Optional<Path> getBasePath() {
        if (this.path == null) return Optional.empty();
        return Optional.of(fs.getPath(path.getHost()));
    }

    @JsonIgnore
    public Optional<Path> getSubstitutedPath(Path basePath) {
        if (this.path == null) return Optional.empty();
        return Optional.of(basePath.resolve(path.getPath()));
    }
}
