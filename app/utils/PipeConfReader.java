package utils;

import models.Pipe;
import models.PipeConfDocument;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import play.api.libs.json.JerksonJson;
import scala.util.parsing.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: danielgronberg
 * Date: 2012-08-27
 * Time: 14:15
 * To change this template use File | Settings | File Templates.
 */
public class PipeConfReader {
    private static String urlToJson = "conf/pipes.json";
    public static List<Pipe> getConfiguredPipes() {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory();
        try {
            JsonParser jp = factory.createJsonParser(new File(urlToJson));
            PipeConfDocument jsonAsPipeConfDoc = mapper.readValue(jp, PipeConfDocument.class);
            return jsonAsPipeConfDoc.getPipes();
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
