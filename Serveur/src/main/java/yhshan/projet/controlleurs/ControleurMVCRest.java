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
import yhshan.projet.LieuxMessage;
import yhshan.projet.Message;
import yhshan.projet.Reponse;
import yhshan.projet.configurations.MonUserPrincipal;
import yhshan.projet.dao.*;
import yhshan.projet.entites.Combat;
import yhshan.projet.entites.Compte;
import yhshan.projet.entites.Examen;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.*;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

@RestController
public class ControleurMVCRest {

    private final CompteDao compteDao;

    private final CombatDao combatDao;

    private final GroupeDao groupeDao;

    private final ExamenDao examenDao;

    private final RoleDao roleDao;

    private final SimpMessagingTemplate template;

    private boolean enCombat = false;

    private List<Compte> lstAilleurs = new ArrayList<>();
    private List<Compte> lstSpectateurs = new ArrayList<>();
    private List<Compte> lstAttente = new ArrayList<>();

    static public Map<String, String> listeDesConnexions = new HashMap();

    LinkedHashMap<String, String> lstPositions = new LinkedHashMap();

    private Compte compteGauche, compteDroite, compteArbitre;

    private int gaucheAttaque = -1, droiteAttaque = -1;

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


    /*
     *  List of users that are connected / list of their positions
     */

    @RequestMapping(value = "/lstComptes", method = RequestMethod.GET)
    public String listeComptes() {

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Compte.class, new CompteSerializer());
        mapper.registerModule(module);

