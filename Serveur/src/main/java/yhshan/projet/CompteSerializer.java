package yhshan.projet;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import yhshan.projet.entites.Compte;

import java.io.IOException;

public class CompteSerializer extends StdSerializer<Compte> {

    public CompteSerializer() {
        this(null);
    }

    public CompteSerializer(Class<Compte> t) {
        super(t);
    }

    @Override
    public void serialize(
            Compte value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeStartObject();
        jgen.writeStringField("courriel", value.getUsername());
        jgen.writeStringField("fullName", value.getFullname());
        jgen.writeStringField("avatar", value.getAvatar().getNom());
        jgen.writeStringField("role", value.getRole().getRole());
        jgen.writeStringField("groupe", value.getGroupe().getGroupe());
        jgen.writeNumberField("talent", value.getTalent());
        jgen.writeNumberField("chouchou", value.getChouchou());
        jgen.writeNumberField("ancienDepuis", value.getAnciendepuis());
        jgen.writeNumberField("entrainement", value.getEntrainement());
        jgen.writeEndObject();
    }
}