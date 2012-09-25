package utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import models.PipeVersion;
import models.StatusInterface.State;
import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import models.statusdata.Phase;
import models.statusdata.Pipe;
import models.statusdata.Task;

import org.junit.Before;
import org.junit.Test;

public class DBHelperTest {

    PipeConfig configuredPipe;
    PipeVersion version;

    @Before
    public void setup() {
        configuredPipe = PipeConfReader.getInstance().getConfiguredPipes().get(0);
        version = PipeVersion.fromString("Version", configuredPipe);
    }

    @Test
    public void testPersistNewPipePersistsTheWholePipeWithStateNotStartedAndCorrectVersion() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                DBHelper.getInstance().persistNewPipe(version, configuredPipe);
                try {
                    Pipe persisted = DBHelper.getInstance().getPipe(version);
                    assertEquals(configuredPipe.getName(), persisted.name);
                    assertEquals(State.NOT_STARTED, persisted.state);
                    /**
                     * Assert that all phases are persisted with correct
                     */
                    for (int i = 0; i < configuredPipe.getPhases().size(); i++) {
                        PhaseConfig phaseConf = configuredPipe.getPhases().get(i);
                        Phase persistedPh = persisted.phases.get(i);
                        assertEquals(phaseConf.getName(), persistedPh.name);
                        assertEquals(State.NOT_STARTED, persistedPh.state);
                        for (int j = 0; j < phaseConf.getTasks().size(); j++) {
                            TaskConfig taskConf = phaseConf.getTasks().get(j);
                            Task persistedTa = persisted.phases.get(i).tasks.get(j);
                            assertEquals(taskConf.getTaskName(), persistedTa.name);
                            assertEquals(State.NOT_STARTED, persistedTa.state);
                        }
                    }
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }
            }
        });
    }
}
