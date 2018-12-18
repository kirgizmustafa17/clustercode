package clustercode.impl.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@AllArgsConstructor
@Getter
public class QueueOptions {

    private Map<String, Object> args;
    private boolean autoAck;
    private boolean autoDelete;
    @Builder.Default
    private String consumerName = "";
    @Builder.Default
    private boolean durable = true;
    @Builder.Default
    private String exchangeName = "";
    @Builder.Default
    private BuiltinExchangeType exchangeType = BuiltinExchangeType.FANOUT;
    private boolean exclusive;
    private boolean internal;
    private boolean immediate;
    private boolean mandatory;
    private boolean noLocal;
    private boolean noWait;
    private String routingKey;
    @Builder.Default
    private String queueName = "";

}
