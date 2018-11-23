var stompClientPubl = null;
var stompClientPriv = null;
var nbMessages = [];

$(document).ready( function(){
    connexionPublic();
    if(document.getElementById('nomUtil') !== null){
        connexionPrive();
    }
});


function connexionPublic() {
    var socket = new SockJS('/webSocket');
    socket.onclose = function (){

    }
    stompClientPubl = Stomp.over(socket);

    stompClientPubl.connect({}, function () {
        stompClientPubl.subscribe('/sujet/reponsepublique', function (reponse) {
            manageMessages(reponse)
        });
    });
}

function connexionPrive() {
    var socket = new SockJS('/webSocket');
    stompClientPriv = Stomp.over(socket);

    stompClientPriv.connect({}, function () {
        stompClientPriv.subscribe('/sujet/reponseprive', function (reponse) {

            manageMessages(reponse)
        });
    });
}

function manageMessages(reponse){
    nbMessages.push(reponse);
    if(nbMessages.length > 10){
        nbMessages =  nbMessages.slice(1,11);
    }
    document.getElementById("reponsePublic").innerHTML = '';
    nbMessages.forEach(function(message){
        afficherReponse(message);
    })
}


function envoyerMessagePub() {
    var de = document.getElementById('nomUtil').innerText;

    stompClientPubl.send("/app/publicmsg", {}, JSON.stringify({'contenu':'Public : ' +  $("#contenu").val() ,
        'creationTemps': 0 , 'de' : de}));
    $("#contenu").val("");
}


function envoyerMessagePri() {
    var de = document.getElementById('nomUtil').innerText;

    stompClientPriv.send("/app/privatemsg", {}, JSON.stringify({'contenu': 'Privé : ' + $("#contenu").val() ,
        'creationTemps': 0 , 'de' : de}));
    $("#contenu").val("");
}

function afficherReponse(message) {

    $.ajax(
        {
            type: 'GET',
            url: '/userAvatar/' + JSON.parse(message.body).de,
            contentType: 'application/json',
            success: function (result) {

                var options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric', hour : 'numeric', minute :'numeric'};
                var date = new Date(JSON.parse(message.body).creationTemps);


                $("#reponsePublic").append("<li class='list-group-item'> " +
                    "<div class='pull-left hidden-xs'>" +
                        "<div>" +
                            "<img src='" + result + "' height='50px'>" +
                        "</div>" +
                    "</div>" +
                    "<small class='pull-right text-muted'>" + date.toLocaleDateString("fr-FR",options) + "</small>" +
                    "<div>" +
                    "<small class='list-group-item-heading text-muted text-primary'>" +
                    " &nbsp;&nbsp;&nbsp;" + JSON.parse(message.body).de +
                    "</small>" +
                    "<p>" +
                    "<span class='list-group-item-text'>" + JSON.parse(message.body).contenu + "</span>" +
                    "</p>" +
                    "</div>" +
                    "<br/>" +
                    "</li><br />");
            }
        });
}


