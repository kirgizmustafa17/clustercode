package clustercode.impl.cleanup;

import clustercode.impl.cleanup.processor.CleanupProcessors;

import java.nio.file.Path;
import java.util.List;

public interface CleanupConfig {

    /**
     * Gets the root path of the output directory.
     *
     * @return the path, not null.
     */
    Path base_output_dir();

    Path base_input_dir();

    /**
     * Returns true if existing files are allowed to be overwritten.
     */
    boolean overwrite_files();

    /**
     * Gets the group jobId of the new owner of the output file(s).
     */
    int group_id();

    /**
     * Gets the user jobId of the new owner of the output file(s).
     */
    int user_id();

    /**
     * Gets the root path of the directory in which the sources should get marked as done.
     *
     * @return the path or empty.
     */
    Path mark_source_dir();

    String skip_extension();

    List<CleanupProcessors> cleanup_processors();
}
