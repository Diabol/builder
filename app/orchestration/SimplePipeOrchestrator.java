package orchestration;

import java.util.List;

import models.PipeVersion;
import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import models.message.PhaseStatus;
import models.message.TaskStatus;
import models.statusdata.Pipe;
import notification.PipeNotificationHandler;
import play.Logger;
import utils.DBHelper;
import executor.TaskExecutionContext;
import executor.TaskResult;

/**
 * Autor: Daniel Grönberg
 */
public class SimplePipeOrchestrator implements Runnable {
    private final PipeConfig pipe;
    private final String version;

    public SimplePipeOrchestrator(PipeConfig pipe, String version) {
        this.pipe = pipe;
        this.version = version;
    }

    @Override
    public void run() {
        DBHelper.getInstance().persistNewPipe(PipeVersion.fromString(version, pipe), pipe);
        for (PhaseConfig phase : pipe.getPhases()) {
            TaskExecutionContext context = new TaskExecutionContext(null, pipe, phase,
                    PipeVersion.fromString(version, pipe));
            context.startedNow();
            PhaseStatus ongoingPhase = PhaseStatus.newRunningPhaseStatus(context);
            PipeNotificationHandler.getInstance().notifyPhaseStatusListeners(ongoingPhase);
            for (TaskConfig task : phase.getTasks()) {
                TaskExecutionContext taskContext = new TaskExecutionContext(task, pipe, phase,
                        PipeVersion.fromString(version, pipe));
                taskContext.startedNow();
                TaskStatus ongoing = TaskStatus.newRunningTaskStatus(taskContext);

                DBHelper.getInstance().updateTaskToOngoing(ongoing);
                PipeNotificationHandler.getInstance().notifyTaskStatusListeners(ongoing);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                taskContext.finishedNow();
                TaskResult result = new TaskResult(true, taskContext);
                TaskStatus taskStatus = TaskStatus.newFinishedTaskStatus(result);
                DBHelper.getInstance().updateTaskToFinished(taskStatus);
                PipeNotificationHandler.getInstance().notifyTaskStatusListeners(taskStatus);
            }
            context.finishedNow();
            PhaseStatus status = PhaseStatus.newFinishedPhaseStatus(context, true);
            PipeNotificationHandler.getInstance().notifyPhaseStatusListeners(status);
        }
        printPipes();
    }

    private void printPipes() {
        List<Pipe> savedPipes = Pipe.find.all();
        for (Pipe pipe : savedPipes) {
            Logger.error(pipe.toString());
        }
    }
}
