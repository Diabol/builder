package models.message;

import models.config.PhaseConfig;
import orchestration.PipeVersion;

import org.joda.time.ReadableDateTime;

import executor.TaskExecutionContext;

/**
 * Use the factory methods.
 * 
 * @author marcus
 */
public class PhaseStatus extends StatusMessage {

    public static PhaseStatus newRunningPhaseStatus(TaskExecutionContext context) {
        return new PhaseStatus(Status.RUNNING, context.getVersion(), context.getPhase(), context.getStarted(), null);
    }

    public static PhaseStatus newFinishedPhaseStatus(TaskExecutionContext context, boolean success) {
        return new PhaseStatus(Status.status(success), context.getVersion(), context.getPhase(), context.getStarted(),
                context.getFinished());
    }

    /** Use the factory methods */
    private PhaseStatus(Status status, PipeVersion<?> version, PhaseConfig phase, ReadableDateTime started,
            ReadableDateTime finished) {
        super(status, version, phase, started, finished);
    }

}
