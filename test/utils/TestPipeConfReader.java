package utils;

import models.Pipe;
import org.junit.*;
import static org.junit.Assert.*;

import org.springframework.beans.factory.annotation.Autowired;
import play.mvc.*;
import play.test.*;
import play.libs.F.*;
import utils.PipeConfReader;

import java.util.List;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;


/**
 * Created with IntelliJ IDEA.
 * User: danielgronberg
 * Date: 2012-08-28
 * Time: 10:40
 * To change this template use File | Settings | File Templates.
 */
public class TestPipeConfReader {
    PipeConfReader reader =  new PipeConfReader();

    @org.junit.Test
    public void testConfIsReadOk() throws Exception{
        List<Pipe> result = reader.getConfiguredPipes();
        assertEquals(2, result.size());
    }
}
