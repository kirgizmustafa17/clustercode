package clustercode.impl.process;

import clustercode.api.process.RunningExternalProcess;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
class RunningProcessImpl implements RunningExternalProcess {

    private Process process;

    RunningProcessImpl(Process process) {
        this.process = process;
    }

    @Override
    public RunningExternalProcess sleep(long millis) {
        return sleep(millis, TimeUnit.MILLISECONDS);
    }

    @Override
    public RunningExternalProcess sleep(long timeout, TimeUnit unit) {
        try {
            Thread.sleep(unit.toMillis(timeout));
        } catch (InterruptedException e) {
            log.warn("", e);
        }
        return this;
    }

    @Override
    public void awaitDestruction() {
        try {
            log.debug("Waiting for process to destroy...");
            process.destroyForcibly().waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean destroyNowWithTimeout(long timeout, TimeUnit unit) {
        try {
            log.warn("Waiting for process to destroy within {} {}...", timeout, unit.toString().toLowerCase());
            return process.destroyForcibly().waitFor(timeout, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
