package models.statusdata;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import models.config.PhaseConfig;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import play.data.validation.Constraints;

@Entity
public class Phase extends CDEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long phaseId;

    @Constraints.Required
    public String name;

    @OneToOne
    @JoinColumn(name = "pipe_id", nullable = false)
    public Pipe pipe;

    @OneToMany(mappedBy = "phase", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public List<Task> tasks;

    Phase(String name, State state, Date started, Date finished, List<Task> tasks) {
        super(state, started, finished);
        this.name = name;
        this.tasks = tasks;
    }

    public static Phase createNewFromConfig(PhaseConfig phaseConf) {
        Phase phase = new Phase(phaseConf.getName(), State.NOT_STARTED, null, null,
                new ArrayList<Task>());
        return phase;
    }

    @Override
    public ObjectNode toObjectNode() {
        ObjectNode result = super.toObjectNode();
        result.put("name", name);
        JsonFactory factory = new JsonFactory();
        ObjectMapper om = new ObjectMapper(factory);
        ArrayNode taskArray = om.createArrayNode();
        for (Task task : tasks) {
            taskArray.add(task.toObjectNode());
        }
        result.put("tasks", taskArray);
        return result;
    }

    public static Finder<Long, Phase> find = new Finder<Long, Phase>(Long.class, Phase.class);

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Phase name: " + name + ", status: " + super.toString() + "\n");
        buf.append("\tTasks: \n");
        for (Task ta : tasks) {
            buf.append("\t\t" + ta.toString());
        }
        return buf.toString();
    }
}
