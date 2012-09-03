package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.config.PipeConfig;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

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
    private final List<PipeConfig> jsonAsObjects =  new ArrayList<PipeConfig>();
    private final String urlToJson = "conf/pipes/";

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
                PipeConfig jsonAsPipe = mapper.readValue(jp, PipeConfig.class);
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

    public List<PipeConfig> getConfiguredPipes() {
        return jsonAsObjects;
    }

    public Map<String, PipeConfig> getConfiguredPipesMappedByName() {
        Map<String, PipeConfig> pipeMap = new HashMap<String, PipeConfig>();
        for (int i = 0; i < jsonAsObjects.size(); i++) {
            pipeMap.put(jsonAsObjects.get(i).getName(), jsonAsObjects.get(i));
        }
        return pipeMap;
    }

    public PipeConfig get(String pipeName) {
        return getConfiguredPipesMappedByName().get(pipeName);
    }

}
