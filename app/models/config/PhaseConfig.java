package models.config;

import java.util.List;

/**
 * @author danielgronberg, marcus
 */
public class PhaseConfig {
    private String name;
    private TaskConfig initialTask;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskConfig getInitialTask() {
        return initialTask;
    }

    public void setInitialTask(TaskConfig initialTask) {
        this.initialTask = initialTask;
    }
}
