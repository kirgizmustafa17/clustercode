package clustercode.main.config;

import io.vertx.core.json.JsonObject;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Configuration {

    rabbitmq_uri,
    rabbitmq_channels_task_added_queue_queueName,
    rabbitmq_channels_task_added_queue_durable,
    rabbitmq_channels_task_added_qos_prefetchCount,
    rabbitmq_channels_task_completed_queue_queueName,
    rabbitmq_channels_task_completed_queue_durable,
    rabbitmq_channels_task_completed_qos_prefetchCount,

    api_http_port,
    api_http_readynessUri,
    api_http_livenessUri,

    input_dir,
    output_dir,
    output_tempDir,
    profile_dir,

    prometheus_enabled,
    prometheus_uri,
    prometheus_publishQuantiles,

    constraint_active,
    constraint_file_size_min,
    constraint_file_size_max,
    constraint_file_regex,
    constraint_time_begin,
    constraint_time_stop,

    scan_allowed_extensions,
    scan_interval_minutes,

    profile_file_name,
    ;

    public String key() {
        return this.name().replaceAll("_", ".");
    }

    public static JsonObject createFromDefault() {
        return new JsonObject()
            .put(rabbitmq_uri.key(), "amqp://guest:guest@rabbitmq:5672/")

            .put(rabbitmq_channels_task_added_queue_queueName.key(), "task-added")
            .put(rabbitmq_channels_task_added_queue_durable.key(), true)
            .put(rabbitmq_channels_task_added_qos_prefetchCount.key(), 1)

            .put(rabbitmq_channels_task_completed_queue_queueName.key(), "task-completed")
            .put(rabbitmq_channels_task_completed_queue_durable.key(), true)
            .put(rabbitmq_channels_task_completed_qos_prefetchCount.key(), 1)

            .put(api_http_port.key(), 8080)
            .put(api_http_readynessUri.key(), "/health/ready")
            .put(api_http_livenessUri.key(), "/health/live")

            .put(input_dir.key(), "/input")
            .put(output_dir.key(), "/output")
            .put(output_tempDir.key(), "/var/tmp/clustercode")
            .put(profile_dir.key(), "/profiles")

            .put(prometheus_enabled.key(), true)
            .put(prometheus_uri.key(), "/metrics")
            .put(prometheus_publishQuantiles.key(), false)

            .put(constraint_active.key(), "FILE_SIZE")
            .put(constraint_file_size_max.key(), 0)
            .put(constraint_file_size_min.key(), 150)
            .put(constraint_file_regex.key(), ".*")
            .put(constraint_time_begin.key(), "08:00")
            .put(constraint_time_stop.key(), "16:00")

            .put(scan_allowed_extensions.key(), "mkv,mp4,avi")
            .put(scan_interval_minutes.key(), 30)

            .put(profile_file_name.key(), "profile")
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

    @SneakyThrows
    public static JsonObject createFromFlags(AnnotatedCli cli) {
        return new JsonObjectBuilder(new JsonObject())
            .addStringProperty(rabbitmq_uri.key(), cli.getRabbitMqUri())
            .addIntProperty(api_http_port.key(), cli.getHttpPort())
            .build();
    }

}
