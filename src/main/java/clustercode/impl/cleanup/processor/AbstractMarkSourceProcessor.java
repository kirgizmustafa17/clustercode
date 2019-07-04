package clustercode.impl.cleanup.processor;

import clustercode.api.cleanup.CleanupContext;
import clustercode.api.cleanup.CleanupProcessor;
import clustercode.impl.cleanup.CleanupConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractMarkSourceProcessor implements CleanupProcessor {

    protected final CleanupConfig cleanupConfig;
    protected Logger log = LoggerFactory.getLogger(getClass());

    protected AbstractMarkSourceProcessor(CleanupConfig cleanupConfig) {
        this.cleanupConfig = cleanupConfig;
    }

    @Override
    public final CleanupContext processStep(CleanupContext context) {
        if (!isStepValid(context)) return context;
        return doProcessStep(context);
    }

    protected abstract CleanupContext doProcessStep(CleanupContext context);

    protected abstract boolean isStepValid(CleanupContext cleanupContext);

    protected final void createMarkFile(Path marked, Path source) {
        try {
            log.debug("Creating file {}", marked);
            Files.createFile(marked);
        } catch (IOException e) {
            log.error("Could not createDefault file {}: {}", marked, e.getMessage());
            log.warn("It may be possible that {} will be scheduled for transcoding again, as it could not be " +
                "marked as done.", source);
        }
    }

    protected Path getSourcePath(CleanupContext context) {
        return context.getTranscodeFinishedEvent().getMedia().getSubstitutedPath(cleanupConfig.base_input_dir()).get();
    }
}
