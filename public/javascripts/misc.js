function toggleVisibility(id) {
    var e = document.getElementById(id);
    if(e.className == 'expanded')
        e.className = 'collapsed';
    else
        e.className = 'expanded';
}

function getTaskDetails(task) {
    var taskSplit = task.split(':');
    var compVersion = shownPipeVersions[taskSplit[0]];
    var url = '/pipe/'+taskSplit[0]+'/'+compVersion+'/'+taskSplit[1]+'/'+taskSplit[2]+'/log';
    $('#detailsPanelTitle').html(taskSplit[0]+'/'+compVersion+'/'+taskSplit[1]+'/'+taskSplit[2]);
    $('#detailsPanelLog').html('Information not available...');
    $.get(url, function(data) {
        $('#detailsPanelLog').html(data);
    });
    toggleVisibility('detailsPanel');
}

function logJson(obj) {
    var output;
    for (property in obj) {
        output += property + ': ' + obj[property]+'; ';
    }
    console.log(output);
}
