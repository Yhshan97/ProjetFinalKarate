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
/*
        jgen.writeStartObject();
        jgen.writeStringField("courriel", value.getCourriel());
        jgen.writeStringField("alias", value.getAlias());
        jgen.writeStringField("avatar", value.getAvatar().getAvatar());
        jgen.writeStringField("role", value.getRole().getNomRole());
        jgen.writeStringField("groupe", value.getGroupe().getNomGroupe());
        jgen.writeStringField("position", value.getPosition());
        jgen.writeEndObject();*/
    }
}