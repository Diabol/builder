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
    private String urlToJson = "conf/pipes/";
    public List<Pipe> getConfiguredPipes() {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory();
        List<Pipe> result =  new ArrayList<Pipe>();
        try {
            File pipeDir = new File(urlToJson);
            for(int i = 0; i < pipeDir.list().length; i++){
                JsonParser jp = factory.createJsonParser(new File(urlToJson + pipeDir.list()[i]));
                Pipe jsonAsPipe = mapper.readValue(jp, Pipe.class);
                result.add(jsonAsPipe);
            }

            return result;
        } catch (JsonGenerationException e) {

            e.printStackTrace();

        } catch (JsonMappingException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
        return null;
    }
}
