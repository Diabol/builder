package models;

import models.config.PhaseConfig;

public class Phase {

    private final PhaseConfig config;

    public Phase(PhaseConfig config) {
        this.config = config;
    }

    public PhaseResult start() {
        PhaseResult result = new PhaseResult(this);

        return result;
    }

}
