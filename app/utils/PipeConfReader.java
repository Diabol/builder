package utils;

import models.Pipe;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: danielgronberg
 * Date: 2012-08-27
 * Time: 14:15
 * To change this template use File | Settings | File Templates.
 */
@Component
public class PipeConfReader {

    private static PipeConfReader theInstance = new PipeConfReader();
    private List<Pipe> jsonAsObjects =  new ArrayList<Pipe>();
    private String urlToJson = "conf/pipes/";

    public static PipeConfReader getInstance() {
        return theInstance;
    }
    private PipeConfReader() {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory();

        try {
            File pipeDir = new File(urlToJson);
            for(int i = 0; i < pipeDir.list().length; i++){
                JsonParser jp = factory.createJsonParser(new File(urlToJson + pipeDir.list()[i]));
                Pipe jsonAsPipe = mapper.readValue(jp, Pipe.class);
                jsonAsObjects.add(jsonAsPipe);
            }
        } catch (JsonGenerationException e) {

            e.printStackTrace();

        } catch (JsonMappingException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    public List<Pipe> getConfiguredPipes() {
        return jsonAsObjects;
    }
}
