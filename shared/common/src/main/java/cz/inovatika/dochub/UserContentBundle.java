package cz.inovatika.dochub;

import java.time.Instant;

public class UserContentBundle {

    private final String token;
    private final String pid;
    private final DocumentType type;
    private final Instant created;
    private final long size;
    private final boolean available;

    public UserContentBundle(String token, String pid, DocumentType type, Instant created, long size) {
        this(token, pid, type, created, size, true);
    }

    public UserContentBundle(String token, String pid, DocumentType type, Instant created, long size, boolean available) {
        this.token = token;
        this.pid = pid;
        this.type = type;
        this.created = created;
        this.size = size;
        this.available = available;
    }

    public String getToken() {
        return token;
    }

    public String getPid() {
        return pid;
    }

    public DocumentType getType() {
        return type;
    }

    public Instant getCreated() {
        return created;
    }

    public long getSize() {
        return size;
    }

    public boolean isAvailable() {
        return available;
    }
}
