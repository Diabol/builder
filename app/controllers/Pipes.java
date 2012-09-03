package controllers;

import models.Pipe;
import play.mvc.Controller;
import play.mvc.Result;
import utils.PipeConfReader;
import views.html.pipe;
import views.html.pipeconfiglist;

public class Pipes extends Controller {

    private static PipeConfReader configReader = PipeConfReader.getInstance() ;

    public static Result list() {
        return ok(pipeconfiglist.render(configReader.getConfiguredPipes()));
    }

    public static Result start(String name) {
        Pipe newPipe = new Pipe(configReader.get(name));
        newPipe.start();
        return ok(pipe.render(newPipe));
    }

}