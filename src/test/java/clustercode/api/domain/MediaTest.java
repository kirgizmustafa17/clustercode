package clustercode.api.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class MediaTest {

    private Media subject = new Media();

    @Test
    void isSerializable_ToPlainJson() throws JsonProcessingException {
        var media = Media.fromPath(Paths.get("base"), Paths.get("relative"), 1);
        var mapper = new ObjectMapper();
        var json = mapper.writeValueAsString(media);
        assertThat(json).contains("clustercode://base:1/relative");
    }

    @Test
    void isSerializable_ToJsonObject() {
        var media = Media.fromPath(Paths.get("base"), Paths.get("relative"), 1);
        var json = media.toJson();
        assertThat(json.getString("path")).isEqualTo("clustercode://base:1/relative");
    }

    @Test
    void isDeserializable_FromPlainJson() throws IOException {
        var json = new JsonObject()
                .put("path", "clustercode://base:1/relative");
        var mapper = new ObjectMapper();
        var media = mapper.readValue(json.toString(), Media.class);
        assertThat(media.getPath())
                .hasHost("base")
                .hasPort(1)
                .hasPath("/relative")
                .hasScheme("clustercode");
    }

    @Test
    void isDeserializable_FromJsonObject() {
        var json = new JsonObject()
                .put("path", "clustercode://base:1/relative");
        var media = new Media(json);
        assertThat(media.getPath())
                .hasHost("base")
                .hasPort(1)
                .hasPath("/relative")
                .hasScheme("clustercode");
    }

    @Test
    void getPriority_ShouldReturnEmpty_IfNoSource() {
        assertThat(subject.getPriority()).isEmpty();
    }

    @Test
    void getPriority_ShouldReturn_1_IfGiven() {
        subject.setPath(URI.create("clustercode://base:1/"));
        assertThat(subject.getPriority()).hasValue(1);
    }

    @Test
    void getFullPath_ShouldReturnEmpty_IfNoSource() {
        assertThat(subject.getFullPath()).isEmpty();
    }

    @Test
    void getFullPath_ShouldReturn_FullPathAsPathObject() {
        subject.setPath(URI.create("clustercode://base:1/movie.mp4"));
        assertThat(subject.getFullPath()).hasValue(Paths.get("base", "1", "movie.mp4"));
    }

    @Test
    void getBasePath_ShouldReturnEmpty_IfNoSource() {
        assertThat(subject.getBasePath()).isEmpty();
    }

    @Test
    void getSubstitutedPath_ShouldReturnEmpty_IfNoSource() {
        assertThat(subject.getSubstitutedPath(null)).isEmpty();
    }

    @Test
    void getSubstitutedPath_ShouldReplace_Base_IfSubstituteGiven() {
        subject.setPath(URI.create("clustercode://base:1/movie.mp4"));
        assertThat(subject.getSubstitutedPath(Paths.get("replaced")))
                .hasValue(Paths.get("replaced", "1", "movie.mp4"));
    }
}
