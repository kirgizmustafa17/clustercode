package clustercode.impl.transcode;

import java.nio.file.Path;

public interface TranscoderConfig {

    /**
     * Gets the path to the temporary directory, which is needed during transcoding.
     *
     * @return the path to the dir.
     */
    Path temporary_dir();

    /**
     * Gets the default video extension with leading "." (e.g. ".mkv").
     *
     * @return the default extension, not null.
     */
    String default_video_extension();

    Path base_input_dir();
}
