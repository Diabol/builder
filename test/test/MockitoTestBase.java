package test;

import org.junit.Before;
import org.mockito.MockitoAnnotations;

/**
 * Extend this to have auto init of Mockito mocks
 * @author marcus
 */
public class MockitoTestBase {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

}