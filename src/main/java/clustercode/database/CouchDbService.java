package clustercode.database;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.api.domain.TaskAddedEvent;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface CouchDbService {

    String SERVICE_ADDRESS = "couchdb.clustercode.svc";

    @GenIgnore
    static CouchDbService createProxy(Vertx vertx, String address) {
        return new CouchDbServiceVertxEBProxy(vertx, address);
    }

    @Fluent
    CouchDbService createNewJob(Media media, Profile profile, Handler<AsyncResult<String>> resultHandler);

    @Fluent
    CouchDbService save(TaskAddedEvent event, Handler<AsyncResult<Void>> resultHandler);

    @Fluent
    CouchDbService get(Handler<AsyncResult<TaskAddedEvent>> resultHandler);
}
