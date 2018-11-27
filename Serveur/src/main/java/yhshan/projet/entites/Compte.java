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

    public Compte() {
    }

    public Compte(String username, String fullname, String password, int talent, int entrainement, int chouchou, Long anciendepuis, Avatar avatar, Role role, Groupe groupe, Set<Combat> rouges, Set<Combat> blancs, Set<Combat> arbitres, Set<Examen> evaluateurs, Set<Examen> evalues) {
        this.username = username;
        this.fullname = fullname;
        this.password = password;
        this.talent = talent;
        this.entrainement = entrainement;
        this.chouchou = chouchou;
        this.anciendepuis = anciendepuis;
        this.avatar = avatar;
        this.role = role;
        this.groupe = groupe;
        this.rouges = rouges;
        this.blancs = blancs;
        this.arbitres = arbitres;
        this.evaluateurs = evaluateurs;
        this.evalues = evalues;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTalent() {
        return talent;
    }

    public void setTalent(int talent) {
        this.talent = talent;
    }

    public int getEntrainement() {
        return entrainement;
    }

    public void setEntrainement(int entrainement) {
        this.entrainement = entrainement;
    }

    public int getChouchou() {
        return chouchou;
    }

    public void setChouchou(int chouchou) {
        this.chouchou = chouchou;
    }

    public Long getAnciendepuis() {
        return anciendepuis;
    }

    public void setAnciendepuis(Long anciendepuis) {
        this.anciendepuis = anciendepuis;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Groupe getGroupe() {
        return groupe;
    }

    public void setGroupe(Groupe groupe) {
        this.groupe = groupe;
    }

    public Set<Combat> getRouges() {
        return rouges;
    }

    public void setRouges(Set<Combat> rouges) {
        this.rouges = rouges;
    }

    public Set<Combat> getBlancs() {
        return blancs;
    }

    public void setBlancs(Set<Combat> blancs) {
        this.blancs = blancs;
    }

    public Set<Combat> getArbitres() {
        return arbitres;
    }

    public void setArbitres(Set<Combat> arbitres) {
        this.arbitres = arbitres;
    }

    public Set<Examen> getEvaluateurs() {
        return evaluateurs;
    }

    public void setEvaluateurs(Set<Examen> evaluateurs) {
        this.evaluateurs = evaluateurs;
    }

    public Set<Examen> getEvalues() {
        return evalues;
    }

    public void setEvalues(Set<Examen> evalues) {
        this.evalues = evalues;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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

    @Override
    public String toString() {
        return "Compte{" +
                "username='" + username + '\'' +
                ", fullname='" + fullname + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public int calculPoints(){
        int ptsCourant = 0;
        Set<Combat> lst = blancs;
        for (Combat comb: lst) {
            if(comb.getCeintureBlanc() == groupe)
                ptsCourant += comb.getPointsBlanc();
        }
        lst = rouges;
        for (Combat comb: lst) {
            if(comb.getCeintureRouge() == groupe)
                ptsCourant += comb.getPointsRouge();
        }

        return ptsCourant;
    }

    public int calculCredits(){

        int credits = 0;
        Set<Combat> lst = arbitres;
        for (Combat comb: lst) {
            credits += comb.getCreditsArbitre();
        }

        for(Examen exam : evalues){
            if(credits >=10)
                credits -= exam.aReussi? 10 : 0;
        }

        return credits;
    }

}