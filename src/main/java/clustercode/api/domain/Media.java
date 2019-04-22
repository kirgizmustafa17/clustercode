package clustercode.api.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.net.URI;
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

    /**
     * The file sourcePath which is relative to the base input dir.
     */
    private Path sourcePath;

    @Setter(AccessLevel.PACKAGE)
    private URI source;

    public Media(JsonObject json) {
        Optional.ofNullable(json.getString("source"))
                .map(URI::create)
                .ifPresent(u -> this.source = u);
    }

    @SneakyThrows
    public static Media fromPath(Path basedir, Path relativePathWithoutPriority, int priority) {
        var media = new Media();
        media.setSource(new URI(
                "clustercode",
                null,
                basedir.toString(),
                priority,
                "/" + relativePathWithoutPriority.toString(),
                null,
                null));
        return media;
    }

    public Optional<Integer> getPriority() {
        if (this.source == null) return Optional.empty();
        return Optional.of(source.getPort());
    }

    public Optional<Path> getRelativePath() {
        if (this.source == null) return Optional.empty();
        return Optional.of(Paths.get(source.getPath()));
    }

    public Optional<Path> getFullPath() {
        if (this.source == null) return Optional.empty();
        return Optional.of(Paths.get(source.getHost(), String.valueOf(source.getPort()), source.getPath()));
    }

}
