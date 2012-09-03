package models.config;

import java.util.List;

/**
 * @author danielgronberg, marcus
 */
public class PhaseConfig {
    private String name;
    private List<TaskConfig> tasks;
    private List<PhaseConfig> nextPhases;

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

    public List<PhaseConfig> getNextPhases() {
        return nextPhases;
    }

    public void setNextPhases(List<PhaseConfig> nextPhases) {
        this.nextPhases = nextPhases;
    }
}
