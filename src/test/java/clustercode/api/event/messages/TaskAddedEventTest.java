package clustercode.api.event.messages;

import clustercode.test.util.SerializerUnitTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

class TaskAddedEventTest {

    private SerializerUnitTest ser = new SerializerUnitTest();

    @Disabled
    @Test
    void serialize_ShouldMatchString() throws JsonProcessingException {
        var subject = TaskAddedEvent
            .builder()
            .jobID(UUID.fromString("620b8251-52a1-4ecd-8adc-4fb280214bba"))
            .priority(1)
            .sliceSize(120)
            .file(new File("0/path/to/file.ext"))
            .args(Arrays.asList("arg1", "arg with space"))
            .fileHash("b8934ef001960cafc224be9f1e1ca82c")
            .build();

        var json = ser.serialize(subject);

        var expected = "{\"args\":[\"arg1\",\"arg with space\"],\"file\":\"0/path/to/file.ext\"," +
            "\"file_hash\":\"b8934ef001960cafc224be9f1e1ca82c\",\"fileHash\":\"b8934ef001960cafc224be9f1e1ca82c\"," +
            "\"job_id\":\"620b8251-52a1-4ecd-8adc-4fb280214bba\",\"jobID\":\"620b8251-52a1-4ecd-8adc-4fb280214bba\"," +
            "\"priority\":1,\"slice_nr\":120,\"sliceSize\":120}";

        Assertions.assertThat(json).isEqualToIgnoringCase(expected);
    }

}
