package yhshan.projet.controlleurs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import yhshan.projet.CompteSerializer;
import yhshan.projet.Message;
import yhshan.projet.Reponse;
import yhshan.projet.configurations.MonUserPrincipal;
import yhshan.projet.dao.*;
import yhshan.projet.entites.Combat;
import yhshan.projet.entites.Compte;

import javax.servlet.http.HttpSession;
import java.util.*;

import static java.lang.Thread.sleep;

@RestController
public class ControleurMVCRest {

    private final CompteDao compteDao;

    private final CombatDao combatDao;

    private final GroupeDao groupeDao;

    private final ExamenDao examenDao;

    private final RoleDao roleDao;

    private final SimpMessagingTemplate template;

    private List<Compte> listeConnected = new ArrayList<Compte>();

    private boolean enCombat = false;

    private Compte compteGauche,compteDroite,compteArbitre;

    private String gaucheAttaque ="",droiteAttaque ="";

    private int nbFois = 0;

    private Thread th;




    @Autowired
    public ControleurMVCRest(CompteDao compteDao, CombatDao combatDao, GroupeDao groupeDao, ExamenDao examenDao, RoleDao roleDao, SimpMessagingTemplate template) {
        this.compteDao = compteDao;
        this.combatDao = combatDao;
        this.groupeDao = groupeDao;
        this.examenDao = examenDao;
        this.roleDao = roleDao;
        this.template = template;
    }
    static public Map<String, String> listeDesConnexions = new HashMap();

    @RequestMapping(value= "/login/{courriel}", method = RequestMethod.GET)
    public String login(@PathVariable("courriel") String courriel, HttpSession session){

        String str = listeDesConnexions.put(courriel, session.getId());
        System.out.println(str);
        System.out.println(listeDesConnexions.toString());

        return str == null  ? "Login OK" : "Remplacé";
    }

    @RequestMapping(value= "/logout/{courriel}", method = RequestMethod.GET)
    public String logout(@PathVariable("courriel") String courriel,HttpSession session){
        System.out.println(listeDesConnexions.toString());
        return listeDesConnexions.remove(courriel,session.getId()) ? "Logout OK" : "Déjà logged out";
    }

    @RequestMapping(value="/lstComptes", method = RequestMethod.GET)
    public String listeComptes(){

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Compte.class, new CompteSerializer());
        mapper.registerModule(module);

