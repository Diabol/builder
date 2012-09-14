package executor;

import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import orchestration.PipeVersion;

import org.joda.time.DateTime;
import org.joda.time.ReadableDateTime;

/**
 * Encapsulates the information that the {@link Task} need to execute a task and
 * the task result receivers need to handle the result.
 * 
 * @author marcus
 */
public class TaskExecutionContext {

    private final TaskConfig task;
    private final PhaseConfig phase;
    private final PipeConfig pipe;
    private final PipeVersion<?> version;
    private ReadableDateTime started;
    private ReadableDateTime finished;

    public TaskExecutionContext(TaskConfig task, PipeConfig pipe, PhaseConfig phase, PipeVersion<?> version) {
        this.task = task;
        this.pipe = pipe;
        this.phase = phase;
        this.version = version;
    }

    public TaskConfig getTask() {
        return task;
    }

    public PhaseConfig getPhase() {
        return phase;
    }

    public PipeConfig getPipe() {
        return pipe;
    }

    public PipeVersion<?> getVersion() {
        return version;
    }

    public ReadableDateTime getStarted() {
        return started;
    }

    public ReadableDateTime getFinished() {
        return finished;
    }

    void startedNow() {
        this.started = new DateTime();
    }

    void finishedNow() {
        this.finished = new DateTime();
    }

}
