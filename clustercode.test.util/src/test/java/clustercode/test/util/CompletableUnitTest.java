package clustercode.test.util;

import org.assertj.core.api.Assertions;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class CompletableUnitTest {

    private AtomicReference<CountDownLatch> _latch = new AtomicReference<>(new CountDownLatch(1));

    public void completeOne() {
        _latch.get().countDown();
    }

    public void setExpectedCountForCompletion(int count) {
        _latch.set(new CountDownLatch(count));
    }

    public void waitForCompletion() {
        try {
            _latch.get().await();
        } catch (InterruptedException e) {
            Assertions.fail(e.getMessage());
        }
    }
}
