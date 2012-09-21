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
import javax.persistence.OneToMany;

import models.config.PipeConfig;
import play.data.validation.Constraints;

@Entity
public class Pipe extends CDEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long pipeId;
	
	@Constraints.Required
	public String name;

	@Constraints.Required
	public String version;

	@OneToMany(mappedBy = "pipe", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	public Set<Phase> phases;

	Pipe(String name, String version, State state, Date started, Date finished, Set<Phase> phases) {
		super(state,started,finished);
		this.name = name;
		this.version = version;
		this.phases = phases;
	}

	public static Pipe createNewFromConfig(String version, PipeConfig pipeConf) {
		return  new Pipe(pipeConf.getName(), version, State.NOT_STARTED, null, null, new HashSet<Phase>());
	}

	public static Finder<Long, Pipe> find = new Finder<Long, Pipe>(Long.class,
			Pipe.class);

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("\nPipe Name: " + name + ", version: " + version
				+ ", status: " + super.toString());
		buf.append("\n");
		buf.append("Phases: \n");
		for (Phase ph : phases) {
			buf.append("\t" + ph.toString());
		}
		return buf.toString();
	}
	
	
}
