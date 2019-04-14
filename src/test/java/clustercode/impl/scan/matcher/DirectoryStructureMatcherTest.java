package clustercode.impl.scan.matcher;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;
import clustercode.api.scan.ProfileParser;
import clustercode.impl.scan.ProfileScanConfig;
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

public class DirectoryStructureMatcherTest {

    private DirectoryStructureMatcher subject;
    @Mock
    private ProfileParser parser;
    @Mock
    private ProfileScanConfig config;
    @Mock
    private Media candidate;
    @Spy
    private Profile profile;
    private Path profileFolder;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        profileFolder = fs.getPath("profiles");
        when(config.profile_file_name()).thenReturn("profile");
        when(config.profile_file_name_extension()).thenReturn(".ffmpeg");
        when(config.profile_base_dir()).thenReturn(profileFolder);
        subject = new DirectoryStructureMatcher(config, parser);
    }

    @Test
    public void apply_ShouldReturnProfile_ClosestToOriginalStructure() throws Exception {
        var media = fs.createFile(fs.getPath("0", "movies", "subdir", "movie.mp4"));
        var profileFile = fs.createFile(profileFolder.resolve("0/movies/subdir/profile.ffmpeg"));

        when(candidate.getSourcePath()).thenReturn(media);
        when(parser.parseFile(profileFile)).thenReturn(Optional.of(profile));

        var result = subject.apply(candidate);

        assertThat(result).hasValue(profile);
    }

    @Test
    public void apply_ShouldReturnProfile_FromParentDirectory() throws Exception {
        var media = fs.createFile(fs.getPath("0", "movies", "subdir", "movie.mp4"));
        var profileFile = fs.createFile(profileFolder.resolve("0/movies/profile.ffmpeg"));

        when(candidate.getSourcePath()).thenReturn(media);
        when(parser.parseFile(profileFile)).thenReturn(Optional.of(profile));

        var result = subject.apply(candidate);

        assertThat(result).hasValue(profile);
    }

    @Test
    public void apply_ShouldReturnProfile_FromGrandParentDirectory() throws Exception {
        var media = fs.createFile(fs.getPath("0", "movies", "subdir", "movie.mp4"));
        var profileFile = fs.createFile(profileFolder.resolve("0/profile.ffmpeg"));

        when(candidate.getSourcePath()).thenReturn(media);
        when(parser.parseFile(profileFile)).thenReturn(Optional.of(profile));
        profile.setLocation(profileFile);

        var result = subject.apply(candidate);

        assertThat(result).hasValue(profile);
    }


    @Test
    public void apply_ShouldReturnEmptyProfile_IfNoFileMatches() throws Exception {
        var media = fs.createFile(fs.getPath("0", "movies", "subdir", "movie.mp4"));

        when(candidate.getSourcePath()).thenReturn(media);
        when(parser.parseFile(any())).thenReturn(Optional.empty());

        var result = subject.apply(candidate);

        assertThat(result).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void apply_ShouldReturnParentProfile_IfSiblingFileCouldNotBeRead() throws Exception {
        var media = fs.createFile(fs.getPath("0", "movies", "subdir", "movie.mp4"));
        fs.createFile(profileFolder.resolve("0/movies/subdir/profile.ffmpeg"));
        fs.createFile(profileFolder.resolve("0/movies/profile.ffmpeg"));

        when(candidate.getSourcePath()).thenReturn(media);
        when(parser.parseFile(any())).thenReturn(Optional.empty(), Optional.of(profile));

        var result = subject.apply(candidate);

        assertThat(result).hasValue(profile);
    }

}