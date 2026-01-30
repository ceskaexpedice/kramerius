package cz.incad.kramerius.uiconfig;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

class JsonValidatorTest {

    JsonValidator validator = new JsonValidator();

    @Test
    void rejectsInvalidJson() {
        String broken = "{ \"a\": 1 ";

        assertThrows(InvalidJsonException.class, () ->
                validator.validate(
                        new ByteArrayInputStream(broken.getBytes())
                )
        );
    }

    @Test
    void acceptsValidJson() throws Exception {
        String ok = "{ \"a\": 1, \"b\": [true, false] }";

        assertDoesNotThrow(() ->
                validator.validate(
                        new ByteArrayInputStream(ok.getBytes())
                )
        );
    }
}
