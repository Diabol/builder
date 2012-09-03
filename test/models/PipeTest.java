package models;

import static org.fest.assertions.Assertions.assertThat;
import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.result.PipeResult;
import models.result.ResultLevel;

import org.junit.Test;

public class PipeTest {

    @Test
    public void startCreatesAndStartsInitialTask() {
        PhaseConfig initialPhaseConfig = new PhaseConfig();
        PipeConfig pipeConfig = new PipeConfig();
        pipeConfig.setInitialPhase(initialPhaseConfig);
        pipeConfig.setName(PipeTest.class.getName() +  "_PipeConfig");
        initialPhaseConfig.setName(PipeTest.class.getName() + "_PhaseConfig");

        Pipe target = new Pipe(pipeConfig);
        PipeResult pipeResult = target.start();
        assertThat(pipeResult.result()).isEqualTo(ResultLevel.SUCESS);
    }

}
