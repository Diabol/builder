package controllers;

import java.util.List;

import models.statusdata.VersionControlInfo;
import orchestration.Orchestrator;

import org.codehaus.jackson.JsonNode;

import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class GitHub extends Controller {

    @BodyParser.Of(BodyParser.Json.class)
    public static Result start(String pipeName) {
        JsonNode json = request().body().asJson();
        List<JsonNode> commits = json.findParents("message");
        if (commits.size() == 0) {
            Logger.error("Received a notification from GitHub without commit message(s). Json:"
                    + json.toString());
        }
        for (JsonNode commitJson : commits) {
            try {
                new Orchestrator().start(pipeName, createVCInfoFromJson(commitJson));
            } catch (Exception e) {
                String errMsg = "Could not start pipe: " + pipeName;
                Logger.error(errMsg, e);
            }
        }
        return ok();
    }

    private static VersionControlInfo createVCInfoFromJson(JsonNode gitHubJSon) {
        String hashtag = gitHubJSon.path("id").getTextValue();
        String commitMessage = gitHubJSon.path("message").getTextValue();
        return new VersionControlInfo(hashtag, commitMessage);
    }
}
