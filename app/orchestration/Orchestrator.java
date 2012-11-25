package orchestration;

import java.util.List;

import models.PipeVersion;
import models.StatusInterface.State;
import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import models.message.PhaseStatus;
import models.message.TaskStatus;
import models.statusdata.Pipe;
import models.statusdata.Task;
import models.statusdata.VersionControlInfo;
import notification.PipeNotificationHandler;
import play.Logger;
import utils.DBHelper;
import utils.DataInconsistencyException;
import utils.DataNotFoundException;
import utils.LogHandler;
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

    private final PipeConfReader configReader;
    private final DBHelper dbHelper;
    private final PipeNotificationHandler notifictionHandler;
    private final TaskExecutor taskExecutor;
    private final LogHandler logHandler;

    public Orchestrator(PipeConfReader configReader, DBHelper dbHelper,
            PipeNotificationHandler notifictionHandler, TaskExecutor taskExecutor,
            LogHandler logHandler) {
        super();
        this.configReader = configReader;
        this.dbHelper = dbHelper;
        this.notifictionHandler = notifictionHandler;
        this.taskExecutor = taskExecutor;
        this.logHandler = logHandler;
    }

    /** Start first task of first phase of pipe */
    public PipeVersion start(String pipeName, VersionControlInfo vcInfo)
            throws DataNotFoundException {
        PipeConfig pipe = getPipe(pipeName);
        PhaseConfig phase = pipe.getFirstPhaseConfig();
        TaskConfig task = phase.getInitialTask();

        PipeVersion version = getNextPipeVersion(pipe, vcInfo);
        dbHelper.persistNewPipe(version, pipe);
        notifictionHandler.notifyNewVersionOfPipe(version);
        startTask(task, phase, pipe, version);

        return version;
    }

    /** Try to start given task in given phase and pipe version. */
    public void startTask(String taskName, String phaseName, String pipeName, String pipeVersion)
            throws DataNotFoundException {
        PipeConfig pipe = getPipe(pipeName);
        PhaseConfig phase = pipe.getPhaseByName(phaseName);
        TaskConfig task = phase.getTaskByName(taskName);
        Pipe persisted = dbHelper.getPipe(pipeName, pipeVersion);
        PipeVersion version = PipeVersion.fromString(pipeVersion, persisted.versionControlInfo,
                pipe);
        startTask(task, phase, pipe, version);
    }

    private PipeVersion getNextPipeVersion(PipeConfig pipe, VersionControlInfo vcInfo) {
        try {
            Pipe latestPipe = dbHelper.getLatestPipe(pipe);
            String version = latestPipe.version;
            String major = version.split("\\.")[0];
            String minor = version.split("\\.")[1];
            long latest = Long.valueOf(minor);
            return PipeVersion.fromString(major + "." + ++latest, vcInfo, pipe);
        } catch (DataNotFoundException ex) {
            return PipeVersion.fromString("1.1", vcInfo, pipe);
        }
    }

    private void startTask(TaskConfig task, PhaseConfig phase, PipeConfig pipe, PipeVersion version) {
        TaskExecutionContext executionContext = new TaskExecutionContext(task, pipe, phase, version);
        startTask(executionContext);
    }

    private void startTask(TaskExecutionContext executionContext) {
        taskExecutor.execute(executionContext, this);
    }

    @Override
    public synchronized void handleTaskStarted(TaskExecutionContext context) {
        TaskStatus taskStatus = TaskStatus.newRunningTaskStatus(context);
        if (isFirstTaskJustStarted(context, taskStatus)) {
            PhaseStatus phaseStatus = PhaseStatus.newRunningPhaseStatus(context);
            dbHelper.updatePhaseToOngoing(phaseStatus);
            notifictionHandler.notifyPhaseStatusListeners(phaseStatus);
            if (isContextForFirstTaskOfPipe(context)) {
                dbHelper.updatePipeToOnging(context.getPipeVersion());
            }
        }
        dbHelper.updateTaskToOngoing(taskStatus);
        notifictionHandler.notifyTaskStatusListeners(taskStatus);

    }

    @Override
    public synchronized void handleTaskResult(TaskResult result) {
        TaskStatus taskStatus = TaskStatus.newFinishedTaskStatus(result);
        dbHelper.updateTaskToFinished(taskStatus);
        String logKey = result.context().getTask().getTaskName()
                + result.context().getPhase().getName() + result.context().getPipe().getName()
                + result.context().getPipeVersion().getVersion();
        logHandler.storeLog(logKey, result.out());
        if (!result.success()) {
            Logger.info("Task '" + result.context().getTask().getTaskName()
                    + "' failed. PipeVersion: " + result.context().getPipeVersion().toString()
                    + ". Error log: " + result.out());
            Logger.info("----End of task log----");
        }
        notifictionHandler.notifyTaskStatusListeners(taskStatus);
        // Check it the phase of the task should be updated with new state. Only
        // applicable for blocking tasks.
        if (result.context().getTask().isBlocking()) {
            PhaseStatus phaseStatus = getNewFinishedPhaseStatus(result);
            if (phaseStatus != null) {
                // Update phase state
                dbHelper.updatePhaseToFinished(phaseStatus);
                notifictionHandler.notifyPhaseStatusListeners(phaseStatus);
                // Update pipe state if phase has changed to failed or it has
                // changed to success AND it is the last phase in the pipe.
                if (!phaseStatus.isSuccess()
                        || (isContextForLastPhase(result.context()) && phaseStatus.isSuccess())) {
                    dbHelper.updatePipeToFinished(result.context().getPipeVersion(),
                            phaseStatus.isSuccess());
                }
            }
            if (result.success()) {
                // Start next task only if the pipe is still running.
                try {
                    Pipe currentPipe = dbHelper.getPipe(result.context().getPipe().getName(),
                            result.context().getPipeVersion().getVersion());
                    if (currentPipe.state == State.RUNNING) {
                        startNextTask(result);
                    }
                } catch (DataNotFoundException ex) {
                    Logger.error("Got a result for task " + taskStatus.getTaskName()
                            + ". But no Pipe found for " + taskStatus.getPipeName()
                            + " with version " + taskStatus.getVersion());
                    throw new DataInconsistencyException(ex.getMessage());
                }
            }
        }

    }

    /**
     * Check to see if the context is for the last phase of a pipe.
     * 
     * @param context
     * @return
     */
    private boolean isContextForLastPhase(TaskExecutionContext context) {
        PhaseConfig phase = context.getPhase();
        PhaseConfig nextPhase = context.getPipe().getNextPhase(phase);
        return nextPhase == null;
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
                startTask(task, taskContext.getPhase(), taskContext.getPipe(),
                        taskContext.getPipeVersion());
            } else {
                TaskExecutionContext context = new TaskExecutionContext(task, lastTaskResult
                        .context().getPipe(), lastTaskResult.context().getPhase(), lastTaskResult
                        .context().getPipeVersion());
                updateAndNotifyTaskPending(context);
            }
        }
        // 3. Trigger first task in next phase if all tasks in this phase
        // finished and auto.
        if (allBlockingTasksInPhaseFinishedSuccessfully(taskContext)) {
            TaskExecutionContext newTaskContext = taskContext.getFirstTaskInNextPhase();
            if (newTaskContext != null) {
                if (newTaskContext.getTask().isAutomatic()) {
                    startTask(newTaskContext);
                } else {
                    updateAndNotifyTaskPending(newTaskContext);
                }
            }
        }
    }

    private void updateAndNotifyTaskPending(TaskExecutionContext context) {
        TaskStatus pending = TaskStatus.newPendingTaskStatus(context);
        dbHelper.updateTaskToPending(pending);
        notifictionHandler.notifyTaskStatusListeners(pending);
    }

    private boolean allBlockingTasksInPhaseFinishedSuccessfully(TaskExecutionContext taskContext) {

        List<Task> tasksInPhaseForContext = dbHelper.getPhaseForContext(taskContext).tasks;
        for (int i = 0; i < tasksInPhaseForContext.size(); i++) {
            if (tasksInPhaseForContext.get(i).state != State.SUCCESS
                    && taskContext.getPhase().getTasks().get(i).isBlocking()) {
                return false;
            }
        }
        return true;
    }

    private boolean isFirstTaskJustStarted(TaskExecutionContext context, TaskStatus taskStatus) {
        TaskConfig currentTask = context.getTask();
        boolean firstTaskJustStarted = currentTask.equals(context.getPhase().getInitialTask())
                && taskStatus.isRunning();
        return firstTaskJustStarted;
    }

    private boolean isContextForFirstTaskOfPipe(TaskExecutionContext context) {
        TaskConfig task = context.getTask();
        return task == context.getPipe().getPhases().get(0).getInitialTask();
    }

    /**
     * @return new finsihed {@link PhaseStatus}, success or fail, null if no
     *         status change.
     */
    private PhaseStatus getNewFinishedPhaseStatus(TaskResult latestTaskResult) {
        if (!latestTaskResult.success()
                || allBlockingTasksInPhaseFinishedSuccessfully(latestTaskResult.context())) {
            return PhaseStatus.newFinishedPhaseStatus(latestTaskResult.context(),
                    latestTaskResult.success());
        } else {
            return null;
        }

    }

    private PipeConfig getPipe(String pipeName) throws DataNotFoundException {
        return configReader.get(pipeName);
    }

    public PipeVersion incrementMajor(String pipeName) throws DataNotFoundException {
        PipeConfig pipe = getPipe(pipeName);
        PhaseConfig phase = pipe.getFirstPhaseConfig();
        TaskConfig task = phase.getInitialTask();

        PipeVersion version = createNewMajorVersion(pipe);
        dbHelper.persistNewPipe(version, pipe);
        notifictionHandler.notifyNewVersionOfPipe(version);
        startTask(task, phase, pipe, version);
        return version;
    }

    private PipeVersion createNewMajorVersion(PipeConfig pipe) {
        try {
            Pipe latestPipe = dbHelper.getLatestPipe(pipe);
            VersionControlInfo vcInfo = latestPipe.versionControlInfo;
            String version = latestPipe.version;
            String major = version.split("\\.")[0];
            long latest = Long.valueOf(major);
            return PipeVersion.fromString(++latest + "." + 1, vcInfo, pipe);
        } catch (DataNotFoundException ex) {
            return PipeVersion.fromString("2.1", VersionControlInfo.createVCInfoNotAvailable(),
                    pipe);
        }
    }
}