        List<String> listeComptesJSON = new ArrayList<>();
        for (Compte user : compteDao.findAll()) {
            try {
                listeComptesJSON.add(mapper.writeValueAsString(user));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        this.template.convertAndSend("/sujet/lstLieux", lstPositions);
        return "{ comptes : [" + String.join(",", listeComptesJSON) + "] }";
    }

    @MessageMapping("/lieux")
    @SendTo("/sujet/lstLieux")
    public HashMap<String, String> lieux(LieuxMessage message) {

        String courriel = message.getCourriel();
        String session = message.getSession();
        String position = message.getPosition();
        boolean arbitre = message.isArbitre();

        if (position.equals("attente") && arbitre)
            position = "arbitre";

        if (listeDesConnexions.get(courriel) == null)
            return null;

        else if (listeDesConnexions.get(courriel).equals(session)) {
            lstPositions.put(courriel, position);
            combatLoop();
        }

        else  //La session est différente
            return null;

        return lstPositions;
    }

    @MessageMapping("/getLstComptes")
    @SendTo("/sujet/lstComptes")
    public String listeComptesWS() {
        return listeComptes();
    }


    /*
     *  Login / logout
     */


    @RequestMapping(value = "/login/{courriel}", method = RequestMethod.GET)
    public String login(@PathVariable("courriel") String courriel, HttpSession session) {

        if(!compteDao.findById(courriel).equals(Optional.empty())) {
            String str = listeDesConnexions.put(courriel, session.getId());
            lstPositions.put(courriel, "ailleurs");

            this.template.convertAndSend("/sujet/lstLieux", lstPositions);

            return session.getId();
        }
        return null;
    }

    @RequestMapping(value = "/logout/{sessionId}", method = RequestMethod.GET)
    public String logout(@PathVariable("sessionId") String sessionId) {
        System.out.println(listeDesConnexions.toString());
        boolean logout = false;
        if (listeDesConnexions.containsValue(sessionId)) {
            for (String key : listeDesConnexions.keySet()) {
                if (listeDesConnexions.get(key).equals(sessionId)) {
                    listeDesConnexions.remove(key);
                    lstPositions.remove(key);
                    logout = true;
                    break;
                }
            }
        }

        this.template.convertAndSend("/sujet/lstLieux", lstPositions);

        return logout ? "Logout OK" : "Déjà logged out";
    }


    /*
     *  Messages
     */


    @MessageMapping("/publicmsg")
    @SendTo("/sujet/reponsepublique")
    public Reponse publique(Message message) {
        Compte sender = compteDao.getOne(message.getDe());

        if (listeDesConnexions.get(message.getDe()) != null && sender != null) {
            return sender.getRole().getId() > 1 && listeDesConnexions.get(message.getDe()).equals(message.getSession()) ?
                    new Reponse(message.getDe(), new Date().getTime(), "public") : null;
        } else return null;
    }

    @MessageMapping("/privatemsg")
    @SendTo("/sujet/reponseprive")
    public Reponse prive(Message message) {
        if (listeDesConnexions.get(message.getDe()) != null) {
            return listeDesConnexions.get(message.getDe()).equals(message.getSession()) ?
                    new Reponse(message.getDe(), new Date().getTime(), "privé") : null;
        }
        return null;
    }


    /*
     *  Combat
     */

    private void combatLoop(){
        th = new Thread(() -> {
            while(!currentThread().isInterrupted()){
                try {
                    System.out.println("Inside loop 1");
                    //Send infos combat (who vs who / )
                    List<Compte> listCombattants = new ArrayList<>();
                    List<Compte> listArbitres = new ArrayList<>();

                    //Put comptes into their proper list
                    for (String compte : lstPositions.keySet()){
                        if(Objects.equals(lstPositions.get(compte), "attente")) // to verify
                            listCombattants.add(compteDao.findById(compte).get());

                        else if(Objects.equals(lstPositions.get(compte), "arbitre")) //to verify
                            listArbitres.add(compteDao.findById(compte).get());
                    }

                    if(listCombattants.size() < 2 || listArbitres.size() < 1)
                        currentThread().interrupt();

                    System.out.println("Inside loop 2");
                    sleep(5000);
                    returnInfoCombat(listCombattants,listArbitres); // choose fighters
                    System.out.println("Inside loop 3");
                    sleep(2000);
                    // randomise attack left and right and send
                    randomiseAttacks();
                    System.out.println("Inside loop 4");
                    sleep(2000);
                    // send winner (check if venerable then he auto wins..)
                    //save fight and reset variables
                    returnResultatCombat();

                    resetCombatState();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        if(listeDesConnexions.size() >= 3 && !th.isAlive() && th.isInterrupted())
            th.start();
    }

    private void randomiseAttacks(){
        if(compteGauche == null || compteDroite == null || compteArbitre == null)
            return;

        Random rand = new Random();
        //1 = rock, 2= paper, 3= scissors
        gaucheAttaque = rand.nextInt(3);
        droiteAttaque = rand.nextInt(3);

        String strJSONResultat =
                "{ \"attaqueGauche\" : \"" + gaucheAttaque + "\"," +
                        " \"attaqueDroite\" : \"" + droiteAttaque + "\" " + //," +
                       // " \"resultatCombat\" : \"" + resultat + "\"," +
                        //" \"nomGauche\" : \"" + compteGauche.getUsername() + "\"," +
                        //" \"nomDroite\" : \"" + compteDroite.getUsername() + "\"," +
                        //" \"nomArbitre\" : \"" + compteArbitre.getUsername() + "\"" +
                        "}";
        System.out.println(strJSONResultat);
        this.template.convertAndSend("/sujet/ChoixCombat",strJSONResultat);

    }


    private void returnInfoCombat(List<Compte> listCombattants, List<Compte> listArbitres) {


        if (listCombattants.size() >= 2 && listArbitres.size() >= 1 && !enCombat) {
            Random rand = new Random();

            int random = rand.nextInt(listCombattants.size());
            int random2 = rand.nextInt(listCombattants.size());
            int randomArbitre = rand.nextInt(listArbitres.size());
            while (random == random2)
                random2 = rand.nextInt(listCombattants.size());

            compteGauche = listCombattants.get(random);
            String nomGauche = compteGauche.getUsername();
            String avatarGauche = compteGauche.getAvatar().getAvatar();

            compteDroite = listCombattants.get(random2);
            String nomDroite = compteDroite.getUsername();
            String avatarDroite = compteDroite.getAvatar().getAvatar();

            compteArbitre = listArbitres.get(randomArbitre);
            String nomArbitre = compteArbitre.getUsername();
            String avatarArbitre = compteArbitre.getAvatar().getAvatar();

            enCombat = true;
            String strJSON = "{ \"gaucheNom\" : \"" + nomGauche + "\", \"gaucheAvatar\" : \"" + avatarGauche + "\", \"droiteNom\" : \"" + nomDroite + "\"," +
                    " \"droiteAvatar\" : \"" + avatarDroite + "\", \"arbitreNom\" : \"" + nomArbitre + "\", \"arbitreAvatar\" : \"" + avatarArbitre + "\"}";

            this.template.convertAndSend("/sujet/infoCombat", strJSON);
        }
    }

    private void returnResultatCombat(){
        if(compteGauche == null || compteDroite == null || compteArbitre == null)
            return;
        if(gaucheAttaque == -1 || droiteAttaque == -1)
            return;

        String result = "";
        int ptsGaucheGain = 0;
        int ptsDroiteGain = 0;
        int ptsArbitre = 1;

        //0 = rock, 1= paper, 2= scissors
        if ((gaucheAttaque + 1) % 3 == droiteAttaque)
            result =  "droite";
        else if ((droiteAttaque + 1) % 3 == gaucheAttaque)
            result = "gauche";
        else if(droiteAttaque == gaucheAttaque)
            result = "draw";

        //Venerable check
        if(compteGauche.getRole().getId() == 4)
            result = "gauche";
        else if(compteDroite.getRole().getId() == 4)
            result = "droite";

        if(result.equals("gauche"))
            ptsGaucheGain = 10;
        else if(result.equals("droite"))
            ptsDroiteGain = 10;
        else{
            ptsGaucheGain = 5;
            ptsDroiteGain = 5;
        }

        this.template.convertAndSend("/sujet/resultCombat", "{ result : \""+ result + "\" }");

        Long milli = new Date().getTime();
        //blanc = gauche
        //rouge = droite
        Combat combat = new Combat(milli, compteArbitre, compteDroite, compteGauche, compteDroite.getGroupe(),
                compteGauche.getGroupe(), ptsArbitre, ptsGaucheGain, ptsDroiteGain);
        combatDao.saveAndFlush(combat);
    }

    private void resetCombatState(){
        enCombat = false;

        compteArbitre = null;
        compteGauche = null;
        compteDroite = null;

        gaucheAttaque = -1;
        droiteAttaque = -1;

        String strJSON =
                "{\"gaucheNom\" : \""    + null + "\"," +
                " \"gaucheAvatar\" : \""  + null + "\"," +
                " \"droiteNom\" : \""     + null + "\"," +
                " \"droiteAvatar\" : \""  + null + "\"," +
                " \"arbitreNom\" : \""    + null + "\"," +
                " \"arbitreAvatar\" : \"" + null + "\"}";

        this.template.convertAndSend("/sujet/infoCombat",strJSON);
    }



//////////////////////////////////////////////////////////////////////////////////////////////////////// to be deleted section


    /*
     * Prototype ONLY functions
     */

    //@GetMapping("/")
    HashMap<String, String> uid() {
        System.out.println(lstPositions.toString());
        randomiseAttacks();
        returnResultatCombat();
        return null;
    }

    @RequestMapping(value = "/combat1/{courriel}/{session}", method = RequestMethod.GET)
    public String combat1(@PathVariable("session") String session, @PathVariable("courriel") String courriel) {
        if (listeDesConnexions.get(courriel) != null && listeDesConnexions.get(courriel).equals(session)) {
            Compte rouge = compteDao.getOne(courriel);
            Compte blanc = compteDao.getOne("s1@dojo");
            Compte arbitre = compteDao.getOne("v1@dojo");

            if (lstPositions.get(rouge.getUsername()).equals("attente")) {
                Long milli = new Date().getTime();
                Combat combat = new Combat(milli, arbitre, rouge, blanc, rouge.getGroupe(), blanc.getGroupe(), 1, 0, 10);
                combatDao.save(combat);
                this.template.convertAndSend("/sujet/MAJCompte", listeComptes());
                return "ok";
            } else return "Pas en attente";
        } else
            return "refusé";
    }

    @RequestMapping(value = "/combat2/{courriel}/{session}", method = RequestMethod.GET)
    public String combat2(@PathVariable("session") String session, @PathVariable("courriel") String courriel) {
        if (listeDesConnexions.get(courriel) != null && listeDesConnexions.get(courriel).equals(session)) {
            Compte rouge = compteDao.getOne(courriel);
            Compte blanc = compteDao.getOne("s1@dojo");
            Compte arbitre = compteDao.getOne("v1@dojo");

            if (lstPositions.get(rouge.getUsername()).equals("attente")) {
                Long milli = new Date().getTime();
                Combat combat = new Combat(milli, arbitre, rouge, blanc, rouge.getGroupe(), blanc.getGroupe(), 1, 10, 0);
                combatDao.save(combat);
                this.template.convertAndSend("/sujet/MAJCompte", listeComptes());
                return "ok";
            } else return "Pas en attente";
        } else
            return "refusé";
    }


    @RequestMapping(value = "/combat3/{courriel}/{session}", method = RequestMethod.GET)
    public String combat3(@PathVariable("session") String session, @PathVariable("courriel") String courriel) {
        if (listeDesConnexions.get(courriel) != null && listeDesConnexions.get(courriel).equals(session)) {
            Compte rouge = compteDao.getOne(courriel);
            Compte blanc = compteDao.getOne("s1@dojo");
            Compte arbitre = compteDao.getOne("v1@dojo");

            if (lstPositions.get(rouge.getUsername()).equals("attente")) {
                Long milli = new Date().getTime();
                Combat combat = new Combat(milli, arbitre, rouge, blanc, rouge.getGroupe(), blanc.getGroupe(), 1, 5, 5);
                combatDao.save(combat);
                this.template.convertAndSend("/sujet/MAJCompte", listeComptes());
                return "ok";
            } else return "Pas en attente";
        } else
            return "refusé";
    }


    @RequestMapping(value = "/arbitrer1/{courriel}/{session}", method = RequestMethod.GET)
    public String arbitrer1(@PathVariable("session") String session, @PathVariable("courriel") String courriel) {
        if (listeDesConnexions.get(courriel) != null && listeDesConnexions.get(courriel).equals(session)) {
            Compte rouge = compteDao.getOne("v1@dojo");
            Compte blanc = compteDao.getOne("s1@dojo");
            Compte arbitre = compteDao.getOne(courriel);

            Long milli = new Date().getTime();
            Combat combat = new Combat(milli, arbitre, rouge, blanc, rouge.getGroupe(), blanc.getGroupe(), 1, 0, 10);
            combatDao.save(combat);
            this.template.convertAndSend("/sujet/MAJCompte", listeComptes());
            return "ok";
        } else
            return "refusé";
    }

    @RequestMapping(value = "/arbitrer2/{courriel}/{session}", method = RequestMethod.GET)
    public String arbitrer2(@PathVariable("session") String session, @PathVariable("courriel") String courriel) {
        if (listeDesConnexions.get(courriel) != null && listeDesConnexions.get(courriel).equals(session)) {
            Compte rouge = compteDao.getOne("v1@dojo");
            Compte blanc = compteDao.getOne("s1@dojo");
            Compte arbitre = compteDao.getOne(courriel);

            Long milli = new Date().getTime();
            Combat combat = new Combat(milli, arbitre, rouge, blanc, rouge.getGroupe(), blanc.getGroupe(), 0, 10, 10);
            combatDao.save(combat);

            this.template.convertAndSend("/sujet/MAJCompte", listeComptes());
            return "ok";
        } else
            return "refusé";
    }

    @Transactional
    @RequestMapping(value = "/examen1/{courriel}/{session}", method = RequestMethod.GET)
    public String examen1(@PathVariable("session") String session, @PathVariable("courriel") String courriel) {
        if (listeDesConnexions.get(courriel) != null && listeDesConnexions.get(courriel).equals(session)) {
            Compte compteCourant = compteDao.getOne(courriel);
            Compte evaluateur = compteDao.getOne("v1@dojo");

            if (compteCourant.calculPoints() >= 100 && compteCourant.calculCredits() >= 10 && compteCourant.getGroupe().getId() < 8) {
                Long milli = new Date().getTime();
                Examen exam = new Examen(milli, true, compteCourant.getGroupe(), evaluateur, compteCourant);
                compteCourant.setGroupe(groupeDao.getOne(compteCourant.getGroupe().getId() + 1));
                examenDao.saveAndFlush(exam);
                compteDao.saveAndFlush(compteCourant);

                //this.template.convertAndSend("/sujet/MAJCompte", listeComptes());
                return "ok";
            } else return "Pas assez de points ou de crédits / Ceinture la plus haute";
        } else
            return "refusé";
    }

    @Transactional
    @RequestMapping(value = "/examen2/{courriel}/{session}", method = RequestMethod.GET)
    public String examen2(@PathVariable("session") String session, @PathVariable("courriel") String courriel) {
        if (listeDesConnexions.get(courriel) != null && listeDesConnexions.get(courriel).equals(session)) {
            Compte compteCourant = compteDao.getOne(courriel);
            Compte evaluateur = compteDao.getOne("v1@dojo");

            if (compteCourant.calculPoints() >= 100 && compteCourant.calculCredits() >= 10 && compteCourant.getGroupe().getId() < 8) {
                Long milli = new Date().getTime();
                Examen exam = new Examen(milli, false, compteCourant.getGroupe(), evaluateur, compteCourant);
                examenDao.saveAndFlush(exam);
                compteCourant.getEvalues().add(exam);
                compteDao.saveAndFlush(compteCourant);
                System.out.println("Credits: " + compteCourant.calculCredits());
                System.out.println("Points: " + compteCourant.calculPoints());
                this.template.convertAndSend("/sujet/MAJCompte", listeComptes());

                return "ok";
            } else return "Pas assez de points ou de crédits / Ceinture la plus haute";
        } else
            return "refusé";
    }

    @RequestMapping(value = "/passage/{courriel}/{session}", method = RequestMethod.GET)
    public String passage(@PathVariable("session") String session, @PathVariable("courriel") String courriel) {
        if (listeDesConnexions.get(courriel) != null && listeDesConnexions.get(courriel).equals(session)) {
            Compte compteCourant = compteDao.getOne(courriel);

            if (compteCourant.getArbitres().size() >= 30 && compteCourant.calculCredits() >= 10 && compteCourant.getRole().getId() == 1) {
                compteCourant.setRole(roleDao.getOne(2));
                compteDao.save(compteCourant);

                this.template.convertAndSend("/sujet/MAJCompte", listeComptes());
                return "ok";
            } else return "Pas assez de crédits ou de nombre de combats / Déjà ancien";
        } else
            return "refusé";
    }


    /*
     * END - Prototype ONLY functions
     */



    /*
     * Older functions
     */

/*
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

/*
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
*/


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
*/

}
