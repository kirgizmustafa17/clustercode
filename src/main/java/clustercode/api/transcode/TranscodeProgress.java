package clustercode.api.transcode;

import clustercode.api.domain.Media;
import clustercode.api.domain.Profile;

public interface TranscodeProgress {

    Media getMedia();

    Profile getProfile();

}
