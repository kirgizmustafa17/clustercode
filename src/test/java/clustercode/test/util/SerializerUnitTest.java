package clustercode.test.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;

public class SerializerUnitTest implements BeforeAllCallback {

    private final ObjectMapper mapper;

    public SerializerUnitTest() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {

    }

    public String serialize(Object object) throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }

    public <T> T deserialize(String json, Class<T> clazz) throws IOException {
        return mapper.readValue(json, clazz);
    }
}
