package yhshan.projet.controlleurs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import yhshan.projet.configurations.MonUserPrincipal;
import yhshan.projet.dao.*;
import yhshan.projet.entites.Combat;
import yhshan.projet.entites.Compte;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Controller
public class ControleurMVC {

    private final CompteDao compteDao;

    private final AvatarDao avatarDao;

    private final RoleDao roleDao;

    private final CombatDao combatDao;

    private final GroupeDao groupeDao;

    @Autowired
    public ControleurMVC(CompteDao compteDao, AvatarDao avatarDao, RoleDao roleDao, CombatDao combatDao, GroupeDao groupeDao) {
        this.compteDao = compteDao;
        this.avatarDao = avatarDao;
        this.roleDao = roleDao;
        this.combatDao = combatDao;
        this.groupeDao = groupeDao;
    }


    @RequestMapping(value = "/")
    public String racine(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean userAuth = authentication.getPrincipal() instanceof MonUserPrincipal;
        MonUserPrincipal user = null;

        if(userAuth){
            user = (MonUserPrincipal) authentication.getPrincipal();
        }

        model.addAttribute("de",userAuth ? user.getUsername() : "Aucun");
        model.addAttribute("alias",userAuth ? user.getAlias() : "Visiteur");
        model.addAttribute("avatar",userAuth ? user.getAvatar() : null );
        model.addAttribute("role",userAuth ? user.getRole() : "aucun rôle");
        model.addAttribute("groupe",userAuth ? user.getGroupe() : "aucun");
        model.addAttribute("points",userAuth ? user.getPoints() : 0);
        model.addAttribute("credits",userAuth ? user.getCredits() : 0);
        return "public/index";
    }

    @RequestMapping(value = "/notreEcole", method = RequestMethod.GET)
    public String ecole(Model model) {
        model.addAttribute("listeRoles", roleDao.findAll());
        model.addAttribute("listeComptes",compteDao.findAll());
        return "public/notreEcole";
    }

    @RequestMapping(value = "/combats", method = RequestMethod.GET)
    public String kumite(Model model) {
        return "prive/kumite";
    }

    @RequestMapping(value = "/grades", method = RequestMethod.GET)
    public String grades(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean userAuth = authentication.getPrincipal() instanceof MonUserPrincipal;
        Compte connected = null;

        if(userAuth){
            MonUserPrincipal user = (MonUserPrincipal) authentication.getPrincipal();
            connected = compteDao.getOne(user.getUsername());
        }

        List<Compte> lstAdmissibles = new ArrayList<>();
        List<Compte> lstHonte = new ArrayList<>();

        for (Compte compte: compteDao.findAll()) {
            if (compte.getGroupe().getId() < 7 && compte.calculPoints() >= 100 && compte.calculCredits() >= 10) {
                if (!compte.isHonte())
                    lstAdmissibles.add(compte);
                else if (compte.isHonte())
                    lstHonte.add(compte);
            }
        }


        model.addAttribute("admissibles",lstAdmissibles);
        model.addAttribute("hontes",lstHonte);
        model.addAttribute("connectedUser",connected);

        return "prive/grades";
    }



    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Map<String, Object> model) { return "public/login"; }

    @RequestMapping(value = "/consoleBD", method = RequestMethod.GET)
    public String console(Map<String, Object> model) {
        return "consoleBD";
    }
}

