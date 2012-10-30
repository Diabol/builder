package controllers;

import java.util.List;
import java.util.Map;

import models.statusdata.VersionControlInfo;

import org.codehaus.jackson.JsonNode;

import play.Logger;
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
        if (commits != null && commits.size() > 0) {
            for (JsonNode commit : commits) {
                return Pipes.start(pipeName, createVCInfoFromJson(commit));
            }
        }
        String errorText = "Could not parse commits from body of request: " + body.asText();
        Logger.error(errorText);
        return badRequest(errorText);
    }

    private static VersionControlInfo createVCInfoFromJson(JsonNode commit) {
        String hashtag = commit.path("id").getTextValue();
        String commitMessage = commit.path("message").getTextValue();
        return new VersionControlInfo(hashtag, commitMessage);
    }
}
