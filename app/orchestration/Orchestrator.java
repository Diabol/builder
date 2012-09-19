package orchestration;

import java.util.List;

import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import models.message.PhaseStatus;
import models.message.TaskStatus;
import notification.PipeNotificationHandler;
import utils.PipeConfReader;
import executor.TaskCallback;
import executor.TaskExecutionContext;
import executor.TaskExecutor;
import executor.TaskResult;

/**
 * Service/Controller that orchestrates the pipe execution.
 * 
 * <ul>
 * <li>Uses the Executor to execute tasks.</li>
 * <li>Manages/creates pipe versions</li>
 * <li>Starts tasks triggered by finished tasks.</li>
 * <li>Notifies the client of status changes.</li>
 * <li>Notifies persistence manager of status changes.</li>
 * </ul>
 * 
 * @author marcus
 */
public class Orchestrator implements TaskCallback {

    private static final PipeConfReader configReader = PipeConfReader.getInstance();

    /** Start first task of first phase of pipe */
    public PipeVersion<?> start(String pipeName) {
        PipeConfig pipe = getPipe(pipeName);
        PhaseConfig phase = pipe.getFirstPhaseConfig();
        TaskConfig task = phase.getInitialTask();

        PipeVersion<?> version = getNextPipeVersion(pipe);

        startTask(task, phase, pipe, version);

        return version;
    }

    /** Try to start given task in given phase and pipe version. */
    public void startTask(String taskName, String phaseName, String pipeName, String pipeVersion) {
        PipeConfig pipe = getPipe(pipeName);
        PhaseConfig phase = pipe.getPhaseByName(phaseName);
        TaskConfig task = phase.getTaskByName(taskName);
        PipeVersion<?> version = createPipeVersion(pipe, pipeVersion);
        startTask(task, phase, pipe, version);
    }

    private PipeVersion<?> getNextPipeVersion(PipeConfig pipe) {
        // TODO Implement getNextPipeVersion. We need to check persistence...
        return new PipeStringVersion("0.1.2", pipe);
    }

    private PipeVersion<?> createPipeVersion(PipeConfig pipe, String pipeVersion) throws PipeVersionValidationException {
        // TODO: Here we could look up the version implementation we would like to use from config...
        return new PipeStringVersion(pipeVersion, pipe);
    }

    private void startTask(TaskConfig task, PhaseConfig phase, PipeConfig pipe, PipeVersion<?> version) {
        TaskExecutionContext executionContext = new TaskExecutionContext(task, pipe, phase, version);
        startTask(executionContext);
    }

    private void startTask(TaskExecutionContext executionContext) {
        // TODO: Persist new state
        TaskExecutor.getInstance().execute(executionContext, this);
    }

    @Override
    public void handleTaskStarted(TaskExecutionContext context) {
        TaskStatus taskStatus = TaskStatus.newRunningTaskStatus(context);
        PipeNotificationHandler handler = getPipeNotificationHandler();
        handler.notifyTaskStatusListeners(taskStatus);

        if (isNewPhaseStatus(context, taskStatus)) {
            PhaseStatus phaseStatus = PhaseStatus.newRunningPhaseStatus(context);
            handler.notifyPhaseStatusListeners(phaseStatus);
        }
    }

    @Override
    public void handleTaskResult(TaskResult result) {
        TaskStatus taskStatus = TaskStatus.newFinishedTaskStatus(result);
        PipeNotificationHandler handler = getPipeNotificationHandler();
        handler.notifyTaskStatusListeners(taskStatus);

        PhaseStatus phaseStatus = getNewPhaseStatus(result);
        if (phaseStatus != null) {
            handler.notifyPhaseStatusListeners(phaseStatus);
        }

        startNextTask(result);
    }

    /**
     * Decide if we should start a new task and then start it.
     */
    private void startNextTask(TaskResult lastTaskResult) {
        // 1. Check if task was successful
        if (!lastTaskResult.success()) {
            return;
        }
        // 2. Start triggered tasks if automatic
        TaskExecutionContext taskContext = lastTaskResult.context();
        List<TaskConfig> triggeredTasks = taskContext.getTriggedTasks();
        for (TaskConfig task : triggeredTasks) {
            if (task.isAutomatic()) {
                startTask(task, taskContext.getPhase(), taskContext.getPipe(), taskContext.getVersion());
            }
        }
        // 3. Trigger first task in next phase if all tasks in this phase finished and auto.
        if(allTasksInPhaseFinishedSuccessfully(taskContext)) {
            TaskExecutionContext newTaskContext = taskContext.getFirstTaskInNextPhase();
            if (newTaskContext != null) {
                if (newTaskContext.getTask().isAutomatic()) {
                    startTask(newTaskContext);
                }
            }
        }
    }

    private boolean allTasksInPhaseFinishedSuccessfully(TaskExecutionContext taskContext) {
        // TODO We need to call persistence to know this
        return false;
    }

    private boolean isNewPhaseStatus(TaskExecutionContext context, TaskStatus taskStatus) {
        TaskConfig currentTask = context.getTask();
        boolean firstTaskJustStarted = currentTask.equals(context.getPhase().getInitialTask()) && taskStatus.isRunning();
        boolean taskSuccceeded = taskStatus.isSuccess();
        return firstTaskJustStarted || !taskSuccceeded || allTasksInPhaseFinishedSuccessfully(context);
    }

    /**
     * @return new finsihed {@link PhaseStatus}, success or fail, null if no
     *         status change.
     */
    private PhaseStatus getNewPhaseStatus(TaskResult latestTaskResult) {
        // TODO Implement. See isNewPhaseStatus(context) above
        boolean success = true;
        return PhaseStatus.newFinishedPhaseStatus(latestTaskResult.context(), success);
    }

    private PipeNotificationHandler getPipeNotificationHandler() {
        return PipeNotificationHandler.getInstance();
    }

    private PipeConfig getPipe(String pipeName) {
        return configReader.get(pipeName);
    }
}
