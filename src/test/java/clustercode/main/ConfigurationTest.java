package clustercode.main;

import clustercode.main.config.Configuration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Properties;

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

    @Test
    void createFromEnvMap_ShouldTreatListAsString() {
        var map = new HashMap<String, String>();
        map.put("CC_CONSTRAINT_ACTIVE", "arg1,arg2");

        var result = Configuration.createFromEnvMap(map);
        Assertions.assertThat(result.getString("constraint.active")).isEqualTo("arg1,arg2");
    }

    @Test
    void createFromEnvMap_ShouldIgnoreInexistingConfig() {
        var map = new HashMap<String, String>();
        map.put("CC_INEXISTENT", "arg1");

        var result = Configuration.createFromEnvMap(map);
        Assertions.assertThat(result.getString("inexistent")).isNull();
    }

    @Test
    void createFromProperties_ShouldIncludeProperty() {
        var props = new Properties();
        props.put("api.http.port", "testvalue");

        var result = Configuration.createFromProperties(props);
        Assertions.assertThat(result.getString("api.http.port")).isEqualTo("testvalue");
    }

    @Test
    void createFromProperties_ShouldIgnoreInexistingConfig() {
        var props = new Properties();
        props.put("inexisting", "testvalue");

        var result = Configuration.createFromProperties(props);
        Assertions.assertThat(result.getString("inexisting")).isNull();
    }

}
