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

function reqAjax(destination,obj) {
    var userConnected = $("idConnectedUsername").val();

    $.ajax(
        {
            type: 'GET',
            url: '/' + destination + '/' + obj.value ,
            contentType: 'application/json'
        });
}
