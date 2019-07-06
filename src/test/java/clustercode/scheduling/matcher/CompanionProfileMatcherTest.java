package clustercode.scheduling.matcher;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.scheduling.ProfileParser;
import clustercode.scheduling.ProfileScanConfig;
import clustercode.test.util.FileBasedUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompanionProfileMatcherTest {

    @Mock
    private ProfileScanConfig config;
    @Mock
    private ProfileParser profileParser;
    private Media candidate;
    @Spy
    private Profile profile;

    private CompanionProfileMatcher subject;
    private FileBasedUnitTest fs = new FileBasedUnitTest();

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new CompanionProfileMatcher(config, profileParser);
        candidate = new Media(0, fs.getPath("movie.mp4"));
        when(config.profile_file_name_extension()).thenReturn(".ffmpeg");
        when(config.profile_base_dir()).thenReturn(fs.getPath("profiles"));
        when(profileParser.parseFile(any())).thenReturn(Optional.of(profile));
    }

    @Test
    public void apply_ShouldReturnProfile_IfProfileFoundAndReadable() throws Exception {
        var profilePath = fs.createFile(fs.getPath("profiles", "0", "movie.mp4.ffmpeg"));
        when(profileParser.parseFile(profilePath)).thenReturn(Optional.of(profile));

        assertThat(subject.apply(candidate)).hasValue(profile);
    }

    @Test
    public void apply_ShouldReturnEmpty_IfProfileNotFound() throws Exception {
        assertThat(subject.apply(candidate)).isEmpty();
    }

    @Test
    public void apply_ShouldReturnEmpty_IfProfileNotReadable() throws Exception {
        var profilePath = fs.createFile(fs.getPath("profiles", "0", "movie.mp4.ffmpeg"));

        when(profileParser.parseFile(profilePath)).thenReturn(Optional.empty());

        assertThat(subject.apply(candidate)).isEmpty();
        verify(profileParser).parseFile(profilePath);
    }

}
