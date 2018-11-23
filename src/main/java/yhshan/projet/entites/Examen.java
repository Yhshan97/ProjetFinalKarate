package yhshan.projet.entites;

import javax.persistence.*;

@Entity
@Table(name="EXAMENS")
public class Examen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    Long date;
    Boolean aReussi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ceinture_id")
    private Groupe ceinture = new Groupe();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluateur_id")
    private Compte evaluateur = new Compte();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evalue_id")
    private Compte evalue = new Compte();

    public Examen(Long date, Boolean aReussi, Groupe ceinture, Compte evaluateur, Compte evalue) {
        this.date = date;
        this.aReussi = aReussi;
        this.ceinture = ceinture;
        this.evaluateur = evaluateur;
        this.evalue = evalue;
    }

    public Examen() {
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

    public Boolean getaReussi() {
        return aReussi;
    }

    public void setaReussi(Boolean aReussi) {
        this.aReussi = aReussi;
    }

    public Groupe getCeinture() {
        return ceinture;
    }

    public void setCeinture(Groupe ceinture) {
        this.ceinture = ceinture;
    }

    public Compte getEvaluateur() {
        return evaluateur;
    }

    public void setEvaluateur(Compte evaluateur) {
        this.evaluateur = evaluateur;
    }

    public Compte getEvalue() {
        return evalue;
    }

    public void setEvalue(Compte evalue) {
        this.evalue = evalue;
    }

    @Override
    public String toString() {
        return "Examen{" +
                "id=" + id +
                ", date=" + date +
                ", aReussi=" + aReussi +
                ", ceinture=" + ceinture +
                ", evaluateur=" + evaluateur +
                ", evalue=" + evalue +
                '}';
    }
}