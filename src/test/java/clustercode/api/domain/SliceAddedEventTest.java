package clustercode.api.domain;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SliceAddedEventTest {

    @Test
    void constructor_ShouldApplyArgList_FromJson() {
        var json = new JsonObject()
                .put("args", new JsonArray()
                        .add("arg1")
                        .add("arg with spaces"));
        var event = new SliceAddedEvent(json);
        assertThat(event.getArgs()).containsExactly("arg1", "arg with spaces");
    }

    @Test
    void constructor_ShouldApplyId_FromJson() {
        var uuid = UUID.randomUUID();
        var json = new JsonObject()
                .put("job_id", uuid.toString());
        var event = new SliceAddedEvent(json);
        assertThat(event.getJobId()).isEqualByComparingTo(uuid);
    }

    @Test
    void constructor_ShouldApplyNumber_FromJson() {
        var nr = 2;
        var json = new JsonObject()
                .put("slice_nr", 2);
        var event = new SliceAddedEvent(json);
        assertThat(event.getSliceNr()).isEqualTo(nr);

    }

}
