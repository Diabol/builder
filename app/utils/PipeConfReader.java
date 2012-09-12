package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.config.PipeConfig;
import models.config.PipeValidationException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import play.Logger;

/**
 * Read pipe configuration from a file in json format
 * 
 * @author danielgronberg, marcus
 */
@Component
public class PipeConfReader {

    private static PipeConfReader instance;
    private final List<PipeConfig> jsonAsObjects =  new ArrayList<PipeConfig>();
    private final String urlToJson = "conf/pipes/";

    static {
        try {
            instance = new PipeConfReader();
        } catch (JsonParseException e) {
            Logger.error("Error parsing Json files with pipe config", e);
        } catch (JsonMappingException e) {
            Logger.error("Error mapping Json files with pipe config to PipeConfig", e);
        } catch (PipeValidationException e) {
            Logger.error("Error creating pipe config: Invalid config", e);
        } catch (IOException e) {
            Logger.error("Error creating pipe config", e);
        }
    }

    public static PipeConfReader getInstance() {
        return instance;
    }

    private PipeConfReader() throws PipeValidationException, JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory();

        File pipeDir = new File(urlToJson);
        for(int i = 0; i < pipeDir.list().length; i++){
            JsonParser jp = factory.createJsonParser(new File(urlToJson + pipeDir.list()[i]));
            PipeConfig jsonAsPipe = mapper.readValue(jp, PipeConfig.class);
            validate(jsonAsPipe);
            jsonAsObjects.add(jsonAsPipe);
        }
    }

    private void validate(PipeConfig confToValidate) throws PipeValidationException {
        for (PipeConfig conf : jsonAsObjects) {
            if (conf.getName().equals(confToValidate.getName())) {
                throw new PipeValidationException("Failed to read PipeConfig: Cannot have two pipes with same name: " + conf.getName());
            }
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
        // PipeConfig was validated at init so need not be done here
        return getConfiguredPipesMappedByName().get(pipeName);
    }

}
