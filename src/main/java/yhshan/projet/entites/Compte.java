package yhshan.projet.entites;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name="COMPTES")
public class Compte implements UserDetails {
    private static final long serialVersionUID = 1L;
    @Id
    private String username;
    private String fullname;
    private String password;

    //Valeurs de 1 à 10. Utilisé lors des combats.
    private int talent;
    //Valeurs de 1 à 10. Utilisé lors des combats.
    private int entrainement;
    //Valeurs de 1 à 10. Utilisé lors des examens.
    private int chouchou;

    private Long anciendepuis;

    //Identification
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id")
    private Avatar avatar = new Avatar();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role = new Role();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupe_id")
    private Groupe groupe = new Groupe();

    //Combats
    @OneToMany(mappedBy = "rouge")
    private Set<Combat> rouges = new HashSet<>();

    @OneToMany(mappedBy = "blanc")
    private Set<Combat> blancs = new HashSet<>();

    @OneToMany(mappedBy = "arbitre")
    private Set<Combat> arbitres = new HashSet<>();

    //Examens
    @OneToMany(mappedBy = "evaluateur")
    private Set<Examen> evaluateurs = new HashSet<>();

    @OneToMany(mappedBy = "evalue")
    private Set<Examen> evalues = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}