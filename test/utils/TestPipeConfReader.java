package utils;

import static org.junit.Assert.assertEquals;

import java.util.List;

import models.Pipe;


/**
 * @author danielgronberg
 */
public class TestPipeConfReader {
    PipeConfReader reader =  new PipeConfReader();

    @org.junit.Test
    public void testConfIsReadOk() throws Exception{
        List<Pipe> result = reader.getConfiguredPipes();
        assertEquals(2, result.size());
    }
}
