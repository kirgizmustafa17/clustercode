package clustercode.impl.cleanup.processor;

import clustercode.api.cleanup.CleanupContext;
import clustercode.api.cleanup.CleanupProcessor;
import clustercode.impl.cleanup.CleanupConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides a processor which deletes the path file.
 */
@Slf4j
public class DeleteSourceProcessor implements CleanupProcessor {

    private final CleanupConfig cleanupConfig;

    DeleteSourceProcessor(CleanupConfig cleanupConfig) {
        this.cleanupConfig = cleanupConfig;
    }

    @Override
    public CleanupContext processStep(CleanupContext context) {

        Path source = context.getTranscodeFinishedEvent().getMedia().getSubstitutedPath(cleanupConfig.base_input_dir()).get();

        if (!context.getTranscodeFinishedEvent().isSuccessful()) {
            log.warn("Not deleting {}, since transcoding failed.", source);
            return context;
        }

        deleteFile(source);

        return context;
    }

    void deleteFile(Path path) {
        try {
            log.info("Deleting {}.", path);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Could not delete file {}: {}", path, e.getMessage());
        }
    }
}
