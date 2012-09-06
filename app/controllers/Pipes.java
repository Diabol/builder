package controllers;

import models.Pipe;
import models.config.PhaseConfig;
import models.config.PipeValidationException;
import models.config.TaskConfig;
import play.api.templates.Html;
import play.mvc.Controller;
import play.mvc.Result;
import utils.PipeConfReader;
import views.html.pipe;
import views.html.pipeconfgraphit;

public class Pipes extends Controller {

    private static PipeConfReader configReader = PipeConfReader.getInstance() ;

    public static Result list() {
        return ok(pipeconfgraphit.render(configReader.getConfiguredPipes()));
    }

    public static Result start(String name) throws PipeValidationException {
        Pipe newPipe = new Pipe(configReader.get(name));
        newPipe.start();
        return ok(pipe.render(newPipe));
    }

    public static Html generateMarkupForTaskTree(PhaseConfig ph) throws PipeValidationException{
        StringBuffer buf = new StringBuffer();
        //Create the nodes
        for(int counter = 0; counter < ph.getTasks().size(); counter++){
            buf.append("<div id=task"+counter+" class='task' style='left: "+(30+150*counter)+"px; top: 50px;'><span>"+ph.getTasks().get(counter).getTaskName()+"</span></div>");
        }
        return new Html(buf.toString());
    }

}