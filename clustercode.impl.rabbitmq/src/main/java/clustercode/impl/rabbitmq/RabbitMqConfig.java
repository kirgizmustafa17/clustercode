package clustercode.impl.rabbitmq;

import org.aeonbits.owner.Config;

import java.net.URL;

public interface RabbitMqConfig extends Config {

    @Key("CC_RABBITMQ_URL")
    @DefaultValue("amqp://guest:guest@localhost:5672/")
    URL rabbitmq_url();

    @Key("CC_RABBITMQ_CHANNELS_TASK_ADDED")
    @DefaultValue("task-added")
    String task_added_name();

    @Key("CC_RABBITMQ_CHANNELS_TASK_COMPLETED")
    @DefaultValue("task-completed")
    String task_completed_name();

    @Key("CC_RABBITMQ_CHANNELS_TASK_CANCELLED")
    @DefaultValue("task-cancelled")
    String task_cancelled_name();

    @Key("CC_RABBITMQ_CHANNELS_SLICE_ADDED")
    @DefaultValue("slice-added")
    String slice_added_name();

    @Key("CC_RABBITMQ_CHANNELS_SLICE_COMPLETED")
    @DefaultValue("slice-completed")
    String slice_completed_name();

}
