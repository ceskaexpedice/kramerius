package cz.incad.kramerius.security;

import java.text.MessageFormat;

/**
 * Request cannot be processed
 */
public class SecurityException extends RuntimeException {
    
    public static final class SecurityExceptionInfo {

        private SecuredActions action;
        private String pid;
        private String dataStream;

        public SecurityExceptionInfo(SecuredActions action, String pid, String dataStream) {
            super();
            this.action = action;
            this.pid = pid;
            this.dataStream = dataStream;
        }
        public SecurityExceptionInfo(SecuredActions action, String pid) {
            super();
            this.action = action;
            this.pid = pid;
        }
        public SecurityExceptionInfo(SecuredActions action) {
            super();
            this.action = action;
        }
        public SecuredActions getAction() {
            return action;
        }
        public String getPid() {
            return pid;
        }
        public String getDataStream() {
            return dataStream;
        }
    }
    
    private static final long serialVersionUID = 1L;

    
    private SecurityExceptionInfo info;
    
    public SecurityException(SecurityExceptionInfo info) {
        super(constructMessage(info));
        this.info = info;
    }


    private static final String constructMessage(SecurityExceptionInfo info) {
        StringBuilder builder = new StringBuilder(MessageFormat.format("action {0} is not allowed", info.action.name()));
        if (info.getPid() != null) {
            builder.append(MessageFormat.format(", object is {0}", info.getPid()));
        }
        if (info.getDataStream() != null) {
            builder.append(MessageFormat.format(", datastream is {0}", info.getDataStream()));
        }
        return builder.toString();
    }
    
}
