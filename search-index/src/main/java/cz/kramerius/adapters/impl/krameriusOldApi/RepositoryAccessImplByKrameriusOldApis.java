package cz.kramerius.adapters.impl.krameriusOldApi;

import cz.kramerius.adapters.impl.RepositoryAccessImplAbstract;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RepositoryAccessImplByKrameriusOldApis extends RepositoryAccessImplAbstract {

    public final String coreBaseUrl;

    public RepositoryAccessImplByKrameriusOldApis(String coreBaseUrl) {
        this.coreBaseUrl = coreBaseUrl;
    }

    @Override
    public boolean isObjectAvailable(String pid) throws IOException {
        //HEAD http://localhost:8080/search/api/v5.0/item/uuid:4a8cf730-af36-11dd-ae88-000d606f5dc6
        URL url = new URL(coreBaseUrl + "/api/v5.0/item/" + pid);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("HEAD");
        con.setConnectTimeout(3000);
        con.setReadTimeout(5000);
        //System.out.println("HEAD " + url.toString());
        int code = con.getResponseCode();
        return code == 200;
    }

    @Override
    public InputStream getFoxml(String pid) throws IOException {
        //GET http://localhost:8080/search/api/v5.0/item/uuid:4a8cf730-af36-11dd-ae88-000d606f5dc6/foxml
        URL url = new URL(coreBaseUrl + "/api/v5.0/item/" + pid + "/foxml");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(3000);
        con.setReadTimeout(5000);
        //System.out.println("GET " + url.toString());
        int code = con.getResponseCode();
        if (code == 200) {
            return con.getInputStream();
        } else {
            throw new IOException(String.format("object %s not found or error reading it", pid));
        }
    }

    @Override
    public InputStream getDataStream(String pid, String datastreamName) throws IOException {
        //GET http://localhost:8080/search/api/v5.0/item/uuid:a8263737-eb03-4107-9723-7200d00036f5/streams/dsName
        URL url = new URL(coreBaseUrl + "/api/v5.0/item/" + pid + "/streams/" + datastreamName);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(3000);
        con.setReadTimeout(5000);
        //System.out.println("GET " + url.toString());
        int code = con.getResponseCode();
        //System.out.println("http response code: " + code);
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
        //inefficient implementation
        InputStream data = getDataStream(pid, datastreamName);
        return data != null;

        //correct implementation (HEAD, no need to send data), but not implemented in API
        //TODO: enable
        /*//HEAD http://localhost:8080/search/api/v5.0/item/uuid:a8263737-eb03-4107-9723-7200d00036f5/streams/dsName
        URL url = new URL(coreBaseUrl + "/api/v5.0/item/" + pid + "/streams/" + datastreamName);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("HEAD");
        con.setConnectTimeout(3000);
        con.setReadTimeout(5000);
        System.out.println("HEAD " + url.toString());
        int code = con.getResponseCode();
        return code == 200;*/
    }

}
