package clustercode.impl.transcode;

import clustercode.api.domain.TranscodeTask;
import clustercode.api.event.RxEventBus;
import clustercode.api.transcode.TranscodingService;
import clustercode.scheduling.messages.ProfileSelectedMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TranscodingMessageHandler {

    private final TranscodingService transcodingService;
    private final RxEventBus eventBus;

    TranscodingMessageHandler(TranscodingService transcodingService,
                              RxEventBus eventBus) {

        this.transcodingService = transcodingService;
        this.eventBus = eventBus;
    }


    public void onProfileSelected(ProfileSelectedMessage msg) {
        TranscodeTask task = TranscodeTask
            .builder()
            .profile(msg.getProfile())
            .media(msg.getMedia())
            .build();
        transcodingService.transcode(task);
    }
}
