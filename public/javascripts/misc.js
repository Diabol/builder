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
    var url = '/pipe/'+taskSplit[0]+'/'+compVersion+'/'+taskSplit[1]+'/'+taskSplit[2]+'/details';
    $('#detailsPanelTitle').html(taskSplit[0]+'/'+compVersion+'/'+taskSplit[1]);
    $('#detailsPanelBody').html('Information not available...');
    $.get(url, function(data) {
        $('#detailsPanelBody').html(data);
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

function doAction(url) {
    alert('Doing ajax POST to: '+url);
    $.ajax({
        url: url,
        type: "POST",
        //success
        success: function () {
        }
    });
}