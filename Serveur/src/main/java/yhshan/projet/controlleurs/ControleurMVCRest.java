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

    private String gaucheAttaque = "", droiteAttaque = "";

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

    @Transactional
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

    @RequestMapping(value = "/login/{courriel}", method = RequestMethod.GET)
    public String login(@PathVariable("courriel") String courriel, HttpSession session) {

        if(!compteDao.findById(courriel).equals(Optional.empty())) {
            String str = listeDesConnexions.put(courriel, session.getId());
            System.out.println(str);
            System.out.println(listeDesConnexions.toString());
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
                System.out.println(key);
                if (listeDesConnexions.get(key).equals(sessionId)) {
                    System.out.println("yes");
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

    @GetMapping("/")
    HashMap<String, String> uid() {
        System.out.println(lstPositions.toString());
        return null;
    }

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
        //return new Reponse(message.getDe(), new Date().getTime(),"privé");

        if (listeDesConnexions.get(message.getDe()) != null) {
            return listeDesConnexions.get(message.getDe()).equals(message.getSession()) ?
                    new Reponse(message.getDe(), new Date().getTime(), "privé") : null;
        }

        return null;
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

        else if (listeDesConnexions.get(courriel).equals(session))
            lstPositions.put(courriel, position);

        else  //La session est différente
            return null;

        return lstPositions;
    }

    @MessageMapping("/getLstComptes")
    @SendTo("/sujet/lstComptes")
    public String listeComptesWS() {
        return listeComptes();
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

    @MessageMapping("/connectedToKumite")
    @SendTo("/sujet/connect")
    private void connected(){

        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(Compte.class, new CompteSerializer());
        mapper.registerModule(module);

        if(listeDesConnexions.size() == 3)
        th = new Thread(() -> {
            while(!currentThread().isInterrupted()){
                try {
                    //listeConnected.clear();
                    //this.template.convertAndSend("/sujet/keepConnected","");

                    sleep(5000);

                    /*
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
                    */
                    //Transform compte to objectJson
                    //List<String> listeComptesJSON = new ArrayList<>();
                    //for (Compte user: listeConnected)
                    //    listeComptesJSON.add(mapper.writeValueAsString(user));

                    if(listeDesConnexions.size() < 3)
                        currentThread().interrupt();

                    //this.template.convertAndSend("/sujet/receiveList",listeComptesJSON);
                    //Send infos combat (who vs who / )

                    returnInfoCombat();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void returnInfoCombat() {
        List<Compte> listCombattants = new ArrayList<>();
        List<Compte> listArbitres = new ArrayList<>();

        //Put comptes into their proper list
        for (String compte : lstPositions.keySet()){
            if(Objects.equals(lstPositions.get(compte), "attente")) // to verify
                listCombattants.add(compteDao.findById(compte).get());

            else if(Objects.equals(lstPositions.get(compte), "arbitre")) //to verify
                listArbitres.add(compteDao.findById(compte).get());
        }

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
