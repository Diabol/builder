package orchestration;

import models.config.PhaseConfig;
import models.config.PipeConfig;

import org.junit.Test;
import org.mockito.Mock;

import test.MockitoTestBase;

public class PipeTest extends MockitoTestBase {

    // TODO: Change when refactoring finished responsibility has moved

    @Mock private PipeConfig pipeConfig;
    @Mock private PhaseConfig phaseConfig;
    @Mock private PhaseConfig phaseConfig2;
//    @Mock private Phase phase;
//    @Mock private Phase phase2;
//    @Mock private PhaseResult phaseResult;
//    @Mock private PhaseResult phaseResult2;

    @Test
    public void testSuccessfulPipe() throws Exception {
//        Pipe target = new Pipe(pipeConfig);
//
//        when(pipeConfig.getPhases()).thenReturn(Arrays.asList(phaseConfig, phaseConfig2));
//        when(phaseConfig.createPhase()).thenReturn(phase);
//        when(phaseConfig2.createPhase()).thenReturn(phase2);
//        when(phase.start()).thenReturn(phaseResult);
//        when(phase2.start()).thenReturn(phaseResult2);
//        when(phaseResult.success()).thenReturn(true);
//        when(phaseResult2.success()).thenReturn(true);
//        when(phaseResult.result()).thenReturn(SUCESS);
//        when(phaseResult2.result()).thenReturn(SUCESS);
//
//        PipeResult result = target.start();
//
//        assertThat(result.success()).isTrue();
//        assertThat(result.executedPhases()).isEqualTo(2);
    }

    @Test
    public void testFailedPipe() throws Exception {
//        Pipe target = new Pipe(pipeConfig);
//
//        when(pipeConfig.getPhases()).thenReturn(Arrays.asList(phaseConfig, phaseConfig2));
//        when(phaseConfig.createPhase()).thenReturn(phase);
//        when(phaseConfig2.createPhase()).thenReturn(phase2);
//        when(phase.start()).thenReturn(phaseResult);
//        when(phase2.start()).thenReturn(phaseResult2);
//        when(phaseResult.success()).thenReturn(false);
//        when(phaseResult2.success()).thenReturn(true);
//        when(phaseResult.result()).thenReturn(FAILURE);
//        when(phaseResult2.result()).thenReturn(SUCESS);
//
//        PipeResult result = target.start();
//
//        assertThat(result.success()).isFalse();
//        assertThat(result.executedPhases()).isEqualTo(1);
    }
}
