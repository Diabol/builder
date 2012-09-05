package models.config;

import models.Phase;
import play.Logger;

import java.util.List;


/**
 * @author danielgronberg, marcus
 */
public class PhaseConfig {
    private String name;
    private List<TaskConfig> tasks;

    public Phase createPhase() {
        return new Phase(this);
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

    public TaskConfig getInitialTask() throws PipeValidationException {
        if(getTasks() != null && getTasks().size() > 0){
            return getTasks().get(0);
        }
        else {

            Logger.error("Can't get initial task when no tasks found for phase '" + getName() + "'");
            throw new PipeValidationException();
        }
    }

    public TaskConfig getTaskByName(String name) throws PipeValidationException {
        for(TaskConfig task: getTasks()){
            if(task.getTaskName().equals(name)) {
                return task;
            }
        }
        Logger.error("No task found with name '" + name + "'");
        throw new PipeValidationException();
    }
}
