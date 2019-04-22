package clustercode.main;

import clustercode.main.config.Configuration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

class ConfigurationTest {

    @Test
    void createFromEnvMap_ShouldIncludeProperty() {
        var map = new HashMap<String, String>();
        map.put("CC_API_HTTP_PORT", "testvalue");

        var result = Configuration.createFromEnvMap(map);
        Assertions.assertThat(result.getString("api.http.port")).isEqualTo("testvalue");
    }

    @Test
    void createFromEnvMap_ShouldIgnoreNullProperty() {
        var map = new HashMap<String, String>();
        map.put("CC_API_HTTP_PORT", "testvalue");
        map.put("CC_API_HTTP_TEST", null);

        var result = Configuration.createFromEnvMap(map);
        Assertions.assertThat(result.getString("api.http.port")).isEqualTo("testvalue");
        Assertions.assertThat(result.getString("api.http.test")).isNull();
    }

}
