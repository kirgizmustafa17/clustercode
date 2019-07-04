package clustercode.impl.cleanup.processor;

import clustercode.api.cleanup.CleanupContext;
import clustercode.api.domain.Media;
import clustercode.api.event.messages.TranscodeFinishedEvent;
import clustercode.impl.cleanup.CleanupConfig;
import clustercode.test.util.FileBasedUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MarkSourceProcessorTest {

    private MarkSourceProcessor subject;
    private Path inputDir;

    @Mock
    private CleanupConfig cleanupConfig;
    @Spy
    private TranscodeFinishedEvent transcodeFinishedEvent;
    @Spy
    private CleanupContext context;
    @Spy
    private Media media;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        inputDir = fs.getPath("input");
        context.setTranscodeFinishedEvent(transcodeFinishedEvent);
        transcodeFinishedEvent.setMedia(media);
        transcodeFinishedEvent.setSuccessful(true);
        when(cleanupConfig.skip_extension()).thenReturn(".done");
        when(cleanupConfig.base_input_dir()).thenReturn(inputDir);

        subject = new MarkSourceProcessor(cleanupConfig);
    }

    @Test
    public void processStep_ShouldCreateFile_IfSourceDoesExist() throws Exception {
        var source = fs.createFile(fs.getPath("0", "video.ext"));
        var expected = inputDir.resolve("0").resolve("video.ext.done");

        fs.createFile(inputDir.resolve(source));
        media.setSourcePath(source);
        subject.processStep(context);

        assertThat(expected).exists();
    }

    @Test
    public void processStep_ShouldNotCreateFile_IfSourceDoesNotExist() throws Exception {
        var source = fs.getPath("0", "video.ext");
        var expected = fs.getPath("0", "video.ext.done");

        subject.processStep(context);

        assertThat(expected).doesNotExist();
    }

}
