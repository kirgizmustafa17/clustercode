package net.chrigel.clustercode.statemachine.actions;

import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.statemachine.Action;
import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;
import net.chrigel.clustercode.transcode.TranscodeTask;
import net.chrigel.clustercode.transcode.TranscodingService;

import javax.inject.Inject;

public class TranscodeAction extends Action {

    private final TranscodingService transcodingService;
    private ClusterService clusterService;

    @Inject
    TranscodeAction(TranscodingService transcodingService,
                    ClusterService clusterService) {
        this.transcodingService = transcodingService;
        this.clusterService = clusterService;
    }

    @Override
    protected StateEvent doExecute(State from, State to, StateEvent event, StateContext context) {
        log.entry(from, to, event, context);
        context.setTranscodeResult(transcodingService.transcode(TranscodeTask.builder()
                .media(context.getSelectedMedia())
                .profile(context.getSelectedProfile())
                .build()));
        clusterService.removeTask();
        return StateEvent.FINISHED;
    }
}
