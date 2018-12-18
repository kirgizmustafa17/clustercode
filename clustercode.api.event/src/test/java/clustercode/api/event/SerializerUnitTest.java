package clustercode.api.event;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SerializerUnitTest implements BeforeAllCallback {

    private Genson genson;

    public SerializerUnitTest() {
        this.genson = new GensonBuilder().create();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {

    }

    public String serialize(Object object) {
        return genson.serialize(object);
    }

    public <T> T deserialize(String json, Class<T> clazz) {
        return genson.deserialize(json, clazz);
    }
}
