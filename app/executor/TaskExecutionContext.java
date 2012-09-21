package executor;

import java.util.ArrayList;
import java.util.List;

import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import orchestration.PipeVersion;

import org.joda.time.DateTime;
import org.joda.time.ReadableDateTime;

/**
 * Encapsulates the information that the {@link TaskRunner} need to execute a task and
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

    public void startedNow() {
        this.started = new DateTime();
    }

    public void finishedNow() {
        this.finished = new DateTime();
    }

    /**
     * @return the tasks that this task triggers
     */
    public List<TaskConfig> getTriggedTasks() {
        List<TaskConfig> triggedTasks = new ArrayList<TaskConfig>();
        List<String> triggedTaskNames = getTask().getTriggersTasks();
        for (String taskName : triggedTaskNames) {
            TaskConfig task = getPhase().getTaskByName(taskName);
            triggedTasks.add(task);
        }
        return triggedTasks;
    }

    public TaskExecutionContext getFirstTaskInNextPhase() {
        PhaseConfig nextPhase = pipe.getNextPhase(phase);
        if (nextPhase == null) {
            return null;
        }
        return new TaskExecutionContext(nextPhase.getInitialTask(), getPipe(), nextPhase, getVersion());
    }

}
