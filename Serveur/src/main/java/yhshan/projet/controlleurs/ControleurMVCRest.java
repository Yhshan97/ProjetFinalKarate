package yhshan.projet.controlleurs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
            listeComptesWeb();
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

    @MessageMapping("/getLstWeb")
    public void listeComptesWeb() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Compte.class, new CompteSerializer());
        mapper.registerModule(module);

        List<String> listeComptesJSON = new ArrayList<>();
        for (String username : listeDesConnexions.keySet()) {
            Optional<Compte> compte = compteDao.findById(username);
            String infoCompte = "{ \"avatar\" : \"" + compte.get().getAvatar().getAvatar() +
                    "\", \"position\" : \"" + lstPositions.get(username) + "\" }";
            listeComptesJSON.add(infoCompte);
        }
        this.template.convertAndSend("/sujet/lstComptesWeb", "{ \"comptes\" : [" + String.join(",", listeComptesJSON) + "] }");
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
        listeComptesWeb();
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

                    if(listCombattants.size() < 2 || listArbitres.size() < 1) {
                        currentThread().interrupt();
                        break;
                    }

                    System.out.println("Inside loop 2");

                    sleep(1000);
                    returnInfoCombat(listCombattants,listArbitres); // choose fighters
                    System.out.println("Inside loop 3");

                    sleep(2000);
                    // randomise attack left and right and send
                    randomiseAttacks();
                    System.out.println("Inside loop 4");

                    sleep(2000);
                    // send winner (check if venerable then he auto wins..)
                    returnResultatCombat();

                    sleep(2000);
                    //save fight and reset variables
                    resetCombatState();

                    listeComptesWeb();

                    this.template.convertAndSend("/sujet/lstComptes",listeComptes());

                    sleep(2000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        if(listeDesConnexions.size() >= 3)
            th.start();
    }

    private void randomiseAttacks(){
        if(compteGauche == null || compteDroite == null || compteArbitre == null)
            return;

        Random rand = new Random();
        //0 = rock, 1= paper, 2= scissors
        gaucheAttaque = rand.nextInt(3);
        droiteAttaque = rand.nextInt(3);

        String strJSONResultat = "{ \"attaqueGauche\" : " + gaucheAttaque + "," + " \"attaqueDroite\" : " + droiteAttaque + "}";

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

        this.template.convertAndSend("/sujet/resultCombat", "{ \"result\" : \""+ result + "\" }");

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

    /*
     *  Passage de grades
     */

    @RequestMapping(value = "/passer/{id}", method = RequestMethod.GET)
    public void passerUtil(@PathVariable("id") String id,@AuthenticationPrincipal MonUserPrincipal compteLogged){

        if(compteLogged != null && (compteLogged.getRole2() == 3 || compteLogged.getRole2() == 4)) {
            Compte compteCourant = compteDao.getOne(id);
            Compte evaluateur = compteDao.getOne(compteLogged.getUsername());

            Long milli = new Date().getTime();
            Examen exam = new Examen(milli, true, compteCourant.getGroupe(), evaluateur, compteCourant);
            compteCourant.setGroupe(groupeDao.getOne(compteCourant.getGroupe().getId() + 1));
            examenDao.saveAndFlush(exam);
            compteDao.saveAndFlush(compteCourant);
        }
    }

    @RequestMapping(value = "/couler/{id}", method = RequestMethod.GET)
    public void coulerUtil(@PathVariable("id") String id,@AuthenticationPrincipal MonUserPrincipal compteLogged){

        if(compteLogged != null && (compteLogged.getRole2() == 3 || compteLogged.getRole2() == 4)) {
            Compte compteCourant = compteDao.getOne(id);
            Compte evaluateur = compteDao.getOne(compteLogged.getUsername());

            Long milli = new Date().getTime();
            Examen exam = new Examen(milli, false, compteCourant.getGroupe(), evaluateur, compteCourant);
            examenDao.saveAndFlush(exam);
            compteDao.saveAndFlush(compteCourant);
        }
    }


    @RequestMapping(value = "/lstExamens/{courriel}/{sessionid}", method = RequestMethod.GET)
    public String historique(@PathVariable("courriel") String username,@PathVariable("sessionid") String sessionid){

        if(listeDesConnexions.get(username) != null && listeDesConnexions.get(username).equals(sessionid)){
            boolean compteExiste = compteDao.findById(username).isPresent();
            if(!compteExiste) return null;

            Compte compte = compteDao.findById(username).get();
            LinkedHashMap<Examen,ArrayList<Combat>> lstExamens = new LinkedHashMap<>();

            Date ancienDepuis = new Date(compte.getAnciendepuis());
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String tout = String.format("Historique du membre : %s ANCIEN : %s \n",username,df.format(ancienDepuis));


            int points = 0;
            int credits = 0;

            long dateStart = 0;
            for(Examen exam : examenDao.findAllByEvalueOrderByDateAsc(compte)){

                long dateStop = exam.getDate();

                ArrayList<Combat> lstCombatsPourExamen = new ArrayList<>();

                List<Combat> combatsByDate = combatDao.findAllByDateBetweenOrderByDateAsc(dateStart,dateStop);

                tout += String.format("\nExamen : %s, Points : %d, Crédits : %d, Ceinture %s ,Réussi : %s \n",
                        df.format(new Date(exam.getDate())),
                        points,
                        credits,
                        exam.getEvalue().getGroupe().getGroupe(),
                        exam.getaReussi().toString());

                points = 0;
                credits = 0;

                tout += "\nCombats\n";
                tout += String.format("|%-25s|%-10s|%-7s|%-10s|%-10s|%-6s|%-10s|%-10s|%-6s|\n",
                        "Date",
                        "Arbitre",
                        "Crédits",
                        "Rouge",
                        "Ceinture",
                        "Points",
                        "Blanc",
                        "Ceinture",
                        "Points"
                        );

                for (Combat combat: combatsByDate) {
                    //If hes arbitre
                    if(combat.getArbitre().getUsername().equals(username)){
                        credits += combat.getCreditsArbitre();
                    }

                    //Scale points based on group
                    //Cote rouge
                    if(combat.getRouge().getUsername().equals(username) && combat.getPointsRouge() != 0) {
                        int ecart = combat.getCeintureBlanc().getId() - combat.getCeintureRouge().getId();
                        int ptsGagne = compte.getPointsBasedOnEcart(ecart); // assumes he wins 10 pts

                        if (combat.getPointsRouge() == 5) { // else divide by 2
                            ptsGagne = ptsGagne >> 1;
                        }
                        combat.setPointsRouge(ptsGagne);
                        points += ptsGagne;
                    }

                    //Cote blanc

                    if(combat.getBlanc().getUsername().equals(username) && combat.getPointsBlanc() != 0){
                        int ecart = combat.getCeintureRouge().getId() - combat.getCeintureBlanc().getId();
                        int ptsGagne = compte.getPointsBasedOnEcart(ecart); // assumes he wins 10 pts

                        if(combat.getPointsBlanc() == 5) { // else divide by 2
                            ptsGagne = ptsGagne >> 1;
                        }
                        combat.setPointsBlanc(ptsGagne);
                        points += ptsGagne;
                    }

                    if(combat.getPointsRouge() != 0) {
                        int ecart = combat.getCeintureBlanc().getId() - combat.getCeintureRouge().getId();
                        int ptsGagne = compte.getPointsBasedOnEcart(ecart); // assumes he wins 10 pts

                        if (combat.getPointsRouge() == 5) { // else divide by 2
                            ptsGagne = ptsGagne >> 1;
                        }
                        combat.setPointsRouge(ptsGagne);
                    }

                    //Cote blanc

                    if( combat.getPointsBlanc() != 0){
                        int ecart = combat.getCeintureRouge().getId() - combat.getCeintureBlanc().getId();
                        int ptsGagne = compte.getPointsBasedOnEcart(ecart); // assumes he wins 10 pts

                        if(combat.getPointsBlanc() == 5) { // else divide by 2
                            ptsGagne = ptsGagne >> 1;
                        }
                        combat.setPointsBlanc(ptsGagne);
                    }



                    if(combat.getArbitre().getUsername().equals(username) ||
                            combat.getRouge().getUsername().equals(username) ||
                            combat.getBlanc().getUsername().equals(username)) {

                        tout += String.format("|%-25s|%-10s|%-7d|%-10s|%-10s|%-6d|%-10s|%-10s|%-6d|\n",
                                df.format(new Date(combat.getDate())),          //Date
                                combat.getArbitre().getUsername(),              //Arbitre
                                combat.getCreditsArbitre(),                     //Credits
                                combat.getRouge().getUsername(),                //Rouge
                                combat.getRouge().getGroupe().getGroupe(),      //Ceinture
                                combat.getPointsRouge(),                        //Points
                                combat.getBlanc().getUsername(),                //Blanc
                                combat.getBlanc().getGroupe().getGroupe(),      //Ceinture
                                combat.getPointsBlanc()                         //Points
                                );


                        lstCombatsPourExamen.add(combat);
                    }

                }
                lstExamens.put(exam,lstCombatsPourExamen);

                dateStart = dateStop;

            }
            System.out.println(tout);
            return tout;
        }

        return null;
    }

}
