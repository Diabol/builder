package acceptance.pipe;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.callAction;
import static play.test.Helpers.charset;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.routeAndCall;
import static play.test.Helpers.status;

import org.junit.Test;

import play.mvc.Result;

public class PipeControllerTest {

    @Test
    public void testPipeControllerListAction() {
        Result result = callAction(controllers.routes.ref.PipeController.list());

        assertThat(status(result)).isEqualTo(OK);
        assertThat(contentType(result)).isEqualTo("text/html");
        assertThat(charset(result)).isEqualTo("utf-8");
        assertThat(contentAsString(result)).contains("id=\"pipeList\"");
        assertThat(contentAsString(result)).contains("ComponentA pipe");
        assertThat(contentAsString(result)).contains("ComponentB pipe");
    }

    @Test
    public void testPipelistRoute() {
        Result result = routeAndCall(fakeRequest(GET, "/pipeList"));
        assertThat(status(result)).isEqualTo(OK);
        assertThat(contentAsString(result)).contains("id=\"pipeList\"");
    }

}
