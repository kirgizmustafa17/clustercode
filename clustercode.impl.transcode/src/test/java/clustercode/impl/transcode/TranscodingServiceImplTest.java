package clustercode.impl.transcode;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.api.domain.TranscodeTask;
import clustercode.api.process.ExternalProcessService;
import clustercode.test.util.CompletableUnitTest;
import clustercode.test.util.FileBasedUnitTest;
import io.reactivex.Single;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class TranscodingServiceImplTest {

    @Mock
    private ExternalProcessService process;
    @Mock
    private TranscoderConfig transcoderConfig;

    @Spy
    private Media media;
    @Spy
    private Profile profile;
    @Spy
    private TranscodeTask task;

    private TranscodingServiceImpl subject;

    private CompletableUnitTest completable = new CompletableUnitTest();
    private FileBasedUnitTest fs = new FileBasedUnitTest();

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(media.getSourcePath()).thenReturn(fs.getPath("0", "video.mkv"));
        when(profile.getFields()).thenReturn(Collections.singletonMap("FORMAT", ".mp4"));
        when(transcoderConfig.temporary_dir()).thenReturn(fs.getPath("tmp"));
        when(transcoderConfig.base_input_dir()).thenReturn(fs.getPath("root"));
        when(profile.getArguments()).thenReturn(Collections.emptyList());

        task.setMedia(media);
        task.setProfile(profile);

        subject = new TranscodingServiceImpl(
            transcoderConfig
        );
    }

    @Test
    public void replaceOutput_ShouldReplaceOutput_WithNewValue() throws Exception {

        var output = fs.getPath("tmp", "video.mp4");
        when(profile.getArguments()).thenReturn(Collections.singletonList(TranscodingServiceImpl.OUTPUT_PLACEHOLDER));

        var result = subject.replaceOutput(
            TranscodingServiceImpl.OUTPUT_PLACEHOLDER,
            output
        );

        assertThat(result).isEqualTo(output.toString());
    }

    @Test
    public void replaceInput_ShouldReplaceInput_WithBasePath() throws Exception {
        var input = fs.getPath("0", "video.mkv");
        when(profile.getArguments()).thenReturn(Arrays.asList());

        var result = subject.replaceInput(
            TranscodingServiceImpl.INPUT_PLACEHOLDER,
            input);

        assertThat(result).isEqualTo(transcoderConfig.base_input_dir().resolve(input).toString());
    }

    @Test
    public void transcode_ShouldFireEvent_IfTranscodingFailed() {
        Assertions.assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
            when(process.start(any(), any())).thenReturn(Single.just(1));

            subject.onTranscodeFinished()
                   .subscribe(result -> {
                       assertThat(result.isSuccessful()).isFalse();
                       completable.completeOne();
                   });
            subject.transcode(task);

            completable.waitForCompletion();
        });
    }

    @Test
    public void transcode_ShouldFireEvent_IfTranscodingFailed_OnException() {
        Assertions.assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
            when(process.start(any(), any())).thenReturn(Single.error(IOException::new));

            subject.onTranscodeFinished()
                   .subscribe(result -> {
                       assertThat(result.isSuccessful()).isFalse();
                       completable.completeOne();
                   });
            subject.transcode(task);

            completable.waitForCompletion();
        });
    }

    @Test
    public void transcode_ShouldFireEvent_IfTranscodingBegins() {
        Assertions.assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
            when(process.start(any(), any())).thenReturn(Single.just(0));

            subject.onTranscodeBegin()
                   .subscribe(result -> {
                       assertThat(result.getTask()).isEqualTo(task);
                       completable.completeOne();
                   });
            subject.transcode(task);

            completable.waitForCompletion();
        });
    }
}
