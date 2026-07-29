package cz.incad.kramerius.processes.notifications;

public class GenerationNotification {

    private final String pid;
    private final String user;
    private final String email;
    private final String documentType;
    private final String filename;
    private final String downloadToken;
    private final String downloadCDKToken;
    private final String downloadUrl;
    private final String title;
    private final String source;

    private GenerationNotification(Builder builder) {
        this.pid = builder.pid;
        this.user = builder.user;
        this.email = builder.email;
        this.documentType = builder.documentType;
        this.filename = builder.filename;
        this.downloadToken = builder.downloadToken;
        this.downloadCDKToken = builder.downloadCDKToken;
        this.downloadUrl = builder.downloadUrl;
        this.title = builder.title;
        this.source = builder.source;
    }

    public String getPid() {
        return pid;
    }

    public String getUser() {
        return user;
    }

    public String getEmail() {
        return email;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getFilename() {
        return filename;
    }

    public String getDownloadToken() {
        return downloadToken;
    }

    public String getDownloadCDKToken() {
        return downloadCDKToken;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public static class Builder {
        private String pid;
        private String user;
        private String email;
        private String documentType;
        private String filename;
        private String downloadToken;
        private String downloadCDKToken;
        private String downloadUrl;
        private String title;
        private String source;


        public Builder pid(String pid) {
            this.pid = pid;
            return this;
        }

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder documentType(String documentType) {
            this.documentType = documentType;
            return this;
        }

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder downloadToken(String downloadToken) {
            this.downloadToken = downloadToken;
            return this;
        }

        public Builder downloadCDKToken(String downloadCDKToken) {
            this.downloadCDKToken = downloadCDKToken;
            return this;
        }

        public Builder downloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public GenerationNotification build() {
            return new GenerationNotification(this);
        }
    }
}
