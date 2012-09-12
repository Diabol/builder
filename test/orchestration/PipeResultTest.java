package orchestration;

import java.util.List;

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
//        List<PhaseResult> phaseResultList = Arrays.asList(phaseResult);
//        PipeResult pipeResult = new PipeResult(pipe, phaseResultList);
//
//        when(pipe.getPhases()).thenReturn(phaseConfigList);
//        when(phaseResult.result()).thenReturn(SUCESS, FAILURE);
//
//        assertThat(pipeResult.result()).isEqualTo(TaskState.SUCESS);
//
//        assertThat(pipeResult.result()).isEqualTo(TaskState.FAILURE);
    }

}
