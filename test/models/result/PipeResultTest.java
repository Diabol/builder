package models.result;

import static models.result.ResultLevel.FAILURE;
import static models.result.ResultLevel.SUCESS;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import models.Pipe;
import models.config.PhaseConfig;

import org.junit.Test;
import org.mockito.Mock;

import test.MockitoTestBase;

public class PipeResultTest extends MockitoTestBase {

    @Mock private Pipe pipe;
    @Mock private PhaseResult phaseResult;
    @Mock private List<PhaseConfig> phaseConfigList;

    @Test
    public void resultReturnsResultOfLastPhase() {
        PipeResult pipeResult = new PipeResult(pipe);
        pipeResult.add(phaseResult);

        when(pipe.getPhases()).thenReturn(phaseConfigList);
        when(phaseResult.result()).thenReturn(SUCESS, FAILURE);

        assertThat(pipeResult.result()).isEqualTo(ResultLevel.SUCESS);

        assertThat(pipeResult.result()).isEqualTo(ResultLevel.FAILURE);
    }

}