        List<String> listeComptesJSON   =  new ArrayList<>();
        for (Compte user: compteDao.findAll()) {
            try {
                listeComptesJSON.add(mapper.writeValueAsString(user));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return "{ comptes : [" + String.join(",",listeComptesJSON) + "] }";
    }

    @GetMapping("/")
    String uid(HttpSession session, MonUserPrincipal user) {
        try {
            System.out.println(session.getId());
            System.out.println(user.getUsername());
        }catch (Exception e){
        }
        return session.getId();
    }

    @RequestMapping(value="/combat1/{courriel}", method= RequestMethod.GET)
    public Combat combat1(@PathVariable("courriel") String courriel){

        Compte rouge = compteDao.getOne(courriel);
        Compte blanc = compteDao.getOne("s1@dojo");
        Compte arbitre = compteDao.getOne("v1@dojo");

        Long milli = new Date().getTime();

        int pointsGagnant = getPointsBasedOnEcart(rouge.getGroupe().getId() - blanc.getGroupe().getId());

        Combat combat = new Combat(milli,arbitre,rouge,blanc,rouge.getGroupe(),blanc.getGroupe(),1,0,pointsGagnant);

        return combatDao.save(combat);
    }

    @RequestMapping(value="/combat2/{courriel}", method= RequestMethod.GET)
    public String combat2(@PathVariable("courriel") String courriel){

        return "";
    }

    @RequestMapping(value="/combat3/{courriel}", method= RequestMethod.GET)
    public String combat3(@PathVariable("courriel") String courriel){
        return "";
    }


    @MessageMapping("/publicmsg")
    @SendTo("/sujet/reponsepublique")
    public Reponse publique(Message message) {
        return new Reponse(message.getDe(), new Date().getTime(),"public");
        //return new Reponse(message.getDe(), new Date().getTime(),message.getContenu());
    }

    @MessageMapping("/privatemsg")
    @SendTo("/sujet/reponseprive")
    public Reponse prive(Message message) {
        return new Reponse(message.getDe(), new Date().getTime(),"privé");
        //return new Reponse(message.getDe(), new Date().getTime(),message.getContenu());
    }

    private int getPointsBasedOnEcart(int ecart){

        int points = 0;

        switch(ecart){
            case 0:points = 10; break;
            case 1:points = 12; break;
            case 2:points = 15; break;
            case 3:points = 20; break;
            case 4:points = 25; break;
            case 5:points = 30; break;
            case 6:points = 50; break;
            case -1:points = 9; break;
            case -2:points = 7; break;
            case -3:points = 5; break;
            case -4:points = 3; break;
            case -5:points = 2; break;
            case -6:points = 1; break;
        }
        return points;
    }
/*
    @RequestMapping(value = "/userAvatar/{id}", method = RequestMethod.GET)
    public String getAvatarUser(@PathVariable("id") String id){ return compteDao.getOne(id).getAvatar().getAvatar(); }

    @RequestMapping(value = "/passer/{id}", method = RequestMethod.GET)
    public void passerUtil(@PathVariable("id") String id,@AuthenticationPrincipal MonUserPrincipal compteLogged){

        Compte compte = compteDao.getOne(id);
        compteDao.passer(compte.getCredits() - 10,0,groupeDao.getOne(compte.getGroupe().getId() + 1),id);
        compteDao.couler(compte.getCredits() - 10,false,id);
        examenDao.save(new Examen(compte.getCourriel(),compteLogged.getUsername(),"Reussite"));
    }

    @RequestMapping(value = "/couler/{id}", method = RequestMethod.GET)
    public void coulerUtil(@PathVariable("id") String id,@AuthenticationPrincipal MonUserPrincipal compteLogged){

        Compte compte = compteDao.getOne(id);
        compteDao.couler(compte.getCredits() - 5,true,id);
        examenDao.save(new Examen(compte.getCourriel(),compteLogged.getUsername(),"Echec"));
    }

    @RequestMapping(value = "/transferrer/{id}", method = RequestMethod.GET)
    public void transferrerUtil(@PathVariable("id") String id){

        Compte compte = compteDao.getOne(id);

        if(compte.getRole().getNomRole().equals("NOUVEAU"))
            compteDao.setCompteCreditsById(compte.getCredits()-10, id);

        compteDao.transferrer(roleDao.getOne(compte.getRole().getIdRole()- 1) ,id);
    }

    @RequestMapping(value = "/enleverSensei/{id}", method = RequestMethod.GET)
    public void enleverSensei(@PathVariable("id") String id){

        Compte compte = compteDao.getOne(id);

        compteDao.transferrer(roleDao.getOne(compte.getRole().getIdRole() + 1),id);
    }


    //@MessageMapping("/connectedToKumite")
    @SendTo("/sujet/connect")
    private void connected(){

        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(Compte.class, new CompteSerializer());
        mapper.registerModule(module);

        th = new Thread(() -> {
            while(!currentThread().isInterrupted()){
                try {
                    listeConnected.clear();
                    this.template.convertAndSend("/sujet/keepConnected","");

                    sleep(5000);

                    if(listeConnected.size() == 0)
                        Thread.currentThread().interrupt();

                    try {
                        for (int x = 0; x < listeConnected.size(); x++)
                        for (int y = 0; y < listeConnected.size(); y++)
                        if (x != y)
                            if (listeConnected.get(x).getUsername().equals(listeConnected.get(y).getUsername())) {
                                listeConnected.get(x).setPosition("spectateur");
                                listeConnected.remove(y);
                                System.out.println("Le compte '" + listeConnected.get(x).getUsername() + "' est connecté deux fois !");
                            }
                    }catch(Exception e){System.out.println(e.getMessage());}

                    //Transform compte to objectJson
                    List<String> listeComptesJSON = new ArrayList<>();
                    for (Compte user: listeConnected)
                        listeComptesJSON.add(mapper.writeValueAsString(user));

                    this.template.convertAndSend("/sujet/receiveList",listeComptesJSON);

                    //Send infos combat (who vs who / )
                    returnInfoCombat();
                } catch (InterruptedException e) {
                    currentThread().interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void returnInfoCombat() {
        List<Compte> listCombattants = new ArrayList<>();
        List<Compte> listArbitres = new ArrayList<>();


        for (Compte compte : listeConnected){
            if(compte.getPosition().equals("combattant"))
                listCombattants.add(compte);

            else if(compte.getPosition().equals("arbitre"))
                listArbitres.add(compte);
        }

        if(listCombattants.size() >= 2 && listArbitres.size() >= 1 && !enCombat){
            Random rand = new Random();

            int random = rand.nextInt(listCombattants.size());
            int random2 = rand.nextInt(listCombattants.size());
            int randomArbitre = rand.nextInt(listArbitres.size());
            while(random == random2)
                random2 = rand.nextInt(listCombattants.size());

            compteGauche = compteDao.getOne(listCombattants.get(random).getUsername());
            String nomGauche = compteGauche.getUsername();
            String avatarGauche = compteGauche.getAvatar().getAvatar();

            compteDroite = listCombattants.get(random2);
            String nomDroite = compteDroite.getUsername();
            String avatarDroite = compteDroite.getAvatar().getAvatar();

            compteArbitre = listArbitres.get(randomArbitre);
            String nomArbitre = compteArbitre.getUsername();
            String avatarArbitre = compteArbitre.getAvatar().getAvatar();

            enCombat = true;
            String strJSON = "{ \"gaucheNom\" : \"" + nomGauche + "\", \"gaucheAvatar\" : \"" + avatarGauche +"\", \"droiteNom\" : \""+ nomDroite +"\"," +
                    " \"droiteAvatar\" : \""+avatarDroite+"\", \"arbitreNom\" : \""+nomArbitre+"\", \"arbitreAvatar\" : \""+avatarArbitre+"\"}";

            this.template.convertAndSend("/sujet/infoCombat",strJSON);
        }
    }


    @MessageMapping("/receiveAttaque")
    public void recoitAttaques(@Header String nomUtil, String attaque){

        Thread th2 = new Thread(() -> {
            try {
                sleep(2000);
                if(nbFois == 1) {
                    faireCombat(true);
                    nbFois = 0;
                }
                if(nbFois == 0){

                }
            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        });

        if(nomUtil.equals(compteDroite.getUsername())){
            droiteAttaque = attaque;
            nbFois++;
        }
        else if(nomUtil.equals(compteGauche.getUsername())){
            gaucheAttaque = attaque;
            nbFois++;
        }

        if(nbFois == 2){
            th2.interrupt();
            faireCombat(false);
            nbFois = 0;
        }else if(nbFois == 1){
            th2.start();
        }
    }

    private void resetCombatState(){

        enCombat = false;

        compteArbitre = null;
        compteGauche = null;
        compteDroite = null;

        gaucheAttaque ="";
        droiteAttaque ="";

        String strJSON =
                "{\"gaucheNom\" : \""    + null + "\"," +
                " \"gaucheAvatar\" : \""  + null + "\"," +
                " \"droiteNom\" : \""     + null + "\"," +
                " \"droiteAvatar\" : \""  + null + "\"," +
                " \"arbitreNom\" : \""    + null + "\"," +
                " \"arbitreAvatar\" : \"" + null + "\"}";

        this.template.convertAndSend("/sujet/infoCombat",strJSON);
    }

    private int gagne(String cote){

        int idGroupeGauche = compteGauche.getGroupe().getId();
        int idGroupeDroite = compteDroite.getGroupe().getId();
        int points = 0;
        if(cote.equals("g")) {
            int ecart = idGroupeDroite - idGroupeGauche;
            points = getPointsBasedOnEcart(ecart);


        }else if(cote.equals("d")){
            int ecart = idGroupeGauche - idGroupeDroite;
            points = getPointsBasedOnEcart(ecart);
            //compteDroite.setPoints(compteDroite.getPoints() + points > 100 ? 100 : compteDroite.getPoints() + points);
            //Compte x = compteDao.getOne(compteDroite.getUsername());
            //x.setPoints(compteDroite.getPoints() + points > 100 ? 100 : compteDroite.getPoints() + points);
            //compteDao.deleteById(compteDroite.getUsername());
            //compteDao.saveAndFlush(x);
            if(compteDroite.getPoints() + points < 100) {
             //   compteDroite.setPoints(compteDroite.getPoints() + points);
            }
            else {
             //   compteDroite.setPoints(100);
            }

            //System.out.println( compteDao.getOne(compteDroite.getUsername()).getPoints());
        }
        return points;
    }

    private int[] draw(){
        int idGroupeGauche = compteGauche.getGroupe().getId();
        int idGroupeDroite = compteDroite.getGroupe().getId();

        int ecart = idGroupeDroite - idGroupeGauche;
        int points = Math.round(getPointsBasedOnEcart(ecart) >> 1);
        //compteGauche.setPoints(compteGauche.getPoints() + points >= 100 ? 100 : compteGauche.getPoints() + points);
        ///// Check if pts <100

        int ecart1 = idGroupeGauche - idGroupeDroite;
        int points1 = Math.round(getPointsBasedOnEcart(ecart1) >> 1);
        //compteDroite.setPoints(compteDroite.getPoints() + points1 >= 100 ? 100 : compteDroite.getPoints() + points1);

        return new int[]{points,points1};
    }

    @MessageMapping("/receive")
    public void recoitMsg(@Header String position, String nomUtil) throws Exception {
        //Receive all msg

        if(th == null || th.isInterrupted()) {
            connected();
            th.start();
        }

        Compte compte1 = compteDao.getOne(nomUtil);

        boolean found = false;
        try {
            for (Compte UnCompte : listeConnected) {
                if (UnCompte.getUsername().equals(nomUtil)) {
                    UnCompte.setPosition(position);
                    found = true;
                }
            }
            if (!found) {
                compte1.setPosition(position);
                listeConnected.add(compte1);
            }
        }catch(Exception e){System.out.println(e.getMessage());}
    }


    private void faireCombat(boolean force){
        if((!droiteAttaque.equals("") && !gaucheAttaque.equals("")) || force){
            String resultat = "";
            int pointsGagneGauche = 0;
            int pointsGagneDroite = 0;

            //Si un des comptes est venerable = gagne
            if(compteGauche.getRole().getNomRole().equals("VENERABLE")) {
                pointsGagneGauche = gagne("g");
                resultat = "gauche";
            }
            else if(compteDroite.getRole().getNomRole().equals("VENERABLE")) {
                pointsGagneDroite = gagne("d");
                resultat = "droite";
            }

            //Si quelqu'un quitte
            else if(droiteAttaque.equals("") || gaucheAttaque.equals("")){
                if(gaucheAttaque.equals("") && droiteAttaque.equals("")) {
                    resultat = "perdants";
                    droiteAttaque = "aucun";
                    gaucheAttaque = "aucun";
                }
                else if(gaucheAttaque.equals("")){
                    System.out.println("gauche loses");
                    gaucheAttaque = "aucun";
                    pointsGagneDroite = gagne("d");
                    resultat = "droite";
                }
                else if (droiteAttaque.equals("")){
                    System.out.println("droite loses");
                    pointsGagneGauche = gagne("g");
                    droiteAttaque = "aucun";
                    resultat = "gauche";
                }
            }

            //Si au moins 1 n'a pas lancé d'attaque
            else if(gaucheAttaque.equals("aucun") || droiteAttaque.equals("aucun")) {
                if(gaucheAttaque.equals("aucun") && droiteAttaque.equals("aucun"))
                    resultat = "perdants";

                else if(gaucheAttaque.equals("aucun")) {
                    System.out.println("gauche loses");
                    pointsGagneDroite = gagne("d");
                    resultat = "droite";
                }
                else if (droiteAttaque.equals("aucun")){
                    System.out.println("droite loses");
                    pointsGagneGauche = gagne("g");
                    resultat = "gauche";
                }
            }
            else if(gaucheAttaque.equals(droiteAttaque)) {
                System.out.println("Draw");
                int[] pointsNul = draw();
                pointsGagneGauche = pointsNul[0];
                pointsGagneDroite = pointsNul[1];
                resultat = "draw";
            }
            else {
                switch(gaucheAttaque){
                    case "roche":
                        if(droiteAttaque.equals("ciseaux"))
                            pointsGagneGauche = gagne("g");
                        else pointsGagneDroite = gagne("d");
                        resultat = droiteAttaque.equals("ciseaux") ? "gauche":"droite";
                        System.out.println(droiteAttaque.equals("roche") ? "gauche wins":"droite wins");
                        break;
                    case "papier":
                        if(droiteAttaque.equals("roche"))
                            pointsGagneGauche = gagne("g");
                        else pointsGagneDroite = gagne("d");

                        resultat = droiteAttaque.equals("roche") ? "gauche":"droite";
                        System.out.println(droiteAttaque.equals("roche") ? "gauche wins":"droite wins");
                        break;
                    case "ciseaux":
                        if(droiteAttaque.equals("papier"))
                            pointsGagneGauche = gagne("g");
                        else pointsGagneDroite = gagne("d");
                        resultat = droiteAttaque.equals("papier") ? "gauche":"droite";
                        System.out.println(droiteAttaque.equals("papier") ? "gauche wins":"droite wins");
                        break;
                }
            }

            compteDao.setComptePtsById(compteGauche.getPoints() + pointsGagneGauche >= 100 ?
                    100 : compteGauche.getPoints() + pointsGagneGauche ,compteGauche.getCourriel());
            compteDao.setComptePtsById(compteDroite.getPoints() + pointsGagneDroite >= 100 ?
                    100 : compteDroite.getPoints() + pointsGagneDroite ,compteDroite.getCourriel());

            compteDao.setCompteCreditsById(compteArbitre.getCredits() + 1,compteArbitre.getCourriel());


            combatDao.save(new Combat(compteGauche.getCourriel(),compteDroite.getCourriel(),compteArbitre.getCourriel(),resultat));

            String strJSONResultat =
                    "{ \"attaqueGauche\" : \"" + gaucheAttaque + "\"," +
                            " \"attaqueDroite\" : \"" + droiteAttaque + "\"," +
                            " \"resultatCombat\" : \"" + resultat + "\"," +
                            " \"ptsGaucheGain\" : " + pointsGagneGauche + "," +
                            " \"ptsDroiteGain\" : " + pointsGagneDroite + "," +
                            " \"nomGauche\" : \"" + compteGauche.getUsername() + "\"," +
                            " \"nomDroite\" : \"" + compteDroite.getUsername() + "\"," +
                            " \"nomArbitre\" : \"" + compteArbitre.getUsername() + "\"}";
            //System.out.println(strJSONResultat);

            this.template.convertAndSend("/sujet/resultatCombat",strJSONResultat);
            new Thread(() -> {
                try {
                    sleep(5000);

                    resetCombatState();
                } catch (InterruptedException e) {
                    currentThread().interrupt();
                }
            }).start();
        }
    }

*/



}
