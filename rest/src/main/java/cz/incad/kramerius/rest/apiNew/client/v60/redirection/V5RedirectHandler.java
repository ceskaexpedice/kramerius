package cz.incad.kramerius.rest.apiNew.client.v60.redirection;

import cz.incad.kramerius.utils.pid.LexerException;

public class V5RedirectHandler extends RedirectHandler{

    public V5RedirectHandler(String source, String pid) {
        super(source, pid);
    }

    @Override
    public String image() throws LexerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams/IMG_FULL";
        return url;
    }

    @Override
    public String zoomifyImageProperties() throws LexerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "zoomify/" + this.pid + "/ImageProperties.xml";
        return url;
    }

    @Override
    public String zoomifyTile(String tileGroupStr, String tileStr) throws LexerException {
        String baseurl = baseUrl();
        String formatted = String.format("zoomify/%s/%s/%s", this.pid,  tileGroupStr, tileStr);
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + formatted;
        return url;
    }

    @Override
    public String providedByLicenses() throws LexerException {
        return "";
    }
}
