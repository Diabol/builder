package models.config;

/**
 * @author danielgronberg, marcus
 */
public class PipeConfig {
    private String name;
    private PhaseConfig initialPhase;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PhaseConfig getInitialPhase() {
        return initialPhase;
    }

    public void setInitialPhase(PhaseConfig initialPhase) {
        this.initialPhase = initialPhase;
    }
}
