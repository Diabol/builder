package controllers;

import java.util.List;
import java.util.Map;

import models.statusdata.VersionControlInfo;
import orchestration.Orchestrator;

import org.codehaus.jackson.JsonNode;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;

public class GitHub extends Controller {
    public static Result start(String pipeName) {

        RequestBody body = request().body();
        Map<String, String[]> map = body.asFormUrlEncoded();
        List<JsonNode> commits = null;
        if (map != null) {
            String[] stringArray = map.get("payload");
            if (stringArray.length > 0) {
                String jsonAsString = stringArray[0];
                commits = Json.parse(jsonAsString).path("commits").findParents("message");
            }
        }
        if (commits != null) {
            for (JsonNode commit : commits) {
                new Orchestrator().start(pipeName, createVCInfoFromJson(commit));
            }
            return ok();
        } else {
            return badRequest("Could not parse body of request.");
        }
    }

    private static VersionControlInfo createVCInfoFromJson(JsonNode commit) {
        String hashtag = commit.path("id").getTextValue();
        String commitMessage = commit.path("message").getTextValue();
        return new VersionControlInfo(hashtag, commitMessage);
    }
}
