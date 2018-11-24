function passer(obj){
    reqAjax("passer",obj);
    remove(obj);
}

function couler(obj){
    reqAjax("couler",obj);
    remove(obj);
}

function remove(obj){
    var row = obj.parentNode.parentNode;
    row.parentNode.removeChild(row);
}

function transferrer(obj){
    reqAjax("transferrer",obj);
    remove(obj);
}

function toggle(obj){
    if($('#toggle-sensei').prop('checked')){
        reqAjax("transferrer",obj);
    }
    else {
        reqAjax("enleverSensei",obj);
    }
}


function reqAjax(destination,obj) {
    $.ajax(
        {
            type: 'GET',
            url: '/' + destination + '/' + obj.value,
            contentType: 'application/json'
        });
}
