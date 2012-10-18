package models.statusdata;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import models.config.TaskConfig;

import org.codehaus.jackson.node.ObjectNode;

import play.data.validation.Constraints;

@Entity
public class Task extends CDEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long taskId;

    @Constraints.Required
    public String name;

    @OneToOne
    @JoinColumn(name = "phase_id", nullable = false)
    public Phase phase;

    Task(String name, State state, Date started, Date finished) {
        super(state, started, finished);
        this.name = name;
    }

    public static Task createNewFromConfig(TaskConfig taskConf) {
        return new Task(taskConf.getTaskName(), State.NOT_STARTED, null, null);
    }

    public static Finder<Long, Task> find = new Finder<Long, Task>(Long.class, Task.class);

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Task name: " + name + ", status: " + super.toString() + "\n");
        return buf.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public ObjectNode toObjectNode() {
        ObjectNode result = super.toObjectNode();
        result.put("name", name);
        return result;
    }

}
