package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import utils.PipeConfReader;
import views.html.pipelist;

public class PipeController extends Controller {

    private static PipeConfReader configReader = PipeConfReader.getInstance() ;

    public static Result list() {
        return ok(pipelist.render(configReader.getConfiguredPipes()));
    }

}