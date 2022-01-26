package cz.incad.kramerius.rest.apiNew.client.v60.redirection;

import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public abstract class RedirectHandler {

    protected String source;
    protected String pid;

    public RedirectHandler(String source, String pid) {
        this.source = source;
        this.pid = pid;
    }

    public abstract  String image() throws LexerException;
    public abstract String zoomifyImageProperties() throws LexerException;
    public abstract String zoomifyTile(String tileGroupStr, String tileStr) throws LexerException;
    public abstract String providedByLicenses() throws LexerException;

    protected String baseUrl() throws LexerException {
        PIDParser parser = new PIDParser(this.source);
        parser.objectPid();
        String objectId = parser.getObjectId();
        String baseurl = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + objectId + ".baseurl");
        return baseurl;
    }
}
