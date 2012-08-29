package utils;

import static org.junit.Assert.assertEquals;

import java.util.List;

import models.Pipe;
import org.junit.Test;


/**
 * @author danielgronberg
 */
public class TestPipeConfReader {
    PipeConfReader reader = PipeConfReader.getInstance();

    @Test
    public void testConfIsReadOk() throws Exception{
        List<Pipe> result = reader.getConfiguredPipes();
        assertEquals(2, result.size());
    }
}
