package acceptance.config.pipe;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentType;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.config.PipeConfig;

import org.fest.assertions.Condition;
import org.junit.Test;

import play.mvc.Content;
import views.html.pipeconfgraphit;

public class PipeConfigListTemplateTest {

    @Test
    public void pipeConfigListTemplateRendersEmptyList() {
        Content html = pipeconfgraphit.render(new ArrayList<PipeConfig>());

        assertThat(contentType(html)).isEqualTo("text/html");
        assertThat(html.body()).satisfies(new Condition<String>("Html body should contain the pipe list even for empty list") {
            @Override
            public boolean matches(String body) {
                Pattern pattern = Pattern.compile("<ul.*id=\"pipeList\".*>");
                Matcher matcher = pattern.matcher(body);
                return matcher.find();
            }
        });
    }

}
