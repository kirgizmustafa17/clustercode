package clustercode.transcoding;

import clustercode.database.CouchDbService;
import clustercode.scheduling.SchedulingMessageHandler;
import clustercode.scheduling.messages.ProfileSelectedMessage;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageConsumer;

import java.util.ArrayList;
import java.util.List;

public class TranscodingMessageHandler {

    private final Vertx vertx;
    private final CouchDbService dbService;

    private EventBus eb;
    private List<MessageConsumer<JsonObject>> handlers = new ArrayList<>();

    public TranscodingMessageHandler(Vertx vertx) {
        this.vertx = vertx;
        this.eb = vertx.eventBus();

        this.dbService = CouchDbService.createProxy(vertx.getDelegate(), CouchDbService.SERVICE_ADDRESS);


        MessageConsumer<JsonObject> profileSelectedConsumer = eb
            .consumer(SchedulingMessageHandler.PROFILE_SELECT_COMPLETE);
        this.handlers.add(profileSelectedConsumer.handler(this::onProfileSelected));



    }

    private void onProfileSelected(Message<JsonObject> payload) {
        var msg = payload.body().mapTo(ProfileSelectedMessage.class);
        dbService.createNewJob(msg.getMedia(), msg.getProfile(), handler -> {

        });
    }


    public void cleanup() {
        this.handlers.forEach(MessageConsumer::unregister);
    }
}
