package clustercode.scheduling;

import clustercode.impl.util.FilesystemProvider;
import clustercode.main.config.Configuration;
import clustercode.test.util.FileBasedUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileParserImplTest {

    private ProfileParserImpl subject;
    private FileBasedUnitTest fs = new FileBasedUnitTest();
    private Path profileDir;

    @BeforeEach
    void setUp() throws Exception {
        FilesystemProvider.setFileSystem(fs.getFileSystem());
        subject = new ProfileParserImpl(
            new ProfileScanConfig(
                Configuration.createFromDefault()
                    .put(Configuration.profile_dir.key(), "profiles")));
        profileDir = fs.createDirectory(fs.getPath("profiles"));
    }

    @Test
    void parseFile_ShouldIgnoreEmptyLine() throws Exception {
        Path testFile = profileDir.resolve("profile.ffmpeg");
        String option1 = " line_without_space";
        String option2 = "line with space";
        Files.write(testFile, Arrays.asList(option1, "", option2));

        var results = subject.parseFile(testFile).get().getArguments();

        assertThat(results).containsExactly("line_without_space", "line with space");
    }

    @Test
    void parseFile_ShouldIgnoreFieldLines() throws Exception {
        var testFile = profileDir.resolve("profile.ffmpeg");
        var option1 = " %{FIELD=value}";
        var option2 = "another line";
        Files.write(testFile, Arrays.asList(option1, option2));

        var results = subject.parseFile(testFile).get().getArguments();

        assertThat(results).containsExactly("another line");
    }

    @Test
    void parseFile_ShouldParseFieldLines() throws Exception {
        var testFile = profileDir.resolve("profile.ffmpeg");
        var option1 = "%{FIELD=value}";
        var option2 = "%{key=other}";
        Files.write(testFile, Arrays.asList(option1, option2));

        var results = subject.parseFile(testFile).get().getFields();

        assertThat(results)
            .containsKeys("FIELD", "KEY")
            .containsValues("value", "other")
            .hasSize(2);
    }

    @Test
    void parseFile_ShouldReturnEmptyProfile_OnError() throws Exception {
        var testFile = profileDir.resolve("profile.ffmpeg");

        var result = subject.parseFile(testFile);

        assertThat(result).isEmpty();
    }

    @Test
    void isCommentLine_ShouldReturnTrue_IfLineBeginsWithHashtag() throws Exception {
        var testLine = "# this is a comment";
        assertThat(subject.isCommentLine(testLine)).isTrue();
    }

    @Test
    void isCommentLine_ShouldReturnTrue_IfLineIsEmpty() throws Exception {
        var testLine = "";
        assertThat(subject.isCommentLine(testLine)).isTrue();
    }

    @Test
    void isCommentLine_ShouldReturnFalse_IfLineIsValid() throws Exception {
        var testLine = "this is not a comment";
        assertThat(subject.isCommentLine(testLine)).isFalse();
    }

    @Test
    void extractKey_ShouldReturnKey_InUppercase() throws Exception {
        var testLine = "%{key=value}";
        assertThat(subject.extractKey(testLine)).isEqualTo("KEY");
    }

    @Test
    void isFieldLine_ShouldReturnTrue_IfLineIsFieldLine() throws Exception {
        var testLine = "%{KEY=value}";
        assertThat(subject.isFieldLine(testLine)).isTrue();
    }

    @Test
    void isFieldLine_ShouldReturnFalse_IfLineDoesNotBeginWithPercent() throws Exception {
        var testLine = "{KEY=value}";
        assertThat(subject.isFieldLine(testLine)).isFalse();
    }

    @Test
    void isFieldLine_ShouldReturnFalse_IfKeyIsInvalid() throws Exception {
        var testLine = "%{=value}";
        assertThat(subject.isFieldLine(testLine)).isFalse();
    }

    @Test
    void isFieldLine_ShouldReturnTrue_IfKeyIsValid_AndNoValuePresent() throws Exception {
        var testLine = "%{key=}";
        assertThat(subject.isFieldLine(testLine)).isTrue();
    }

    @Test
    void isFieldLine_ShouldReturnFalse_IfKeyIsValid_AndNoNoClosingBracket() throws Exception {
        var testLine = "%{key=";
        assertThat(subject.isFieldLine(testLine)).isFalse();
    }

    @Test
    void extractValue_ShouldReturnValue() throws Exception {
        var testLine = "%{KEY=.value}";
        assertThat(subject.extractValue(testLine)).isEqualTo(".value");
    }

    @Test
    void extractValue_ShouldReturnEmptyString_IfNoValuePresent() throws Exception {
        var testLine = "%{KEY=}";
        assertThat(subject.extractValue(testLine)).isEqualTo("");
    }
}
