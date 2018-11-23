package yhshan.projet.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import yhshan.projet.dao.AvatarDao;
import yhshan.projet.dao.CompteDao;
import yhshan.projet.dao.GroupeDao;
import yhshan.projet.dao.RoleDao;
import yhshan.projet.entites.Compte;
import yhshan.projet.entites.Role;

import java.util.Optional;

@Service
public class MonUserDetailsService implements UserDetailsService {

    @Autowired
    private CompteDao compteDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private GroupeDao groupeDao;

    @Autowired
    private AvatarDao avatarDao;

    @Override
    public UserDetails loadUserByUsername(String nom) {
        Optional<Compte> compte = compteDao.findById(nom);

        Compte c = null;
        if (compte.isPresent())
            c = compte.get();
        else
        {
            //Mot de passe anonyme est JaimeLesPatates!!!
            //c = new Compte("anonyme", "$2a$10$v3EXvAFWCsyUNWmKaw6uI.TanoXeQ9h0zq4mxurfdSow.dvCvOovO",
            //        "anonyme",avatarDao.getOne("Tintin"), roleDao.getOne(2),groupeDao.getOne(3));
        }
        //System.out.println(c.toString());
        return new MonUserPrincipal(c);
    }
}
