package utils;

import java.util.List;

import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import models.message.TaskStatus;
import models.statusdata.Phase;
import models.statusdata.Pipe;
import models.statusdata.Task;
import orchestration.PipeVersion;
import play.Logger;
import executor.TaskExecutionContext;
import executor.TaskResult;

public class DBHelper {
    public static synchronized void persistNewPipe(PipeVersion<String> version, PipeConfig pipe) {
        // Create the pipe
        Pipe pipeData = Pipe.createNewFromConfig(version.getVersion(), pipe);
        pipeData.save();
        // Create the phases belonging to the pipes
        for (PhaseConfig phaseConf : pipe.getPhases()) {
            // Create phase
            Phase phase = Phase.createNewFromConfig(phaseConf);
            phase.pipe = pipeData;
            phase.save();
            for (TaskConfig taskConf : phaseConf.getTasks()) {
                Task task = Task.createNewFromConfig(taskConf);
                task.phase = phase;
                task.save();
            }
        }
    }

    public static synchronized void updateTaskToOngoing(TaskStatus taskStatus) {
        Task task = findTask(taskStatus.getPipeName(), taskStatus.getVersion(),
                taskStatus.getPhaseName(), taskStatus.getTaskName());
        if (task != null) {
            task.startNow();
            task.update();
        }
    }

    public static synchronized void updateTaskToFinished(TaskResult taskResult) {
        TaskExecutionContext taskContext = taskResult.context();
        Task task = findTask(taskContext.getPipe().getName(), (String) taskContext.getVersion()
                .getVersion(), taskContext.getPhase().getName(), taskContext.getTask()
                .getTaskName());
        if (task != null) {
            task.finishNow(taskResult.success());
            task.update();
        }
    }

    private static Task findTask(String pipeName, String version, String phaseName, String taskName) {
        List<Task> foundTasks = Task.find.where().eq("name", taskName).eq("phase.name", phaseName)
                .eq("phase.pipe.name", pipeName).eq("phase.pipe.version", version).findList();
        if (foundTasks.size() != 1) {
            Logger.error("Found "
                    + foundTasks.size()
                    + " instances of task with name "
                    + taskName
                    + ", phase "
                    + phaseName
                    + " , pipe "
                    + pipeName
                    + " and version "
                    + version
                    + " When updating the task status. Should only be 1 match. Ignoring the update.");
            return null;
        } else {
            Task task = foundTasks.get(0);
            return task;
        }
    }
}
