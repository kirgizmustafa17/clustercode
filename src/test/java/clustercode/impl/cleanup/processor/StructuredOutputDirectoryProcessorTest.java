package clustercode.impl.cleanup.processor;

import clustercode.api.cleanup.CleanupContext;
import clustercode.api.domain.Media;
import clustercode.api.event.messages.TranscodeFinishedEvent;
import clustercode.impl.cleanup.CleanupConfig;
import clustercode.test.util.ClockBasedUnitTest;
import clustercode.test.util.FileBasedUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Disabled
public class StructuredOutputDirectoryProcessorTest {

    private StructuredOutputDirectoryProcessor subject;

    @Mock
    private CleanupConfig settings;
    @Spy
    private CleanupContext context;
    @Spy
    private TranscodeFinishedEvent transcodeFinishedEvent;
    @Spy
    private Media media;

    private Path outputDir;

    private FileBasedUnitTest fs = new FileBasedUnitTest();
    private ClockBasedUnitTest clock = new ClockBasedUnitTest();

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        context.setTranscodeFinishedEvent(transcodeFinishedEvent);
        transcodeFinishedEvent.setMedia(media);

        outputDir = fs.getPath("output");
        when(settings.base_output_dir()).thenReturn(outputDir);
        when(transcodeFinishedEvent.isSuccessful()).thenReturn(true);

        subject = new StructuredOutputDirectoryProcessor(settings, clock.getFixedClock(8, 20));
    }

    @Test
    public void processStep_ShouldMoveFileToNewDestination() throws Exception {

        var source = fs.createFile(fs.getPath("0", "subdir", "file.ext"));
        var temp = fs.createFile(fs.getPath("tmp", "file.ext"));

        transcodeFinishedEvent.setTemporaryPath(temp);

        var result = subject.processStep(context);

        var expected = fs.getPath("output", "subdir", "file.ext");
        assertThat(result.getOutputPath()).isEqualTo(expected);
        assertThat(expected).exists();
    }

    @Test
    public void processStep_ShouldMoveFileToNewDestination_WithOtherFileExtension() throws Exception {

        var source = fs.createFile(fs.getPath("0", "subdir", "file.mp4"));
        var temp = fs.createFile(fs.getPath("tmp", "file.mkv"));

        transcodeFinishedEvent.setTemporaryPath(temp);

        var result = subject.processStep(context);

        var expected = fs.getPath("output", "subdir", "file.mkv");
        assertThat(result.getOutputPath()).isEqualTo(expected);
        assertThat(expected).exists();
    }

    @Test
    public void processStep_ShouldMoveFileWithTimestamp_IfFileExists() throws Exception {

        var source = fs.createFile(fs.getPath("0", "subdir", "file.ext"));
        fs.createFile(fs.getPath("output", "subdir", "file.ext"));
        var temp = fs.createFile(fs.getPath("tmp", "file.ext"));

        transcodeFinishedEvent.setTemporaryPath(temp);

        var result = subject.processStep(context);

        var expected = fs.getPath("output", "subdir", "file.2017-01-31.08-20-00.ext");
        assertThat(result.getOutputPath()).isEqualTo(expected);
        assertThat(expected).exists();
    }

    @Test
    public void createOutputDirectoryTree_ShouldRecreateDirectoryTree_WithSubdirectories() throws Exception {
        var source = fs.getPath("0", "subdir1", "subdir2", "file.ext");
        var expected = outputDir.resolve("subdir1").resolve("subdir2").resolve("file.ext");

        var result = subject.createOutputDirectoryTree(source);

        assertThat(result)
            .isEqualTo(expected)
            .hasParentRaw(expected.getParent());
    }

    @Test
    public void createOutputDirectoryTree_ShouldRecreateDirectoryTree_WithoutSubdirs() throws Exception {
        var source = fs.getPath("0", "file.ext");
        var expected = outputDir.resolve("file.ext");

        var result = subject.createOutputDirectoryTree(source);

        assertThat(result).isEqualTo(expected);
    }

}
