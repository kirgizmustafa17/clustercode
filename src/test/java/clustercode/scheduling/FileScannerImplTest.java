package clustercode.scheduling;

import clustercode.test.util.FileBasedUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class FileScannerImplTest {

    private FileScannerImpl subject;

    private FileBasedUnitTest fs = new FileBasedUnitTest();

    @BeforeEach
    public void setUp() throws Exception {
        subject = new FileScannerImpl();
    }


    @Test
    public void scan_ShouldReturnEmptyList_IfSearchDirDoesNotExist() throws Exception {
        var searchDir = fs.getPath("input");

        var results = subject.searchIn(searchDir).withRecursion(true)
                             .scan();

        assertThat(results.isPresent()).isFalse();
    }

    @Test
    public void scan_ShouldFindOneFile_IfRecursionIsDisabled() throws Exception {
        var searchDir = fs.getPath("input");
        var testMedia = fs.createFile(searchDir.resolve("media.mp4"));
        fs.createFile(searchDir.resolve("subdir/ignored.mp4"));

        var results = subject.searchIn(searchDir).withDepth(1).withRecursion(false)
                             .scan();

        assertThat(results.get()).containsExactly(testMedia);
        assertThat(results.get()).hasSize(1);
    }

    @Test
    public void scan_ShouldFindDirectory_IfDirSearchIsEnabled() throws Exception {
        var searchDir = fs.getPath("input");

        var subdir = fs.createDirectory(searchDir.resolve("subdir"));

        var results = subject.searchIn(searchDir).withRecursion(true).withDirectories(true)
                             .scan();

        assertThat(results.get()).containsExactly(subdir);
        assertThat(results.get()).hasSize(1);
    }

    @Test
    public void scan_ShouldFindRecursiveFile_IfFileExists() throws Exception {
        var searchDir = fs.getPath("input");

        var testMedia = fs.createFile(searchDir.resolve("subdir/media.mp4"));

        var results = subject.searchIn(searchDir).withRecursion(true)
                             .scan();

        assertThat(results.get()).containsExactly(testMedia);
        assertThat(results.get()).hasSize(1);
    }

    @Test
    public void hasAllowedExtension_ShouldReturnTrue_IfHasExtension() throws Exception {
        subject.withFileExtensions(Arrays.asList(".mp4"));
        var testFile = fs.getPath("something.mp4");

        assertThat(subject.hasAllowedExtension(testFile)).isTrue();
    }

    @Test
    public void hasAllowedExtension_ShouldReturnFalse_IfNotHasExtension() throws Exception {
        subject.withFileExtensions(Arrays.asList("mp4"));
        var testFile = fs.getPath("mp4.mkv");

        assertThat(subject.hasAllowedExtension(testFile)).isFalse();
    }

    @Test
    public void hasAllowedExtension_ShouldReturnTrue_IfNoFilterInstalled() throws Exception {
        var testFile = fs.getPath("mp4.mkv");

        assertThat(subject.hasAllowedExtension(testFile)).isTrue();
    }

    @Test
    public void hasNotCompanionFile_ShouldReturnFalseIfFileExists() throws Exception {
        var ext = ".done";
        subject.whileSkippingExtraFilesWith(ext);

        var testFile = fs.createFile(fs.getPath("foo", "bar.ext"));
        fs.createFile(fs.getPath("foo", "bar.ext.done"));

        assertThat(subject.hasNotCompanionFile(testFile)).isEqualTo(false);
    }

    @Test
    public void hasNotCompanionFile_ShouldReturnTrueIfFileNotExists() throws Exception {
        var ext = ".done";
        subject.whileSkippingExtraFilesWith(ext);

        var testFile = fs.createFile(fs.getPath("foo", "bar.ext"));

        assertThat(subject.hasNotCompanionFile(testFile)).isTrue();
    }

    @Test
    public void hasNotCompanionFile_ShouldReturnTrue_IfExtensionNotProvided() throws Exception {
        var testFile = fs.createFile(fs.getPath("foo", "bar.ext"));

        assertThat(subject.hasNotCompanionFile(testFile)).isTrue();
    }

    @Test
    public void hasNotCompanionFile_ShouldReturnTrue_IfMarkDirProvided_ButFileNotFound() throws Exception {
        var ext = ".done";
        var testFile = fs.createFile(fs.getPath("foo", "bar.ext"));
        var dir = fs.createDirectory(fs.getPath("mark"));

        subject.whileSkippingExtraFilesWith(ext)
               .whileSkippingExtraFilesIn(dir)
               .searchIn(fs.getPath("input", "foo"));

        assertThat(subject.hasNotCompanionFile(testFile)).isTrue();
    }

    @Test
    public void hasNotCompanionFile_ShouldReturnFalse_IfFileExistsInDirectory() throws Exception {
        var ext = ".done";
        var inputFolder = fs.getPath("input", "0");
        var testFile = fs.createFile(inputFolder.resolve(("bar.ext")));
        fs.createFile(fs.getPath("mark", "0", "bar.ext" + ext));

        subject.whileSkippingExtraFilesWith(ext)
               .whileSkippingExtraFilesIn(fs.getPath("mark"))
               .searchIn(inputFolder);

        assertThat(subject.hasNotCompanionFile(testFile)).isFalse();
    }

    @Test
    public void stream_ShouldReturnEmptyStream_IfIOExceptionOccurred() throws Exception {
        var testDir = fs.getPath("foo", "bar");
        assertThat(subject.searchIn(testDir).streamAndIgnoreErrors()).isEmpty();
    }

    @Test
    public void emptyStreamOnError_ShouldThrowException_IfIOExceptionOccurred() throws Exception {
        var testDir = fs.getPath("foo", "bar");
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
            subject.searchIn(testDir).stream());
    }
}
