package orchestration;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import test.MockitoTestBase;

public class OrchestratorTest extends MockitoTestBase {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testStart() {
        Orchestrator target = new Orchestrator();
        PipeVersion<?> pipeVersion = target.start("Component-A");
        assertThat(pipeVersion).isNotNull();
    }

//    @Test
//    public void testStartTask() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testHandleTaskStarted() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testHandleTaskResult() {
//        fail("Not yet implemented");
//    }

}
