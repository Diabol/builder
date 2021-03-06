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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import models.config.PipeConfig;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import play.data.validation.Constraints;

@Entity
public class Pipe extends CDEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long pipeId;

    @Constraints.Required
    public String name;

    @Constraints.Required
    public String version;

    @OneToOne(mappedBy = "pipe", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public VersionControlInfo versionControlInfo;

    @OneToMany(mappedBy = "pipe", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public List<Phase> phases;

    Pipe(String name, String version, State state, Date started, Date finished, List<Phase> phases,
            VersionControlInfo versionControlInfo) {
        super(state, started, finished);
        this.name = name;
        this.version = version;
        this.phases = phases;
        this.versionControlInfo = versionControlInfo;
    }

    public static Pipe createNewFromConfig(String version, PipeConfig pipeConf,
            VersionControlInfo versionControlInfo) {
        return new Pipe(pipeConf.getName(), version, State.NOT_STARTED, null, null,
                new ArrayList<Phase>(), versionControlInfo);
    }

    public static Finder<Long, Pipe> find = new Finder<Long, Pipe>(Long.class, Pipe.class);

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("\nPipe Name: " + name + ", version: " + version + ", versionControlInfo: "
                + versionControlInfo + ", status: " + super.toString());
        buf.append("\n");
        buf.append("Phases: \n");
        for (Phase ph : phases) {
            buf.append("\t" + ph.toString());
        }
        return buf.toString();
    }

    @Override
    public ObjectNode toObjectNode() {
        ObjectNode result = super.toObjectNode();
        result.put("name", name);
        JsonFactory factory = new JsonFactory();
        ObjectMapper om = new ObjectMapper(factory);
        ArrayNode phaseArray = om.createArrayNode();
        for (Phase phase : phases) {
            phaseArray.add(phase.toObjectNode());
        }
        result.put("phases", phaseArray);
        result.put("version", version);
        result.put("versionControlInfo", versionControlInfo.toObjectNode());
        return result;
    }

}
