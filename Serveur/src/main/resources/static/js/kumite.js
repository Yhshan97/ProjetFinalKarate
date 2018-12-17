var stompClient = null;
var interval;

var counter = 10;
$(function () {
    var socket = new SockJS('/webSocket');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {

        stompClient.send('/app/getLstWeb');

        stompClient.subscribe('/sujet/lstComptesWeb', function (liste) {
            var listeComptes = JSON.parse(liste.body).comptes;

            $("#bodyArbitres,#bodyCombattants,#bodySpectateurs").text("");

            listeComptes.forEach(function (objet) {
                var position = objet.position;
                var avatar = objet.avatar;
                var strAppend = "<div class='pull-left' style='margin:2px'><img src=\"" + avatar + "\" style='width: 45px'></div>";

                if (position === "spectateur")
                    $("#bodySpectateurs").append(strAppend);
                else if (position === "attente")
                    $("#bodyCombattants").append(strAppend);
                else if (position === "arbitre")
                    $("#bodyArbitres").append(strAppend);
            })
        });

        stompClient.subscribe("/sujet/infoCombat", function (message) {
            var messageJSON = JSON.parse(message.body);

            $("#avatarGauche").attr("src" ,messageJSON.gaucheAvatar === "null" ? "images/noprofile.jpeg" : messageJSON.gaucheAvatar);
            $("#avatarArbitre").attr("src",messageJSON.arbitreAvatar=== "null" ? "images/noprofile.jpeg" : messageJSON.arbitreAvatar);
            $("#avatarDroite").attr("src" ,messageJSON.droiteAvatar === "null" ? "images/noprofile.jpeg" : messageJSON.droiteAvatar);


            $("#nomGauche").html(messageJSON.gaucheNom === "null" ? "<br/>" : messageJSON.gaucheNom);
            $("#nomDroite").html(messageJSON.droiteNom === "null" ? "<br/>" : messageJSON.droiteNom);
            $("#nomArbitre").html(messageJSON.arbitreNom === "null" ? "<br/>" : messageJSON.arbitreNom);


            $("#droiteAttaque, #gaucheAttaque").addClass("hidden");
            $("#droiteGagne,#gaucheGagne").addClass("hidden");
        });


        stompClient.subscribe("/sujet/ChoixCombat", function(message){

            var jsonObj = JSON.parse(message.body);

            //Convert 0 to rock, 1 to paper, 2 to scissors
            $("#droiteAttaque").attr("src","/images/" + returnAttaque(jsonObj.attaqueDroite) + ".jpg");
            $("#gaucheAttaque").attr("src","/images/" + returnAttaque(jsonObj.attaqueGauche) + ".jpg");

            if(jsonObj.attaqueGauche === "aucun")
                $("#gaucheAttaque").removeClass("flipped");
            else if(!$("#gaucheAttaque").hasClass('flipped'))
                $("#gaucheAttaque").addClass("flipped");

            $("#droiteAttaque, #gaucheAttaque").removeClass("hidden");
        });

        stompClient.subscribe("/sujet/resultCombat", function(message){
            var winner = JSON.parse(message.body).result;

            if(winner === "gauche"){
                $("#gaucheGagne").removeClass("hidden");
            }
            else if(winner === "droite"){
                $("#droiteGagne").removeClass("hidden");
            }
            else if(winner === "draw"){
                $("#droiteGagne,#gaucheGagne").removeClass("hidden");
            }
        });

        /*
        stompClient.subscribe("/sujet/resultatCombat",function(message){
            var jsonObj = JSON.parse(message.body);
            var gaucheAttaque = $("#gaucheAttaque");

            $("#droiteAttaque").attr("src","/images/" + jsonObj.attaqueDroite + ".jpg");
            gaucheAttaque.attr("src","/images/" + jsonObj.attaqueGauche + ".jpg");

            if(jsonObj.attaqueGauche === "aucun")
                gaucheAttaque.removeClass("flipped");
            else if(!gaucheAttaque.hasClass('flipped'))
                gaucheAttaque.addClass("flipped");

            $("#droiteAttaque, #gaucheAttaque").removeClass("hidden");

            if(jsonObj.resultatCombat === "gauche"){
                $("#gaucheGagne").removeClass("hidden");
                $("#headerArbitre").text("一本(Ippon) !");
            }
            else if(jsonObj.resultatCombat === "droite"){
                $("#droiteGagne").removeClass("hidden");
                $("#headerArbitre").text("一本(Ippon) !");
            }
            else if(jsonObj.resultatCombat === "draw" || jsonObj.resultatCombat === "perdants"){
                $("#droiteGagne,#gaucheGagne").removeClass("hidden");
                $("#headerArbitre").text(jsonObj.resultatCombat === "draw" ? "Match nul !" : "Les deux combattants perdent !");
            }

            if($("#nomUtil").val() === jsonObj.nomGauche && jsonObj.ptsGaucheGain !== 0){
                if(parseInt($("#spPoints").text()) + parseInt(jsonObj.ptsGaucheGain) >= 100)
                    $("#plusPts").text(" max").slideDown();
                    else
                    $("#plusPts").text(" +" + jsonObj.ptsGaucheGain).slideDown();
            }
            else if($("#nomUtil").val() === jsonObj.nomDroite && jsonObj.ptsDroiteGain !== 0){
                if(parseInt($("#spPoints").text()) + parseInt(jsonObj.ptsDroiteGain) >= 100)
                    $("#plusPts").text(" max").slideDown();
                else
                    $("#plusPts").text(" +" + jsonObj.ptsDroiteGain).slideDown();
            }
            else if($("#nomUtil").val() === jsonObj.nomArbitre ){
                    $("#plusCred").text(" +1").slideDown();

            }

            setTimeout(function() {
                $("#droiteAttaque, #gaucheAttaque").addClass("hidden");
                $("#droiteGagne,#gaucheGagne").addClass("hidden");

                $("#lblCiseaux,#lblRoche,#lblPapier").removeClass("active");
                $("#spanInfo").text("");
                $("#headerArbitre").text("");

                var nomUtilis = $("#nomUtil").val();

                if(nomUtilis === jsonObj.nomGauche && jsonObj.ptsGaucheGain !== 0){
                    $("#plusPts").fadeOut();
                    $("#plusPts").text("");
                    if(parseInt($("#spPoints").text()) + parseInt(jsonObj.ptsGaucheGain) <= 100)
                        $("#spPoints").text(parseInt($("#spPoints").text()) + parseInt(jsonObj.ptsGaucheGain));
                    else $("#spPoints").text(100);
                }
                else if(nomUtilis === jsonObj.nomDroite && jsonObj.ptsDroiteGain !== 0){
                    $("#plusPts").fadeOut();
                    $("#plusPts").text("");
                    if(parseInt($("#spPoints").text()) + parseInt(jsonObj.ptsDroiteGain) <= 100)
                        $("#spPoints").text(parseInt($("#spPoints").text()) + parseInt(jsonObj.ptsDroiteGain));
                    else $("#spPoints").text(100);
                }
                else if(nomUtilis === jsonObj.nomArbitre){
                    $("#plusCred").fadeOut();
                    $("#plusCred").text("");
                    $("#spCredits").text(parseInt($("#spCredits").text()) + 1);
                }
            }, 5000);
            $('div label').removeClass('disabled');

            counter = 10;
        });*/


    });
});

function returnAttaque(nombre){
    switch(nombre){
        case 0:
            return "roche";
        case 1:
            return "papier";
        case 2:
            return "ciseaux";
        default:
            return "aucun";
    }
}