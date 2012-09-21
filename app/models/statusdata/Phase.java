package models.statusdata;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
import play.data.validation.Constraints;

@Entity
public class Phase extends CDEntity {
	

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long phaseId;

	@Constraints.Required
	public String name;

	@OneToOne
	@JoinColumn(name = "pipe_id", nullable = false)
	public Pipe pipe;
	
	@OneToMany(mappedBy = "phase", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	public Set<Task> tasks;

	Phase(String name, State state, Date started, Date finished, Set<Task> tasks) {
		super(state, started, finished);
		this.name = name;
		this.tasks = tasks;
	}

	public static Phase createNewFromConfig(PhaseConfig phaseConf) {
		Phase phase = new Phase(phaseConf.getName(), State.NOT_STARTED, null, null, new HashSet<Task>());
		return phase;
	}

	public static Finder<Long, Phase> find = new Finder<Long, Phase>(
			Long.class, Phase.class);

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Phase name: " + name + ", status: " + super.toString()+"\n");
		buf.append("\tTasks: \n");
		for (Task ta : tasks) {
			buf.append("\t\t" + ta.toString());
		}
		return buf.toString();
	}
}
