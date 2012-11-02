package utils;

import static junit.framework.Assert.assertTrue;

import org.junit.Test;

public class LogHandlerTest {
    private final LogHandler handler = LogHandler.getInstance();

    @Test
    public void testStoreAndReadLogFile() throws Exception {
        String key = "key";
        String log = "Output";
        handler.storeLog(key, log);
        String result = handler.getLog(key);
        assertTrue(log.equals(result));
    }

    @Test
    public void testReadLogThrowsDataNotFoundExceptionWhenNotStored() {
        try {
            handler.getLog("notpersisted");
            assertTrue(false);
        } catch (DataNotFoundException ex) {
            assertTrue(ex != null);
        }
    }
}
