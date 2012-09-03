package models.config;

import java.util.List;

/**
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
}
