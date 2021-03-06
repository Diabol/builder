package models.message;

import models.PipeVersion;
import models.config.PhaseConfig;

import org.joda.time.ReadableDateTime;

import executor.TaskExecutionContext;

/**
 * Use the factory methods.
 * 
 * @author marcus
 */
public class PhaseStatus extends PipeStatus {

    private final PhaseConfig phase;

    public static PhaseStatus newRunningPhaseStatus(TaskExecutionContext context) {
        return new PhaseStatus(context.getPipeVersion(), context.getPhase(), State.RUNNING,
                context.getStarted(), null);
    }

    public static PhaseStatus newFinishedPhaseStatus(TaskExecutionContext context, boolean success) {
        return new PhaseStatus(context.getPipeVersion(), context.getPhase(), State.state(success),
                null, context.getFinished());
    }

    /** Use the factory methods */
    PhaseStatus(PipeVersion version, PhaseConfig phase, State state, ReadableDateTime started,
            ReadableDateTime finished) {
        super(version, state, started, finished);
        this.phase = phase;
    }

    public String getPhaseName() {
        return phase.getName();
    }

}
