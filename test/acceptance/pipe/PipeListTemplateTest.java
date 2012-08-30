package acceptance.pipe;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentType;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Pipe;

import org.fest.assertions.Condition;
import org.junit.Test;

import play.mvc.Content;

public class PipeListTemplateTest {

    @Test
    public void pipeListTemplateRendersEmptyList() {
        Content html = views.html.pipelist.render(new ArrayList<Pipe>());

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
