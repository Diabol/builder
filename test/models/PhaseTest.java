package models;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import models.config.PhaseConfig;
import models.config.TaskConfig;
import models.result.PhaseResult;

import org.junit.Test;
import org.mockito.Mock;

import test.MockitoTestBase;

public class PhaseTest extends MockitoTestBase {

    @Mock private PhaseConfig phaseConfig;
    @Mock private TaskConfig taskConfig;
    @Mock private TaskConfig nextTask;

    @Test
    public void testOneTaskThatSucceeds() {
        Phase target = new Phase(phaseConfig);
        when(phaseConfig.getInitialTask()).thenReturn(taskConfig);
        // TODO: Fix
        PhaseResult result = target.start();
        assertThat(result.success()).isTrue();
    }

    @Test
    public void testOneTaskThatFails() {
        Phase target = new Phase(phaseConfig);
        when(phaseConfig.getInitialTask()).thenReturn(taskConfig);
        when(taskConfig.getNextTasks()).thenReturn(Arrays.asList(nextTask));
        // TODO: Fix
        PhaseResult result = target.start();
        assertThat(result.success()).isFalse();
    }

}
