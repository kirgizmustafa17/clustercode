package clustercode.api.domain;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TaskCompletedEventTest {

    @Test
    void constructor_ShouldDeserializeFromJson() {

        var uuid = UUID.randomUUID();
        var json = new JsonObject().put("job_id", uuid);

        var result = new TaskCompletedEvent(json);

        assertThat(result.getJobId()).isEqualByComparingTo(uuid);
    }
}
