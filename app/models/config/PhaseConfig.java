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

    public TaskConfig getInitialTask() {
        return getTasks().get(0);
    }

    public TaskConfig getTaskByName(String name) {
        for (TaskConfig task : getTasks()) {
            if (task.getTaskName().equals(name)) {
                return task;
            }
        }
        throw new IllegalArgumentException("No task found with name '" + name + "' for: " + this);
    }

    public void validate() throws PipeValidationException {
        if (getName() == null ||
                getTasks() == null ||
                getName().isEmpty() ||
                getTasks().isEmpty()) {
            throw new PipeValidationException("Invalid: " + this);
        }

        for (TaskConfig taskConfig : getTasks()) {
            taskConfig.validate();
        }
    }

    @Override
    public String toString() {
        final int maxLen = 5;
        StringBuilder builder = new StringBuilder();
        builder.append("PhaseConfig [name=");
        builder.append(name);
        builder.append(", tasks=");
        builder.append(tasks != null ? tasks.subList(0, Math.min(tasks.size(), maxLen)) : null);
        builder.append("]");
        return builder.toString();
    }

}
