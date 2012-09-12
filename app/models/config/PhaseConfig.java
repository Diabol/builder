package models.config;

import java.util.List;

/**
 * @author danielgronberg, marcus
 */
public class PhaseConfig {
    private String name;
    private List<TaskConfig> tasks;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TaskConfig> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskConfig> tasks) {
        this.tasks = tasks;
    }

    public TaskConfig getInitialTask() throws PipeValidationException {
        if (getTasks() != null && getTasks().size() > 0) {
            return getTasks().get(0);
        } else {
            throw new PipeValidationException("Can't get initial task when no tasks found for phase '" + getName()
                    + "'");
        }
    }

    public TaskConfig getTaskByName(String name) throws PipeValidationException {
        for (TaskConfig task : getTasks()) {
            if (task.getTaskName().equals(name)) {
                return task;
            }
        }
        throw new PipeValidationException("No task found with name '" + name + "' for phase: " + getName());
    }

    public void validate() throws PipeValidationException {
        if (getInitialTask() == null || getName() == null || getTasks().size() == 0 || getName().length() == 0) {
            throw new PipeValidationException("Invalid: " + this);
        }
    }

    @Override
    public String toString() {
        final int maxLen = 3;
        StringBuilder builder = new StringBuilder();
        builder.append("PhaseConfig [name=");
        builder.append(name);
        builder.append(", tasks=");
        builder.append(tasks != null ? tasks.subList(0, Math.min(tasks.size(), maxLen)) : null);
        builder.append("]");
        return builder.toString();
    }

}
