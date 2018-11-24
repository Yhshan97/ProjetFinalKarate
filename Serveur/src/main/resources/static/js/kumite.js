var stompClient = null;
var interval;

var counter = 10;
$(function () {
    $('div label').click(function () {
        if($(this).hasClass('disabled') === false){
            $(this).addClass('active').siblings().removeClass('active');
        stompClient.send("/app/receive", {position: getPosition()}, $("#nomUtil").val());

        $(this).addClass('disabled').siblings().addClass('disabled');

        $("#spanInfo").delay(500).fadeIn();
        document.getElementById("spanInfo").innerHTML = ("Veuillez patienter ...");

        setTimeout(function() {
             if(!interval) {
                 $("#lblSpectateur, #lblCombattant, #lblArbitre").removeClass('disabled');
                 //$("#lblCombattant").removeClass('disabled');
                 //$("#lblArbitre").removeClass('disabled');
                 if (document.getElementById("spanInfo").innerHTML === ("Veuillez patienter ..."))
                     $("#spanInfo").fadeOut();
             }
        }, 5000);
        }
    });

    $('#lblRoche, #lblPapier, #lblCiseaux').on("click",function () {
        $(this).addClass('active').siblings().removeClass('active');
    });

    var socket = new SockJS('/webSocket');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {

        stompClient.send("/app/receive", {position: getPosition()}, $("#nomUtil").val());

        stompClient.subscribe("/sujet/keepConnected", function () {
            stompClient.send("/app/receive", {position: getPosition()}, $("#nomUtil").val());
        });


        stompClient.subscribe('/sujet/receiveList', function (liste) {
            var listeComptes = JSON.parse(liste.body);

            $("#bodyArbitres,#bodyCombattants,#bodySpectateurs").text("");

            listeComptes.forEach(function (compteJson) {
                var compte = JSON.parse(compteJson);
                var strAppend = "<div class='pull-left' style='margin:2px'><img src=\"" + compte.avatar + "\" style='width: 45px'></div>";

                if (compte.position === "spectateur")
                    $("#bodySpectateurs").append(strAppend);
                else if (compte.position === "combattant")
                    $("#bodyCombattants").append(strAppend);
                else if (compte.position === "arbitre")
                    $("#bodyArbitres").append(strAppend);

            })
        });

        stompClient.subscribe("/sujet/infoCombat", function (message) {

            var messageJSON = JSON.parse(message.body);
            var nomUtilis = $("#nomUtil").val();
            var spanInfo = $("#spanInfo");

            $("#avatarGauche").attr("src" ,messageJSON.gaucheAvatar === "null" ? "images/noprofile.jpeg" : messageJSON.gaucheAvatar);
            $("#avatarArbitre").attr("src",messageJSON.arbitreAvatar=== "null" ? "images/noprofile.jpeg" : messageJSON.arbitreAvatar);
            $("#avatarDroite").attr("src" ,messageJSON.droiteAvatar === "null" ? "images/noprofile.jpeg" : messageJSON.droiteAvatar);

            if(messageJSON.gaucheAvatar !== "null"){
                if(messageJSON.gaucheNom === nomUtilis){
                    spanInfo.text("Vous êtes à gauche..").fadeIn();
                    $('div label').addClass('disabled');
                }
                else if(messageJSON.droiteNom === nomUtilis){
                    spanInfo.text("Vous êtes à droite..").fadeIn();
                    $('div label').addClass('disabled');
                }
                else if(messageJSON.arbitreNom === nomUtilis) {
                    spanInfo.text("Vous êtes l'arbitre..").fadeIn();
                    $('div label').addClass('disabled');
                }

                if(counter === 10){
                interval = setInterval(function() {
                    $("#spanCount").text("Le combat commence dans " + counter + " secondes...").fadeIn();

                    if(counter === 5)
                        $("#headerArbitre").text("礼(Rei) !");

                    if (counter <= 0) {
                        $("#spanCount").text("");
                        $("#headerArbitre").text("はじめ(Hajime) !");

                        if(messageJSON.gaucheNom === nomUtilis){
                            $("#grpAttaque").delay(300).removeClass("hidden").fadeIn();
                            setTimeout(function(){
                                $("#grpAttaque").addClass("hidden").fadeOut();
                                stompClient.send("/app/receiveAttaque",{nomUtil : nomUtilis},getAttaque());
                            },5000);
                        }
                        else if(messageJSON.droiteNom === nomUtilis){
                            $("#grpAttaque").delay(300).removeClass("hidden").fadeIn();
                            setTimeout(function(){
                                $("#grpAttaque").addClass("hidden").fadeOut();
                                stompClient.send("/app/receiveAttaque",{nomUtil : nomUtilis},getAttaque());
                            },5000);
                        }
                        else if(messageJSON.arbitreNom === nomUtilis)
                            $("#grpArbitre").delay(300).removeClass("hidden").fadeIn();

                        clearInterval(interval);
                        interval = null;
                    }
                    counter--;
                }, 1000);
                }
            }
        });

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
        });
    });
});


function getPosition() {
    if ($("#lblSpectateur").hasClass("active")) {
        return "spectateur";
    }
    else if ($("#lblCombattant").hasClass("active")) {
        return "combattant";
    }
    else {
        return "arbitre";
    }
}

function getAttaque(){
    if ($("#lblRoche").hasClass("active"))
        return "roche";

    else if ($("#lblPapier").hasClass("active"))
        return "papier";

    else if($("#lblCiseaux").hasClass("active"))
        return "ciseaux";

    return "aucun";
}