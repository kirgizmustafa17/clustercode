package clustercode.scan.matcher;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.scan.ProfileParser;
import clustercode.scan.ProfileScanConfig;
import clustercode.test.util.FileBasedUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class CompanionProfileMatcherTest {

    @Mock
    private ProfileScanConfig config;
    @Mock
    private ProfileParser profileParser;
    @Mock
    private Media candidate;
    @Spy
    private Profile profile;

    private CompanionProfileMatcher subject;
    private FileBasedUnitTest fs = new FileBasedUnitTest();

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new CompanionProfileMatcher(config, profileParser);
        when(config.profile_file_name_extension()).thenReturn(".ffmpeg");
        when(profileParser.parseFile(any())).thenReturn(Optional.of(profile));
    }

    @Test
    public void apply_ShouldReturnProfile_IfFileFoundAndReadable() throws Exception {
        Path media = fs.createParentDirOf(fs.getPath("input", "movie.mp4"));
        fs.createFile(fs.getPath("input", "movie.mp4.ffmpeg"));

        when(candidate.getRelativePath()).thenReturn(Optional.of(media));

        Profile result = subject.apply(candidate).get();

        assertThat(result).isEqualTo(profile);
    }

    @Test
    public void apply_ShouldReturnEmpty_IfFileNotFound() throws Exception {
        Path media = fs.createParentDirOf(fs.getPath("input", "movie.mp4"));

        when(candidate.getRelativePath()).thenReturn(Optional.of(media));

        assertThat(subject.apply(candidate)).isEmpty();
    }

    @Test
    public void apply_ShouldReturnEmpty_IfFileNotReadable() throws Exception {
        Path media = fs.createParentDirOf(fs.getPath("input", "movie.mkv"));
        Path file = fs.createFile(fs.getPath("input", "movie.mkv.ffmpeg"));

        when(candidate.getRelativePath()).thenReturn(Optional.of(media));

        when(profileParser.parseFile(file)).thenReturn(Optional.empty());

        assertThat(subject.apply(candidate)).isEmpty();
    }

}
