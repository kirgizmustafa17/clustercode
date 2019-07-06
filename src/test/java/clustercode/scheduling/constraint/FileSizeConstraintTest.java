package clustercode.scheduling.constraint;

import clustercode.api.domain.Media;
import clustercode.impl.util.InvalidConfigurationException;
import clustercode.test.util.FileBasedUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class FileSizeConstraintTest {

    private FileBasedUnitTest fs = new FileBasedUnitTest();

    private FileSizeConstraint subject;
    private Path inputDir;
    private Path prioDir;

    private Media media;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        inputDir = fs.createDirectory(fs.getPath("test"));
        prioDir = fs.createDirectory(inputDir.resolve("0"));
    }

    private void writeBytes(int count, Path path) throws IOException {
        Files.write(path, new byte[count]);
    }

    @Test
    void accept_ShouldReturnTrue_IfFileIsGreaterThanMinSize() throws Exception {
        subject = new FileSizeConstraint(inputDir, 1L, 1024L);
        media = Media.fromPath(0, fs.getPath("movie.mp4"));

        var file = prioDir.resolve("movie.mp4");
        writeBytes(1200000, file);

        assertThat(subject.accept(media)).isTrue();
    }

    @Test
    void accept_ShouldReturnTrue_IfFileIsGreaterThanMinSize_AndMaxSizeDisabled() throws Exception {
        subject = new FileSizeConstraint(inputDir, 0L, 104L);
        media = Media.fromPath(0, fs.getPath("movie.mp4"));

        var file = prioDir.resolve("movie.mp4");
        writeBytes(12, file);

        assertThat(subject.accept(media)).isTrue();
    }

    @Test
    void accept_ShouldReturnTrue_IfFileIsSmallerThanMinSize_AndMinSizeDisabled() throws Exception {
        subject = new FileSizeConstraint(inputDir, 0L, 16L);
        media = Media.fromPath(0, fs.getPath("movie.mp4"));

        var file = prioDir.resolve("movie.mp4");
        writeBytes(12, file);

        assertThat(subject.accept(media)).isTrue();
    }

    @Test
    void accept_ShouldReturnFalse_IfFileIsSmallerThanMinSize() throws Exception {
        subject = new FileSizeConstraint(inputDir, 10L, 1024L);
        media = Media.fromPath(0, fs.getPath("movie.mp4"));

        var file = prioDir.resolve("movie.mp4");
        writeBytes(8, file);

        assertThat(subject.accept(media)).isFalse();
    }

    @Test
    void accept_ShouldReturnFalse_IfFileIsGreaterThanMaxSize() throws Exception {
        subject = new FileSizeConstraint(inputDir, 1000000L, 10000000L);
        media = Media.fromPath(0, fs.getPath("movie.mp4"));

        var file = prioDir.resolve("movie.mp4");
        writeBytes(101, file);

        assertThat(subject.accept(media)).isFalse();
    }

    @Test
    void accept_ShouldReturnFalse_IfFileSizeCannotBeDetermined() throws Exception {
        subject = new FileSizeConstraint(inputDir, 10L, 100L);
        media = Media.fromPath(0, fs.getPath("movie.mp4"));

        assertThat(subject.accept(media)).isFalse();
    }

    @Test
    void ctor_ShouldThrowException_IfFileSizesEqual() throws Exception {
        assertThatExceptionOfType(InvalidConfigurationException.class).isThrownBy(() ->
            new FileSizeConstraint(inputDir, 0L, 0L));
    }

    @Test
    void ctor_ShouldThrowException_IfConfiguredIncorrectly_WhenSizesSwapped() throws Exception {
        assertThatExceptionOfType(InvalidConfigurationException.class).isThrownBy(() ->
            new FileSizeConstraint(inputDir, 12L, 1L));
    }

    @Test
    void ctor_ShouldThrowException_IfConfiguredIncorrectly_WhenSizesNegative() throws Exception {
        assertThatExceptionOfType(InvalidConfigurationException.class).isThrownBy(() ->
            new FileSizeConstraint(inputDir, -1L, -1L));
    }

}
