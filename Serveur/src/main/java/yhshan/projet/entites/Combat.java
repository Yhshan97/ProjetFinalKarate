package yhshan.projet.entites;

import javax.persistence.*;

@Entity
@Table(name="COMBATS")
public class Combat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    Long date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arbitre_id")
    private Compte arbitre = new Compte();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rouge_id")
    private Compte rouge = new Compte();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blanc_id")
    private Compte blanc = new Compte();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ceintureRouge_id")
    private Groupe ceintureRouge = new Groupe();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ceintureBlanc_id")
    private Groupe ceintureBlanc = new Groupe();

    int creditsArbitre;
    int pointsBlanc;
    int pointsRouge;

    public Combat() {
    }

    public Combat(Long date, Compte arbitre, Compte rouge, Compte blanc, Groupe ceintureRouge, Groupe ceintureBlanc, int creditsArbitre, int pointsBlanc, int pointsRouge) {
        this.date = date;
        this.arbitre = arbitre;
        this.rouge = rouge;
        this.blanc = blanc;
        this.ceintureRouge = ceintureRouge;
        this.ceintureBlanc = ceintureBlanc;
        this.creditsArbitre = creditsArbitre;
        this.pointsBlanc = pointsBlanc;
        this.pointsRouge = pointsRouge;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Compte getArbitre() {
        return arbitre;
    }

    public void setArbitre(Compte arbitre) {
        this.arbitre = arbitre;
    }

    public Compte getRouge() {
        return rouge;
    }

    public void setRouge(Compte rouge) {
        this.rouge = rouge;
    }

    public Compte getBlanc() {
        return blanc;
    }

    public void setBlanc(Compte blanc) {
        this.blanc = blanc;
    }

    public Groupe getCeintureRouge() {
        return ceintureRouge;
    }

    public void setCeintureRouge(Groupe ceintureRouge) {
        this.ceintureRouge = ceintureRouge;
    }

    public Groupe getCeintureBlanc() {
        return ceintureBlanc;
    }

    public void setCeintureBlanc(Groupe ceintureBlanc) {
        this.ceintureBlanc = ceintureBlanc;
    }

    public int getCreditsArbitre() {
        return creditsArbitre;
    }

    public void setCreditsArbitre(int creditsArbitre) {
        this.creditsArbitre = creditsArbitre;
    }

    public int getPointsBlanc() {
        return pointsBlanc;
    }

    public void setPointsBlanc(int pointsBlanc) {
        this.pointsBlanc = pointsBlanc;
    }

    public int getPointsRouge() {
        return pointsRouge;
    }

    public void setPointsRouge(int pointsRouge) {
        this.pointsRouge = pointsRouge;
    }

    @Override
    public String toString() {
        return "Combat{" +
                "id=" + id +
                ", date=" + date +
                ", arbitre=" + arbitre +
                ", rouge=" + rouge +
                ", blanc=" + blanc +
                ", ceintureRouge=" + ceintureRouge +
                ", ceintureBlanc=" + ceintureBlanc +
                ", creditsArbitre=" + creditsArbitre +
                ", pointsBlanc=" + pointsBlanc +
                ", pointsRouge=" + pointsRouge +
                '}';
    }
}