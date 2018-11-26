package yhshan.projet;

public class LieuxMessage {

    String courriel;
    String session;
    String position;
    boolean arbitre;

    public LieuxMessage() {
    }

    public LieuxMessage(String courriel, String session, String position, boolean arbitre) {
        this.courriel = courriel;
        this.session = session;
        this.position = position;
        this.arbitre = arbitre;
    }

    public String getCourriel() {
        return courriel;
    }

    public void setCourriel(String courriel) {
        this.courriel = courriel;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public boolean isArbitre() {
        return arbitre;
    }

    public void setArbitre(boolean arbitre) {
        this.arbitre = arbitre;
    }
}
