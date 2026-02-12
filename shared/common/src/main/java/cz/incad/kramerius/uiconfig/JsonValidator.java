package cz.incad.kramerius.uiconfig;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParseException;

import java.io.IOException;
import java.io.InputStream;

/**
 * JsonValidator
 * @author ppodsednik
 */
public class JsonValidator {

    private static final JsonFactory FACTORY = new JsonFactory();

    public void validate(InputStream in) throws IOException {
        try (JsonParser parser = FACTORY.createParser(in)) {
            while (parser.nextToken() != null) {
                // just consume tokens
            }
        } catch (JsonParseException e) {
            throw new InvalidJsonException("Invalid JSON", e);
        }
    }
}
