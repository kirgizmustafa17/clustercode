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
        assertThat(json.getString("source")).isEqualTo("clustercode://base:1/relative");
    }

    @Test
    void isDeserializable_FromPlainJson() throws IOException {
        var json = new JsonObject()
                .put("source", "clustercode://base:1/relative");
        var mapper = new ObjectMapper();
        var media = mapper.readValue(json.toString(), Media.class);
        assertThat(media.getSource())
                .hasHost("base")
                .hasPort(1)
                .hasPath("/relative")
                .hasScheme("clustercode");
    }

    @Test
    void isDeserializable_FromJsonObject() {
        var json = new JsonObject()
                .put("source", "clustercode://base:1/relative");
        var media = new Media(json);
        assertThat(media.getSource())
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
        subject.setSource(URI.create("clustercode://base:1/"));
        assertThat(subject.getPriority()).hasValue(1);
    }
}
