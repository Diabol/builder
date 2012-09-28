package utils;

import java.util.List;

import models.PipeVersion;
import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import models.message.PhaseStatus;
import models.message.TaskStatus;
import models.statusdata.Phase;
import models.statusdata.Pipe;
import models.statusdata.Task;
import play.db.ebean.Model.Finder;
import executor.TaskExecutionContext;
import executor.TaskResult;

public class DBHelper {

    private static DBHelper instance;
    private Finder<Long, Pipe> pipeFind = Pipe.find;
    private Finder<Long, Phase> phaseFind = Phase.find;
    private Finder<Long, Task> taskFind = Task.find;

    static {
        instance = new DBHelper();
    }

    private DBHelper() {

    }

    public static DBHelper getInstance() {
        return instance;
    }

    /**
     * For test
     * 
     * @param pipeFinder
     */
    public void setPipeFinder(Finder<Long, Pipe> pipeFinder) {
        this.pipeFind = pipeFinder;
    }

    /**
     * For test
     * 
     * @param pipeFind
     */
    public void setPhaseFinder(Finder<Long, Phase> phaseFinder) {
        this.phaseFind = phaseFinder;
    }

    /**
     * For test
     * 
     * @param pipeFind
     */
    public void setTaskFinder(Finder<Long, Task> taskFinder) {
        this.taskFind = taskFinder;
    }

    public synchronized void persistNewPipe(PipeVersion version, PipeConfig pipe) {
        // Create the pipe
        Pipe pipeData = Pipe.createNewFromConfig(version.getVersion(), pipe);
        pipeData.save();
        // Create the phases belonging to the pipe
        for (PhaseConfig phaseConf : pipe.getPhases()) {
            Phase phase = Phase.createNewFromConfig(phaseConf);
            phase.pipe = pipeData;
            phase.save();
            // Create tasks for each phase
            for (TaskConfig taskConf : phaseConf.getTasks()) {
                Task task = Task.createNewFromConfig(taskConf);
                task.phase = phase;
                task.save();
            }
        }
    }

    public synchronized void updatePhaseToOngoing(PhaseStatus phaseStatus) {
        Phase phase = getPhase(phaseStatus.getPipeName(), phaseStatus.getVersion(),
                phaseStatus.getPhaseName());
        if (phase != null) {
            phase.startNow();
            phase.update();
        }
    }

    public synchronized void updatePhaseToFinished(TaskExecutionContext context, boolean success) {
        Phase phase = getPhase(context.getPipe().getName(), context.getPipeVersion().getVersion(),
                context.getPhase().getName());
        if (phase != null) {
            phase.finishNow(success);
            phase.update();
        }
    }

    public synchronized void updateTaskToOngoing(TaskStatus taskStatus) {
        Task task = getTask(taskStatus.getPipeName(), taskStatus.getVersion(),
                taskStatus.getPhaseName(), taskStatus.getTaskName());
        if (task != null) {
            task.startNow();
            task.update();
        }
    }

    public synchronized void updateTaskToFinished(TaskResult taskResult) {
        TaskExecutionContext taskContext = taskResult.context();
        Task task = getTask(taskContext.getPipe().getName(), taskContext.getPipeVersion()
                .getVersion(), taskContext.getPhase().getName(), taskContext.getTask()
                .getTaskName());
        if (task != null) {
            task.finishNow(taskResult.success());
            task.update();
        }
    }

    private Phase getPhase(String pipeName, String version, String phaseName) {
        List<Phase> foundPhases = phaseFind.where().eq("name", phaseName).eq("pipe.name", pipeName)
                .eq("pipe.version", version).findList();
        if (foundPhases.size() != 1) {
            throw new DataInconsistencyException("Found " + foundPhases.size()
                    + " instances of phase with name " + phaseName + " , pipe " + pipeName
                    + " and version " + version
                    + " when updating the phase status. Expected to find 1 match.");
        } else {
            Phase phase = foundPhases.get(0);
            return phase;
        }
    }

    private Task getTask(String pipeName, String version, String phaseName, String taskName) {
        List<Task> foundTasks = taskFind.where().eq("name", taskName).eq("phase.name", phaseName)
                .eq("phase.pipe.name", pipeName).eq("phase.pipe.version", version).findList();
        if (foundTasks.size() != 1) {
            throw new DataInconsistencyException("Found " + foundTasks.size()
                    + " instances of task with name " + taskName + ", phase " + phaseName
                    + " , pipe " + pipeName + " and version " + version
                    + " when updating the task status. Expected to find 1 match.");
        } else {
            Task task = foundTasks.get(0);
            return task;
        }
    }

    public Pipe getPipe(PipeVersion version) throws DataNotFoundException {
        List<Pipe> foundPipes = pipeFind.where().eq("name", version.getPipeName())
                .eq("version", version.getVersion()).findList();
        if (foundPipes.size() == 0) {
            throw new DataNotFoundException("No data found for pipe '" + version.getPipeName()
                    + "' with version " + version.getVersion());
        }
        if (foundPipes.size() > 1) {
            throw new DataInconsistencyException("Found " + foundPipes.size()
                    + " instances of pipe with name " + version.getPipeName() + " and version "
                    + version.getVersion() + ". Should only be 1 match.");
        } else {
            return foundPipes.get(0);
        }
    }

    public Pipe getLatestPipe(PipeConfig pipe) throws DataNotFoundException {
        List<Pipe> foundPipes = pipeFind.all();
        if (foundPipes.size() == 0) {
            throw new DataNotFoundException("No persisted pipes found for '" + pipe.getName() + "'");
        } else {
            return foundPipes.get(foundPipes.size() - 1);
        }
    }

    public Phase getPhaseForContext(TaskExecutionContext context) {
        return getPhase(context.getPipe().getName(), context.getPipeVersion().getVersion(), context
                .getPhase().getName());
    }

    public synchronized void updatePipeToOnging(TaskExecutionContext context) {
        try {
            Pipe pipe = getPipe(context.getPipeVersion());
            pipe.startNow();
            pipe.save();
        } catch (DataNotFoundException ex) {
            throw new DataInconsistencyException("Unable to update pipe with name "
                    + context.getPipe().getName() + " and version "
                    + context.getPipeVersion().getVersion() + " to RUNNING. Reason:"
                    + ex.getMessage());
        }

    }

}
