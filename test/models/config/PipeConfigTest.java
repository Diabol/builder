package models.config;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class PipeConfigTest {

    private final PipeConfig target = new PipeConfig();
    private final List<PhaseConfig> phases = new ArrayList<PhaseConfig>();

    @SuppressWarnings("unchecked")
    @Test
    public void pipeWithNoPhasesThrowsValidationExceptionWhenCreated() {
        try {
            target.setName("test");
            target.setPhases(Collections.EMPTY_LIST);
            target.validate();
            fail();
        } catch (PipeValidationException e) {
            assertThat(e.getMessage()).contains("PipeConfig");
        }
    }

    @Test
    public void testGetNextPhase() throws Exception {
        target.setName("test-pipe");
        target.setPhases(phases);

        PhaseConfig phaseConfig0 = new PhaseConfig();
        phaseConfig0.setName("test-phase-0");
        phases.add(phaseConfig0);
        assertThat(target.getNextPhase(phaseConfig0)).isNull();

        PhaseConfig phaseConfig1 = new PhaseConfig();
        phaseConfig1.setName("test-phase-1");
        phases.add(phaseConfig1);

        assertThat(target.getNextPhase(phaseConfig0)).isEqualTo(phaseConfig1);
        assertThat(target.getNextPhase(phaseConfig1)).isNull();

        PhaseConfig phaseConfig2 = new PhaseConfig();
        phaseConfig2.setName("test-phase-2");
        phases.add(phaseConfig2);

        assertThat(target.getNextPhase(phaseConfig1)).isEqualTo(phaseConfig2);
        assertThat(target.getNextPhase(phaseConfig2)).isNull();
    }
}
