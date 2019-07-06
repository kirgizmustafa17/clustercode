package clustercode.scheduling;

import clustercode.test.util.FileBasedUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

public class MediaScanServiceImplTest {

    private MediaScanServiceImpl subject;
    private Path inputDir;

    @Mock
    private MediaScanConfig scanSettings;

    private FileBasedUnitTest fs = new FileBasedUnitTest();

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(scanSettings.allowed_extensions()).thenReturn(Arrays.asList(".mp4"));
        when(scanSettings.base_input_dir()).thenReturn(fs.getPath("input"));

        inputDir = scanSettings.base_input_dir();
        subject = new MediaScanServiceImpl(scanSettings, FileScannerImpl::new);
    }

    @Test
    public void getListOfMediaFiles_ShouldReturnListWithTwoEntries() throws Exception {
        var dir1 = inputDir.resolve("1");

        fs.createFile(dir1.resolve("file11.mp4"));
        fs.createFile(dir1.resolve("file12.mp4"));

        var result = subject.getListOfMediaFiles(dir1);

        assertThat(result)
            .extracting(m -> m
                .setFileSystem(fs.getFileSystem())
                .getRelativePathWithPriority()
                .orElse(null))
            .containsExactly(fs.getPath("1/file11.mp4"), fs.getPath("1/file12.mp4"));
    }

    @Test
    public void getListOfMediaFiles_ShouldReturnEmptyList_IfNoFilesFound() throws Exception {
        var dir1 = fs.createDirectory(inputDir.resolve("1"));

        var result = subject.getListOfMediaFiles(dir1);

        assertThat(result).isEmpty();
    }

    @Test
    public void retrieveFiles_ShouldReturnOneEntry_AndIgnoreInvalidDirectories() throws Exception {
        var dir1 = fs.createDirectory(inputDir.resolve("1"));
        fs.createDirectory(inputDir.resolve("inexistent"));

        var candidates = subject.retrieveFiles();

        assertThat(candidates).containsKey(dir1);
        assertThat(candidates.get(dir1)).isEmpty();
        assertThat(candidates).hasSize(1);
    }

    @Test
    public void doExecute_ShouldThrowException_IfInputDirIsInexistent() throws Exception {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> subject.retrieveFiles());
    }

    @Test
    public void isPriorityDirectory_ShouldReturnTrue_IfDirectoryIsPositive() throws Exception {
        var dir = inputDir.resolve("2");
        assertThat(subject.isPriorityDirectory(dir)).isTrue();
    }

    @Test
    public void isPriorityDirectory_ShouldReturnTrue_IfDirectoryIsZero() throws Exception {
        var dir = inputDir.resolve("0");
        assertThat(subject.isPriorityDirectory(dir)).isTrue();
    }

    @Test
    public void isPriorityDirectory_ShouldReturnFalse_IfDirectoryIsInvalid() throws Exception {
        var dir = inputDir.resolve("-1");
        assertThat(subject.isPriorityDirectory(dir)).isFalse();
    }

    @Test
    public void getNumberFromDir_ShouldReturn2_IfPathBeginsWith2() throws Exception {
        var dir = inputDir.resolve("2");
        assertThat(subject.getNumberFromDir(dir)).isEqualTo(2);
    }

    @Test
    public void getNumberFromDir_ShouldThrowException_IfPathDoesNotContainNumber() throws Exception {
        var dir = inputDir.resolve("error");
        assertThatExceptionOfType(NumberFormatException.class).isThrownBy(() -> subject.getNumberFromDir(dir));
    }
}
