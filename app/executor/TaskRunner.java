package executor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.config.TaskConfig;
import play.Logger;

class TaskRunner implements Runnable {

    private final TaskExecutionContext context;
    private final TaskCallback callback;
    private TaskResult result;

    TaskRunner(TaskExecutionContext context, TaskCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void run() {
        context.startedNow();
        callback.handleTaskStarted(context);
        execute();
        context.finishedNow();
        callback.handleTaskResult(result);
    }

    private void execute() {
        Process process = null;
        try {
            // TODO: Maybe it's better to set
            // ProcessBuilder.redirectErrorStream(true) and have only one
            // output.
            List<String> commands = getCommand();
            process = new ProcessBuilder(commands).start();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                Logger.warn("Thread for task: '" + getName()
                        + "' interrupted while waiting for process to finish.", e);
            }
        } catch (IOException e) {
            Logger.error("Failed while running task: '" + getName() + "'.", e);
        } finally {
            if (process != null) {
                result = new TaskResult(process, context);
                process.destroy();
            } else {
                result = TaskResult.getEmptyFailedResult(context);
            }
        }
    }

    public String getName() {
        return getConfig().getTaskName();
    }

    public boolean isAutomatic() {
        return getConfig().isAutomatic();
    }

    /**
     * Get the arguments for the command to execute. Splits on space and inserts
     * task parameters
     * 
     * @return
     */
    public List<String> getCommand() {
        List<String> result = new ArrayList<String>();
        String[] commands = getConfig().getCommand().split(" ");
        for (String cmd : commands) {
            if (cmd.contains("{VERSION}")) {
                Logger.error("cmd contains {VERSION}");
                cmd = cmd.replace("{VERSION}", context.getPipeVersion().getVersion());
            }
            if (cmd.contains("{COMMIT_ID}")) {
                cmd = cmd.replace("{COMMIT_ID}",
                        context.getPipeVersion().getVersionControlInfo().versionControlId);
            }
            result.add(cmd);
        }
        return result;
    }

    private TaskConfig getConfig() {
        return context.getTask();
    }

}
