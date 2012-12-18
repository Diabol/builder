
function createCanvasMarkupNameAsId(pipe, counter) {
	return createCanvasMarkup(pipe, counter, pipe.name);
}

function createCanvasMarkupVersionAsId(pipe) {
	return createCanvasMarkup(pipe, counter, pipe.version);
}

function createCanvasMarkup(pipe, counter, pipeId){
	var result = "";
	result = result + "<div id='"+pipeId+"canvas' class='canvas component "+pipe.version+" "+pipe.name+"' style='width: 100%; height: 170px; top: "+counter*170+"px; padding-top: 10px; position: absolute;'>";
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