package acceptance;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.callAction;
import static play.test.Helpers.charset;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;
import static play.test.Helpers.status;

import org.junit.Test;

import play.mvc.Content;
import play.mvc.Http.Status;
import play.mvc.Result;

public class PipeListControllerAcceptanceTest {
	
	// TODO: I start by testing the Application and index to learn about testing. Later we test PipeList
	// http://www.playframework.org/documentation/2.0.2/JavaFunctionalTest
	// http://digitalsanctum.com/2012/05/28/play-framework-2-tutorial-testing/

	@Test
	public void renderTemplate() {
	  Content html = views.html.index.render("Coco");
	  
	  assertThat(contentType(html)).isEqualTo("text/html");
	  assertThat(contentAsString(html)).contains("Coco");
	}
	
	@Test
	public void callApplicationIndex() {
	    Result result = callAction(
	      controllers.routes.ref.Application.index()
	    );
	    
	    assertThat(status(result)).isEqualTo(Status.OK);
	    assertThat(contentType(result)).isEqualTo("text/html");
	    assertThat(charset(result)).isEqualTo("utf-8");
	    assertThat(contentAsString(result)).contains("Your new application is ready");
	}

}
