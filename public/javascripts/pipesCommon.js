
function createCanvasMarkupNameAsId(pipe) {
	return createCanvasMarkup(pipe, pipe.name);
}

function createCanvasMarkupVersionAsId(pipe) {
	return createCanvasMarkup(pipe, pipe.version);
}

function createCanvasMarkup(pipe, pipeId){
	var result = "";
	result = result + "<div id='"+pipeId+"canvas' class='canvas component "+pipe.version+"' style='width: 100%; height: 170px; padding-top: 10px;'>";
	result = result + "<h2><span class='componentName'>"+pipeId+"</span>";
    result = result + "<span id='pipeVersion' class='componentVersion'>Version: "+pipe.version+"</span>";
    result = result +     "<span id='versionControlInfo' class='right'>";
    result = result +     	"<span id='commitText'>'"+pipe.versionControlInfo.versionControlText+"'</span>";
    result = result +     	"<span>by </span>";
    result = result +     	"<span id=committer>"+pipe.versionControlInfo.committer.name+"</span>";
    result = result +    "</span>";
   	result = result +     "</h2>";       
    result = result + "</div>";
    return result;
}