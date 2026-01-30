package cz.incad.kramerius.uiconfig;

import java.io.*;

public class UIConfigService {

    private final UIConfigStore store;
    private final JsonValidator validator;

    public UIConfigService(UIConfigStore store, JsonValidator validator) {
        this.store = store;
        this.validator = validator;
    }

    public void save(UIConfigType type, InputStream json) throws IOException {
        byte[] data = json.readAllBytes();

        // validate
        validator.validate(new ByteArrayInputStream(data));

        // persist
        store.save(type, new ByteArrayInputStream(data));
    }

    public InputStream load(UIConfigType type) throws IOException {
        return store.load(type);
    }

    public boolean exists(UIConfigType type) {
        return store.exists(type);
    }
}
