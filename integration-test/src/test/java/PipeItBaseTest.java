import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;

import com.gargoylesoftware.htmlunit.WebClient;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import common.Pipe;

public abstract class PipeItBaseTest {

    protected WebClient webClient;
    protected String host;

    @Before
    public void prepare() {
        host = System.getProperty("pipeit.host");
        if (host == null) {
            host = "http://localhost:9000";
        }
        webClient = new WebClient();
    }

    protected List<Pipe> getLatestPipes() throws Exception {
        List<Pipe> result = new ArrayList<Pipe>();
        Response response = RestAssured.get(host + "/pipes/latest", new Object[0]);

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory();
        JsonParser jp = factory.createJsonParser(response.asString());

        JsonNode actualObj = mapper.readTree(jp);
        List<JsonNode> pipes = actualObj.findParents("phases");
        for (JsonNode node : pipes) {
            Pipe pipe = mapper.readValue(node, Pipe.class);
            result.add(pipe);
        }
        return result;
    }
}
