package clustercode.main;

import clustercode.api.domain.TaskAddedEvent;
import clustercode.database.CouchDbService;
import clustercode.main.config.Configuration;
import clustercode.messaging.RabbitMqService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.micrometer.PrometheusScrapingHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.File;

@Slf4j
public class HttpVerticle extends AbstractVerticle {
    @Override
    public void start(Future<Void> startFuture) throws Exception {

        var router = Router.router(vertx);
        router
                .route("/")
                .handler(r -> r
                        .response()
                        .end("Hello from vertx"));
        router
                .route(config().getString(Configuration.prometheus_uri.key()))
                .handler(PrometheusScrapingHandler.create());

        var dbService = CouchDbService.createProxy(vertx.getDelegate(), CouchDbService.SERVICE_ADDRESS);
        var messagingService = RabbitMqService.create(vertx);
        router.errorHandler(500, ex -> log.error("Unhandled router exception:", ex.failure()));
        router
                .route("/create")
                .handler(r -> {
                    dbService.createNewJob(result -> {
                        if (result.succeeded()) {
                            r.response().end(result.result());
                        } else {
                            r.response().end(result.cause().getMessage());
                        }
                    });
                });
        router
                .route("/message")
                .handler(r -> {

                    dbService.get(result -> {
                        if (result.succeeded()) {

                            addTask(dbService, r, result.result().getId());
                            messagingService.sendTaskAdded(
                                    result.result(),
                                    handler -> {
                                        if (handler.succeeded()) {
                                            r.response().end("success");
                                        } else {
                                            log.error("failed to send message over rabbitmq: ", handler.cause());
                                            r.response().end("failed to send message");
                                        }
                                    }
                            );

                            r.response().end(result.result().toJson().toString());
                        } else {
                            log.error("Failed to get from database:", result.cause());
                            r.response().end("get failed");
                        }
                    });

                });

        router
                .route("/get")
                .handler(r -> {
                    dbService.get(result -> {
                        if (result.succeeded()) {
                            r.response().end(result.result().toJson().toString());
                        } else {
                            log.error("Failed to get:", result.cause());
                            r.response().end("get failed");
                        }
                    });
                });

        vertx
                .createHttpServer()
                .requestHandler(router)
                .rxListen(config().getInteger(Configuration.api_http_port.key()))
                .doAfterTerminate(MDC::clear)
                .doAfterSuccess(server -> {
                    MeterRegistry registry = BackendRegistries.getDefaultNow();
                    new DiskSpaceMetrics(new File("/")).bindTo(registry);
                    new DiskSpaceMetrics(new File("/tmp/")).bindTo(registry);
                    new ClassLoaderMetrics().bindTo(registry);
                    new JvmMemoryMetrics().bindTo(registry);
                    new JvmGcMetrics().bindTo(registry);
                    new ProcessorMetrics().bindTo(registry);
                    new JvmThreadMetrics().bindTo(registry);

                    vertx.deployVerticle(
                            new HealthCheckVerticle(router), new DeploymentOptions().setConfig(config()));
                })
                .subscribe(s -> {
                            MDC.put("port", String.valueOf(s.actualPort()));
                            log.info("Server started.");
                            startFuture.complete();
                        },
                        ex -> {
                            log.error("Could not start http server: ", ex);
                            startFuture.fail(ex);
                        });

    }

    private void addTask(CouchDbService dbService, RoutingContext r, String uuid) {
        dbService.save(TaskAddedEvent.builder().id(uuid).build(), result -> {
            if (result.succeeded()) {
                r.response().end("save succeeded");
            } else {
                log.error("Failed to save:", result.cause());
                r.response().end("save failed");
            }
        });
    }

}
