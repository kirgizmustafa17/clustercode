package clustercode.database;

import clustercode.api.HealthCheckable;
import clustercode.api.domain.JobDocument;
import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.api.domain.TaskAddedEvent;
import clustercode.impl.util.UriUtil;
import clustercode.main.config.Configuration;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.ektorp.DbAccessException;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.slf4j.MDC;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class CouchDbServiceImpl implements CouchDbService, HealthCheckable {

    private final Vertx vertx;
    private final CircuitBreaker breaker;
    private boolean connected = false;
    private StdCouchDbConnector db;

    CouchDbServiceImpl(Vertx vertx) {
        this.vertx = vertx;

        this.breaker = CircuitBreaker.create(
            "couchdb-circuit-breaker",
            io.vertx.reactivex.core.Vertx.newInstance(vertx),
            new CircuitBreakerOptions()
                .setMaxFailures(5)
                .setTimeout(5000)
        );

        try {
            var uri = new URI(config().getString(Configuration.couchdb_uri.key()));
            var strippedUri = UriUtil.stripCredentialFromUri(uri);
            var databaseName = Optional.ofNullable(uri.getPath()).orElse("");
            MDC.put("uri", strippedUri);
            MDC.put("help", "Credentials have been removed from URL in the log.");
            var dbInstance = new StdCouchDbInstance(
                new StdHttpClient.Builder().url(uri.toURL()).build()
            );
            this.db = new StdCouchDbConnector(databaseName, dbInstance);
            db.createDatabaseIfNotExists();
            log.info("Connected.");
            this.connected = true;
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            MDC.put("error", e.toString());
            log.error("Cannot connect to CouchDB, exiting...");
            System.exit(1);
        } catch (DbAccessException e) {
            MDC.put("error", e.toString());
            log.error("Cannot connect to CouchDB.");
        } finally {
            MDC.remove("uri");
            MDC.remove("help");
            MDC.remove("error");
        }
    }

    private JsonObject config() {
        return this.vertx.getOrCreateContext().config();
    }

    @Override
    public CouchDbService createNewJob(Media media, Profile profile, Handler<AsyncResult<String>> resultHandler) {
        breaker
            .rxExecuteCommandWithFallback(
                future -> {
                    var newId = UUID.randomUUID();
                    MDC.put("job_id", newId.toString());
                    try {
                        this.db
                            .create(JobDocument
                                .builder()
                                .id(newId.toString())
                                .creationTime(LocalDateTime.now().toString())
                                .media(media)
                                .profile(profile)
                                .build());
                        future.complete(newId.toString());
                    } catch (DbAccessException ex) {
                        future.fail(ex);
                        this.connected = false;
                    } finally {
                        MDC.remove("job_id");
                    }
                },
                ex -> "")
            .subscribe(
                uuid -> {
                    log.info("Created job in database.");
                    resultHandler.handle(Future.succeededFuture(uuid));
                },
                ex -> {
                    resultHandler.handle(Future.failedFuture(ex));
                }
            );
        return this;
    }

    @Override
    public CouchDbService save(TaskAddedEvent event, Handler<AsyncResult<Void>> resultHandler) {
        MDC.put("event", event.toJson().toString());
        log.info("Saving event.");
        try {
            var doc = this.db.get(JobDocument.class, event.getJobId().toString());
            //         doc.setTask(Task
            //                 .builder()
            //                 .args(event.getArgs())
            //                 .file(event.getFile())
            //                .build());
            this.db.update(doc);
            resultHandler.handle(Future.succeededFuture());

        } catch (DbAccessException ex) {
            resultHandler.handle(Future.failedFuture(ex));
        }
        MDC.clear();
        return this;
    }

    @Override
    public CouchDbService get(Handler<AsyncResult<TaskAddedEvent>> resultHandler) {
        log.info("Getting event.");
        try {
            var job = this.db
                .get(JobDocument.class, "f2298ec6-f036-4b9c-8a57-8f070268d37a");
            //     var event = job.getTask();
            //     MDC.put("event", event.toString());
            log.info("Got event");
            //     resultHandler.handle(Future.succeededFuture(job.getTaskAddedEvent()));
        } catch (DbAccessException ex) {
            resultHandler.handle(Future.failedFuture(ex));
        } finally {
            MDC.clear();
        }
        return this;
    }

    @Override
    public JsonObject checkLiveness() throws Exception {
        if (connected) return new JsonObject().put("connected", connected);
        else throw new Exception("Database not connected");
    }
}
