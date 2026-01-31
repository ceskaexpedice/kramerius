package cz.incad.kramerius.uiconfig;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonValidatorTest {

    JsonValidator validator = new JsonValidator();

    @Test
    public void rejectsInvalidJson() {
        String broken = "{ \"a\": 1 ";
        assertThrows(InvalidJsonException.class, () ->
                validator.validate(new ByteArrayInputStream(broken.getBytes())));
    }

    @Test
    public void acceptsValidJson() {
        String ok = "{ \"a\": 1, \"b\": [true, false] }";
        assertDoesNotThrow(() ->
                validator.validate(new ByteArrayInputStream(ok.getBytes())));
    }
}
