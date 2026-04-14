package cz.inovatika.dochub;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface UserContentSpace {


    public String storeBundle(InputStream is, String user, String pid, DocumentType type, String auditInfo) throws IOException;

    public Optional<InputStream> getBundle(String token, String user, DocumentType type) throws UsageException, IOException;

    public String getToken(String pid, String user);

    public boolean exists(String token);

    public void deleteBundle(String token) throws IOException;

    public Optional<String> getAuditInfo(String token) throws IOException;

    public UsageCounter getUsageCounter() throws IOException;

    public class UsageException extends IOException {
        public UsageException(String message) {
            super(message);
        }
    }

}
