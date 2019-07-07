package clustercode.database;

import clustercode.api.domain.JobDocument;
import clustercode.api.domain.Task;
import clustercode.api.domain.TaskAddedEvent;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.ektorp.DbAccessException;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.slf4j.MDC;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
public class CouchDbServiceImpl implements CouchDbService {

    private final Vertx vertx;
    private final CircuitBreaker breaker;
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
            var dbInstance = new StdCouchDbInstance(
                    new StdHttpClient.Builder().url("http://localhost:5984").username("admin").password("password").build()
            );
            this.db = new StdCouchDbConnector("clustercode", dbInstance);
            db.createDatabaseIfNotExists();
        } catch (MalformedURLException | DbAccessException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public CouchDbService createNewJob(Handler<AsyncResult<String>> resultHandler) {
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
                                                .build());
                                future.complete(newId.toString());
                            } catch (DbAccessException ex) {
                                future.fail(ex);
                            }
                        },
                        ex -> "")
                .doFinally(MDC::clear)
                .subscribe(
                        uuid -> {
                            log.info("Created job");
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
}
