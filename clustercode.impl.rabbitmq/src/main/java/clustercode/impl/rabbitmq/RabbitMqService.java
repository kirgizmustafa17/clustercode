package clustercode.impl.rabbitmq;

import clustercode.api.event.messages.TranscodeBeginEvent;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.annotation.JsonProperty;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RabbitMqService {

    private final RabbitMqConfig config;
    private final Genson genson;
    private Connection connection;
    private Map<Class, Channel> channelMap;
    private Subject<Object> publishSubject;

    @Inject
    RabbitMqService(RabbitMqConfig config) {
        this.config = config;
        this.channelMap = new HashMap<>();
        this.publishSubject = PublishSubject.create().toSerialized();
        this.genson = new GensonBuilder().create();
    }

    public void connect() throws IOException {
        try {
            var factory = new ConnectionFactory();
            factory.setUri(config.rabbitmq_url().toURI());
            this.connection = factory.newConnection();


            channelMap.put(TranscodeBeginEvent.class, connection.createChannel());

        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException | TimeoutException ex) {
            throw new IOException(ex);
        }
    }

    public void onTranscodeBegin(TranscodeBeginEvent event) {
        publishSubject.onNext(event);
    }

    private void createQueue(Channel channel, QueueOptions o) throws IOException {
        channel.queueDeclare(
            o.getQueueName(),
            o.isDurable(),
            o.isExclusive(),
            o.isAutoDelete(),
            o.getArgs()
            );
    }

}
