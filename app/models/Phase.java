package models;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: danielgronberg
 * Date: 2012-08-27
 * Time: 14:10
 * To change this template use File | Settings | File Templates.
 */
public class Phase {
    private String name;
    private List<Task> tasks;
    private List<Phase> nextPhases;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Phase> getNextPhases() {
        return nextPhases;
    }

    public void setNextPhases(List<Phase> nextPhases) {
        this.nextPhases = nextPhases;
    }
}
