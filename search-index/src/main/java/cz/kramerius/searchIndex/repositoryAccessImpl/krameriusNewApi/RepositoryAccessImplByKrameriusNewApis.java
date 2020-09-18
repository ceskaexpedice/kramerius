package cz.kramerius.searchIndex.repositoryAccessImpl.krameriusNewApi;

import cz.kramerius.searchIndex.repositoryAccess.Utils;
import cz.kramerius.searchIndex.repositoryAccessImpl.RepositoryAccessImplAbstract;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RepositoryAccessImplByKrameriusNewApis extends RepositoryAccessImplAbstract {

    public final String coreBaseUrl;
    private boolean logHttpRequests = false;

    public RepositoryAccessImplByKrameriusNewApis(String coreBaseUrl) {
        this.coreBaseUrl = coreBaseUrl;
    }

    @Override
    public boolean isObjectAvailable(String pid) throws IOException {
        //HEAD http://localhost:8080/search/api/client/v6.0/items/uuid:4a8cf730-af36-11dd-ae88-000d606f5dc6
        URL url = new URL(coreBaseUrl + "/api/client/v6.0/items/" + pid);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("HEAD");
        con.setConnectTimeout(3000);
        con.setReadTimeout(5000);
        if (logHttpRequests) {
            System.out.println("HEAD " + url.toString());
        }
        int code = con.getResponseCode();
        //System.out.println("response code: " + code);
        return code == 200;
    }

    @Override
    public InputStream getFoxml(String pid) throws IOException {
        //TODO: auth
        //GET http://localhost:8080/search/api/admin/v1.0/items/uuid:4a8cf730-af36-11dd-ae88-000d606f5dc6/foxml
        URL url = new URL(coreBaseUrl + "/api/admin/v1.0/items/" + pid + "/foxml");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        //con.setRequestMethod("GET");
        con.setConnectTimeout(3000);
        con.setReadTimeout(5000);
        if (logHttpRequests) {
            System.out.println("GET " + url.toString());
        }
        int code = con.getResponseCode();
        //System.out.println("response code: " + code);
        if (code == 200) {
            return con.getInputStream();
        } else {
            throw new IOException(String.format("object %s not found or error reading it", pid));
        }
    }

    @Override
    public InputStream getDataStream(String pid, String datastreamName) throws IOException {
        //TODO: auth
        //GET http://localhost:8080/search/api/admin/v1.0/items/uuid:a8263737-eb03-4107-9723-7200d00036f5/streams/BIBLIO_MODS
        URL url = new URL(coreBaseUrl + "/api/admin/v1.0/items/" + pid + "/streams/" + datastreamName);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        //con.setRequestMethod("GET");
        con.setConnectTimeout(3000);
        con.setReadTimeout(5000);
        if (logHttpRequests) {
            System.out.println("GET " + url.toString());
        }
        int code = con.getResponseCode();
        //System.out.println("response code: " + code);
        if (code == 200) {
            InputStream is = null;
            is = con.getInputStream();
            return is;
        } else if (code == 404) {
            //not found
            return null;
        } else {
            throw new IOException(String.format("datastream %s of object %s not found or error reading it", datastreamName, pid));
        }
    }

    @Override
    public boolean isStreamAvailable(String pid, String datastreamName) throws IOException {
        //TODO: auth
        //HEAD http://localhost:8080/search/api/admin/v1.0/items/uuid:4a8cf730-af36-11dd-ae88-000d606f5dc6/streams/BIBLIO_MODS
        URL url = new URL(coreBaseUrl + "/api/admin/v1.0/items/" + pid + "/streams/" + datastreamName);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        //con.setRequestMethod("HEAD");
        con.setConnectTimeout(3000);
        con.setReadTimeout(5000);
        if (logHttpRequests) {
            System.out.println("HEAD " + url.toString());
        }
        int code = con.getResponseCode();
        //System.out.println("response code: " + code);
        return code == 200;
    }

    @Override
    public String getDatastreamMimeType(String pid, String datastreamName) throws IOException {
        //TODO: auth
        //GET http://localhost:8080/search/api/admin/v1.0/items/uuid:a8263737-eb03-4107-9723-7200d00036f5/streams/streams/IMG_FULL/mime
        URL url = new URL(coreBaseUrl + "/api/admin/v1.0/items/" + pid + "/streams/" + datastreamName + "/mime");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        //con.setRequestMethod("GET");
        con.setConnectTimeout(3000);
        con.setReadTimeout(5000);
        if (logHttpRequests) {
            System.out.println("GET " + url.toString());
        }
        int code = con.getResponseCode();
        //System.out.println("response code: " + code);
        if (code == 200) {
            InputStream is = null;
            is = con.getInputStream();
            return Utils.inputstreamToString(is);
        } else if (code == 404) {
            //not found
            return null;
        } else {
            throw new IOException(String.format("datastream %s of object %s not found or error reading it", datastreamName, pid));
        }
    }

}
