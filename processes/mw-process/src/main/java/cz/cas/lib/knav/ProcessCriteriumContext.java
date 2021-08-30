package cz.cas.lib.knav;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.*;

/**
 * Context for process
 * @author pavels
 */
public class ProcessCriteriumContext implements RightCriteriumContext {

    private String pid;
    private FedoraAccess fa;
    private SolrAccess sa;
    private Map<String, String> map = new HashMap<>();
    
    public ProcessCriteriumContext(String pid, FedoraAccess fa, SolrAccess sa) {
        super();
        this.pid = pid;
        this.fa = fa;
        this.sa = sa;
    }

    @Override
    public String getRequestedPid() {
        return this.pid;
    }

    @Override
    public String getRequestedStream() {
        throw new UnsupportedOperationException("unsupported for this context");
    }

    @Override
    public String getAssociatedPid() {
        return this.pid;
    }

    @Override
    public void setAssociatedPid(String uuid) {
        throw new UnsupportedOperationException("unsupported for this context");
    }

    @Override
    public ObjectPidsPath[] getPathsToRoot() {
        try {
            return this.sa.getPath(getRequestedPid());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public User getUser() {
        throw new UnsupportedOperationException("unsupported for this context");
    }

    @Override
    public FedoraAccess getFedoraAccess() {
        return this.fa;
    }

    @Override
    public SolrAccess getSolrAccess() {
        return this.sa;
    }

    @Override
    public UserManager getUserManager() {
        throw new UnsupportedOperationException("unsupported for this context");
    }

    @Override
    public String getRemoteHost() {
        throw new UnsupportedOperationException("unsupported for this context");
    }

    @Override
    public String getRemoteAddr() {
        throw new UnsupportedOperationException("unsupported for this context");
    }

    @Override
    public SecuredActions getAction() {
        throw new UnsupportedOperationException("unsupported for this context");
    }

    @Override
    public RightsResolver getRightsResolver() {
        throw new UnsupportedOperationException("unsupported for this context");
    }

    @Override
    public Map<String, String> getEvaluateInfoMap() {
        return this.map;
    }
}
