package yhshan.projet.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import yhshan.projet.dao.AvatarDao;
import yhshan.projet.entites.Combat;
import yhshan.projet.entites.Compte;

import java.util.Collection;
import java.util.Set;

public class MonUserPrincipal implements UserDetails {

    private Compte compte;

    @Autowired
    private AvatarDao avatarDao;

    public MonUserPrincipal(Compte compte) {
        if (compte != null)
            this.compte = compte;
        else
            this.compte = new Compte();
    }

    public Set<Combat> test(){ return compte.getBlancs(); }

    public int getPoints(){ return compte.calculPoints(); }

    public int getCredits(){ return compte.calculCredits(); }

    public String getAlias(){ return compte.getFullname(); }

    public String getAvatar(){ return compte.getAvatar().getAvatar();}

    public String getRole() { return compte.getRole().getRole();}

    public int getRole2() { return compte.getRole().getId();}

    public String getGroupe(){ return compte.getGroupe().getGroupe(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return compte.getAuthorities();
    }
    @Override
    public String getPassword() { return compte.getPassword();}
    @Override
    public String getUsername() { return compte.getUsername();}
    @Override
    public boolean isAccountNonExpired() { return compte.isAccountNonExpired();}
    @Override
    public boolean isAccountNonLocked() { return compte.isAccountNonLocked();}
    @Override
    public boolean isCredentialsNonExpired() { return compte.isCredentialsNonExpired(); }
    @Override
    public boolean isEnabled() { return compte.isEnabled(); }
    @Override
    public String toString() {
        return "Compte{" +
                "username='" + compte.getUsername() + '\'' +
                '}';
    }
}
