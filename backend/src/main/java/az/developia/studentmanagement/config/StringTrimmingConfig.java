package az.developia.studentmanagement.config;

import java.io.IOException;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

@JsonComponent
public class StringTrimmingConfig extends StdDeserializer<String> {

    private static final long serialVersionUID = 1L;

    public StringTrimmingConfig() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        String value = parser.getValueAsString();
        return (value == null) ? "" : value.trim();
    }
}