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

public class MarkSourceDirProcessorTest {

    private MarkSourceDirProcessor subject;
    private Path inputDir;
    private Path markDir;

    @Mock
    private CleanupConfig cleanupConfig;
    @Spy
    private CleanupContext context;
    @Spy
    private Media media;
    @Spy
    private TranscodeFinishedEvent transcodeFinishedEvent;

    private FileBasedUnitTest fs = new FileBasedUnitTest() {
    };

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        inputDir = fs.getPath("input");
        markDir = fs.getPath("mark");
        context.setTranscodeFinishedEvent(transcodeFinishedEvent);
        transcodeFinishedEvent.setMedia(media);
        transcodeFinishedEvent.setSuccessful(true);
        when(cleanupConfig.skip_extension()).thenReturn(".done");
        when(cleanupConfig.base_input_dir()).thenReturn(inputDir);
        when(cleanupConfig.mark_source_dir()).thenReturn(markDir);
        subject = new MarkSourceDirProcessor(cleanupConfig);
    }

    @Test
    public void processStep_ShouldRecreateDirectoryStructure() throws Exception {
        var source = fs.createFile(fs.getPath("0", "video.ext"));
        var expected = markDir.resolve("0").resolve("video.ext.done");

        fs.createFile(inputDir.resolve(source));
        media.setSourcePath(source);
        subject.processStep(context);

        assertThat(expected).exists();
    }

    @Test
    public void processStep_ShouldRecreateDirectoryStructure_WithSubdirectories() throws Exception {
        var source = fs.createFile(fs.getPath("0", "movies", "video.ext"));
        var expected = markDir.resolve("0").resolve("movies").resolve("video.ext.done");

        fs.createFile(inputDir.resolve(source));
        media.setSourcePath(source);
        subject.processStep(context);

        assertThat(expected).exists();
    }

    @Test
    public void processStep_ShouldNotCreateDirectoryStructure_IfSourceDoesNotExist() throws Exception {
        var source = fs.getPath("0", "movies", "video.ext");
        var expected = markDir.resolve("0").resolve("movies").resolve("video.ext.done");

        subject.processStep(context);

        assertThat(expected).doesNotExist();
    }
}
