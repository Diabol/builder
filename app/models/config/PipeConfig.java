package models.config;

import java.util.List;

/**
 * The config for a Pipe.
 * @author danielgronberg, marcus
 */
public class PipeConfig {
    private String name;
    private List<PhaseConfig> phases;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PhaseConfig> getPhases() {
        return phases;
    }

    public void setPhases(List<PhaseConfig> phases) {
        this.phases = phases;
    }

    public void validate() throws PipeValidationException {
        if (getPhases().size() == 0 ||
                getName().length() == 0)
            throw new PipeValidationException();
    }
}
