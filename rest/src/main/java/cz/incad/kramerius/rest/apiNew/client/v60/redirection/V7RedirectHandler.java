package cz.incad.kramerius.rest.apiNew.client.v60.redirection;

import cz.incad.kramerius.utils.pid.LexerException;

public class V7RedirectHandler extends RedirectHandler{

    public V7RedirectHandler(String source, String pid) {
        super(source, pid);
    }

    @Override
    public String image() throws LexerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image";
        return url;
    }

    @Override
    public String zoomifyImageProperties() throws LexerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image/zoomify/ImageProperties.xml";
        return url;
    }

    @Override
    public String zoomifyTile(String tileGroupStr, String tileStr) throws LexerException {
        String formatted = String.format("image/zoomify/%s/%s", tileGroupStr, tileStr);
        //String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + pid + "/" + endpoint;
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/"+formatted;
        return url;
    }

    @Override
    public String providedByLicenses() throws LexerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info/providedByLicenses";
        return url;
    }
}
