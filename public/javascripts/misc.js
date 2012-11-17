function toggleVisibility(id) {
    var e = document.getElementById(id);
    if(e.className == 'expanded')
        e.className = 'collapsed';
    else
        e.className = 'expanded';
}
