package models.config;

import java.util.List;

/**
 * The config for a Pipe.
 * 
 * @author danielgronberg, marcus
 */
public class PipeConfig {
    private String name;
    private List<PhaseConfig> phases;

    public PipeConfig() {

    }

    public PipeConfig(String name) {
        super();
        this.name = name;
    }

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

    public PhaseConfig getFirstPhaseConfig() {
        return getPhases().get(0);
    }

    public PhaseConfig getPhaseByName(String name) {
        for (PhaseConfig phase : getPhases()) {
            if (phase.getName().equals(name)) {
                return phase;
            }
        }
        throw new IllegalArgumentException("No phase found with name: '" + name + "' in: " + this);
    }

    public PhaseConfig getNextPhase(PhaseConfig currentPhase) {
        for (int i = 0; i < getPhases().size(); i++) {
            PhaseConfig phase = getPhases().get(i);
            if (currentPhase.equals(phase) && getPhases().size() > i + 1) {
                return getPhases().get(i + 1);
            }
        }
        return null;
    }

    public void validate() throws PipeValidationException {
        if (getPhases() == null || getName() == null || getPhases().size() == 0
                || getName().length() == 0) {
            throw new PipeValidationException("Invalid: " + this);
        }

        for (PhaseConfig phaseConfig : getPhases()) {
            phaseConfig.validate();
        }
    }

    @Override
    public String toString() {
        final int maxLen = 9;
        StringBuilder builder = new StringBuilder();
        builder.append("PipeConfig [name=");
        builder.append(name);
        builder.append(", phases=");
        builder.append(phases != null ? phases.subList(0, Math.min(phases.size(), maxLen)) : null);
        builder.append("]");
        return builder.toString();
    }

}
