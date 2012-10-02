package models.config;

import java.util.List;

/**
 * @author danielgronberg, marcus
 */
public class PhaseConfig {
    private String name;
    private List<TaskConfig> tasks;

    public PhaseConfig() {

    }

    public PhaseConfig(String name) {
        super();
        this.name = name;
    }

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
        if (getName() == null || getTasks() == null || getName().isEmpty() || getTasks().isEmpty()) {
            throw new PipeValidationException("Invalid: " + this);
        }

        for (TaskConfig taskConfig : getTasks()) {
            taskConfig.validate();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((tasks == null) ? 0 : tasks.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PhaseConfig)) {
            return false;
        }
        PhaseConfig other = (PhaseConfig) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (tasks == null) {
            if (other.tasks != null) {
                return false;
            }
        } else if (!tasks.equals(other.tasks)) {
            return false;
        }
        return true;
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
