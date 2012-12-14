package common;

import java.util.List;

/**
 * Pojo representing the json of a Phase in PipeIt API.
 * 
 * @author danielgronberg
 * 
 */
public class Phase extends CDEntity {
    private List<Task> tasks;

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

}