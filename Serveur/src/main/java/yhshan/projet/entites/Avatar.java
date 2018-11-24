package yhshan.projet.entites;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name="AVATAR")
public class Avatar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nom;
    //@Size(max = 21844)
    @Lob
    @Column(name = "AVATAR", length = 32767)
    private String avatar;

    @OneToOne(mappedBy = "avatar")
    private Compte compte = null;

    public Avatar(String nom, String avatar, Compte compte) {
        this.nom = nom;
        this.avatar = avatar;
        this.compte = compte;
    }

    public Avatar() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Compte getCompte() {
        return compte;
    }

    public void setCompte(Compte compte) {
        this.compte = compte;
    }

    @Override
    public String toString() {
        return "Avatar{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", avatar='" + avatar + '\'' +
                ", compte=" + compte +
                '}';
    }
}