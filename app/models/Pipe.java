package models;

import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.result.PipeResult;

public class Pipe {

    private final PipeConfig config;

    public Pipe(PipeConfig config) {
        this.config = config;
    }

    public PipeResult start() {
        PipeResult result = new PipeResult(this);

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
    public PhaseConfig getInitialPhase() {
        return config.getInitialPhase();
    }

}
