package controllers;

import org.springframework.beans.factory.annotation.Autowired;
import play.mvc.Controller;
import play.mvc.Result;
import utils.PipeConfReader;
import views.html.pipelist;

public class PipeListController extends Controller {

  private static PipeConfReader configReader = PipeConfReader.getInstance() ;

  public static Result index() {
    return ok(pipelist.render(configReader.getConfiguredPipes()));
  }
  
}