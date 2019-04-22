package clustercode.main.config;

import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Configuration {

    rabbitmq_url,
    rabbitmq_channels_task_added_queue_queueName,
    rabbitmq_channels_task_added_queue_durable,
    rabbitmq_channels_task_added_qos_prefetchCount,
    rabbitmq_channels_task_completed_queue_queueName,
    rabbitmq_channels_task_completed_queue_durable,
    rabbitmq_channels_task_completed_qos_prefetchCount,

    api_http_port,
    api_http_readyUri,
    api_http_healthUri,

    input_dir,
    output_dir,
    output_tempDir,

    prometheus_enabled,
    prometheus_uri,
    prometheus_publishQuantiles,
    ;

    public String key() {
        return this.name().replaceAll("_", ".");
    }

    public static JsonObject createFromDefault() {
        return new JsonObject()
                .put(rabbitmq_url.key(), "amqp://guest:guest@rabbitmq:5672/")

                .put(rabbitmq_channels_task_added_queue_queueName.key(), "task-added")
                .put(rabbitmq_channels_task_added_queue_durable.key(), true)
                .put(rabbitmq_channels_task_added_qos_prefetchCount.key(), 1)

                .put(rabbitmq_channels_task_completed_queue_queueName.key(), "task-completed")
                .put(rabbitmq_channels_task_completed_queue_durable.key(), true)
                .put(rabbitmq_channels_task_completed_qos_prefetchCount.key(), 1)

                .put(api_http_port.key(), 8080)
                .put(api_http_readyUri.key(), "/.well-known/ready")
                .put(api_http_healthUri.key(), "/.well-known/live")

                .put(input_dir.key(), "/input")
                .put(output_dir.key(), "/output")
                .put(output_tempDir.key(), "/var/tmp/clustercode")

                .put(prometheus_enabled.key(), true)
                .put(prometheus_uri.key(), "/.well-known/metrics")
                .put(prometheus_publishQuantiles.key(), false)
                ;

    }

    public static JsonObject createFromEnvMap(Map<String, String> map) {
        var json = new JsonObject();
        Arrays
                .stream(Configuration.values())
                .collect(Collectors.toMap(Function.identity(), c -> "CC_" + c.name().toUpperCase(Locale.ENGLISH)))
                .entrySet()
                .stream()
                .filter(e -> map.get(e.getValue()) != null)
                .forEach(e -> {
                    var value = map.get(e.getValue());
                    json.put(e.getKey().key(), value);
                });
        return json;
    }

    public static JsonObject createFromFlags(AnnotatedCli cli) {
        return new JsonObjectBuilder(new JsonObject())
                .addStringProperty(rabbitmq_url.key(), cli.getRabbitMqUrl())
                .addIntProperty(api_http_port.key(), cli.getHttpPort())
                .build();
    }

}
