package models.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author danielgronberg, marcus
 */
public class TaskConfig {

    private String taskName;
    private String cmd;
    private boolean isAutomatic = true;
    private List<String> triggersTasks = new ArrayList<String>();

    public TaskConfig(String taskName, String cmd, boolean isAutomatic) {
        super();
        this.taskName = taskName;
        this.cmd = cmd;
        this.isAutomatic = isAutomatic;
    }

    public TaskConfig() {

    }

    public String getTaskName() {
        return taskName;
    }

    public void setName(String taskName) {
        this.taskName = taskName;
    }

    public Boolean isAutomatic() {
        return isAutomatic;
    }

    public void setIsAutomatic(Boolean isAutomatic) {
        this.isAutomatic = isAutomatic;
    }

    public String getCommand() {
        return cmd;
    }

    public void setCommand(String cmd) {
        this.cmd = cmd;
    }

    public List<String> getTriggersTasks() {
        return triggersTasks;
    }

    public void setTriggersTasks(List<String> triggersTasks) {
        this.triggersTasks = triggersTasks;
    }

    public void validate() throws PipeValidationException {
        if (getTaskName() == null || getCommand() == null || getTaskName().isEmpty()
                || getCommand().isEmpty()) {
            throw new PipeValidationException("Invalid: " + this);
        }
    }

    @Override
    public String toString() {
        final int maxLen = 3;
        StringBuilder builder = new StringBuilder();
        builder.append("TaskConfig [taskName=");
        builder.append(taskName);
        builder.append(", cmd=");
        builder.append(cmd);
        builder.append(", isAutomatic=");
        builder.append(isAutomatic);
        builder.append(", triggersTasks=");
        builder.append(triggersTasks != null ? triggersTasks.subList(0,
                Math.min(triggersTasks.size(), maxLen)) : null);
        builder.append("]");
        return builder.toString();
    }

}
