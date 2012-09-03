package models;

import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.result.PhaseResult;
import models.result.PipeResult;

public class Pipe {

    private final PipeConfig config;

    public Pipe(PipeConfig config) {
        this.config = config;
    }

    public PipeResult start() {
        PipeResult result = new PipeResult(this);
        PhaseConfig initialPhaseConfig = getInitialPhaseConfig();
        Phase initialPhase = new Phase(initialPhaseConfig);
        PhaseResult initialPhaseResult = initialPhase.start();
        result.add(initialPhaseResult);
        return result;
    }

    /**
     * @see models.config.PipeConfig#getName()
     */
    public String getName() {
        return config.getName();
    }

    /**
     * @see models.config.PipeConfig#getInitialPhase()
     */
    public PhaseConfig getInitialPhaseConfig() {
        return config.getInitialPhase();
    }

}
