package yhshan.projet;

public class Message {

    private String de;
    private Long creationTemps;
    private String contenu;

    public Message() {
    }

    public Message(String de, Long creationTemps, String contenu) {
        this.de = de;
        this.creationTemps = creationTemps;
        this.contenu = contenu;
    }

    public String getDe() {
        return de;
    }

    public void setDe(String de) {
        this.de = de;
    }

    public Long getCreationTemps() {
        return creationTemps;
    }

    public void setCreationTemps(Long creationTemps) {
        this.creationTemps = creationTemps;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    @Override
    public String toString() {
        return "Message{" +
                "de='" + de + '\'' +
                ", creationTemps=" + creationTemps +
                ", contenu='" + contenu + '\'' +
                '}';
    }
}
