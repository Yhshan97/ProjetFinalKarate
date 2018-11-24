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
        c = compte.orElseGet(Compte::new);
        //System.out.println(c.toString());
        return new MonUserPrincipal(c);
    }
}
