package clustercode.transcoding;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface TranscodingService {

    String SERVICE_ADDRESS = "transcode.clustercode.svc";

    static TranscodingService create(Vertx vertx) {
        return new TranscodingServiceImpl(vertx);
    }

    static TranscodingService createProxy(Vertx vertx) {
        return new TranscodingServiceVertxEBProxy(vertx, SERVICE_ADDRESS);
    }

    @Fluent
    TranscodingService startNewJob(Media media, Profile profile);

}
