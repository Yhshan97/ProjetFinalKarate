package yhshan.projet.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import yhshan.projet.dao.AvatarDao;
import yhshan.projet.entites.Compte;
import yhshan.projet.entites.Groupe;

import java.util.Collection;

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

}
