package cz.kramerius.adapters.impl.krameriusNewApi;

import cz.kramerius.shared.IoUtils;
import cz.kramerius.adapters.impl.RepositoryAccessImplAbstract;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RepositoryAccessImplByKrameriusNewApis extends RepositoryAccessImplAbstract {

    private final boolean logHttpRequests = false;
    private final String coreBaseUrl;
    private final Credentials credentials;

    public RepositoryAccessImplByKrameriusNewApis(String coreBaseUrl, Credentials credentials) {
        this.coreBaseUrl = coreBaseUrl;
        this.credentials = credentials;
    }

    private void setAuthHeaders(HttpURLConnection con) {
        con.setRequestProperty("client", credentials.client);
        con.setRequestProperty("uid", credentials.uid);
        con.setRequestProperty("access-token", credentials.accessToken);
    }

    @Override
    public boolean isObjectAvailable(String pid) throws IOException {
        //HEAD http://localhost:8080/search/api/admin/v7.0/items/uuid:4a8cf730-af36-11dd-ae88-000d606f5dc6
        URL url = new URL(coreBaseUrl + "/api/admin/v7.0/items/" + pid);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("HEAD");
        setAuthHeaders(con);
        con.setConnectTimeout(3000);
        con.setReadTimeout(5000);
        int code = con.getResponseCode();
        if (logHttpRequests) {
            System.out.println(String.format("%s %s %d", con.getRequestMethod(), url, code));
        }
        return code == 200;
    }

    @Override
    public InputStream getFoxml(String pid) throws IOException {
        //GET http://localhost:8080/search/api/admin/v7.0/items/uuid:4a8cf730-af36-11dd-ae88-000d606f5dc6/foxml
        URL url = new URL(coreBaseUrl + "/api/admin/v7.0/items/" + pid + "/foxml");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        //con.setRequestMethod("GET");
        setAuthHeaders(con);
        con.setConnectTimeout(3000);
        con.setReadTimeout(5000);
        int code = con.getResponseCode();
        if (logHttpRequests) {
            System.out.println(String.format("%s %s %d", con.getRequestMethod(), url, code));
        }
        if (code == 200) {
            return con.getInputStream();
        } else {
            throw new IOException(String.format("object %s not found or error reading it", pid));
        }
    }

    @Override
    public InputStream getDataStream(String pid, String datastreamName) throws IOException {
        //GET http://localhost:8080/search/api/admin/v7.0/items/uuid:a8263737-eb03-4107-9723-7200d00036f5/streams/BIBLIO_MODS
        URL url = new URL(coreBaseUrl + "/api/admin/v7.0/items/" + pid + "/streams/" + datastreamName);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        //con.setRequestMethod("GET");
        setAuthHeaders(con);
        con.setConnectTimeout(3000);
        con.setReadTimeout(5000);
        int code = con.getResponseCode();
        if (logHttpRequests) {
            System.out.println(String.format("%s %s %d", con.getRequestMethod(), url, code));
        }
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
        //HEAD http://localhost:8080/search/api/admin/v7.0/items/uuid:4a8cf730-af36-11dd-ae88-000d606f5dc6/streams/BIBLIO_MODS
        URL url = new URL(coreBaseUrl + "/api/admin/v7.0/items/" + pid + "/streams/" + datastreamName);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("HEAD");
        setAuthHeaders(con);
        con.setConnectTimeout(3000);
        con.setReadTimeout(5000);
        int code = con.getResponseCode();
        if (logHttpRequests) {
            System.out.println(String.format("%s %s %d", con.getRequestMethod(), url, code));
        }
        return code == 200;
    }

    @Override
    public String getDatastreamMimeType(String pid, String datastreamName) throws IOException {
        //GET http://localhost:8080/search/api/admin/v7.0/items/uuid:a8263737-eb03-4107-9723-7200d00036f5/streams/streams/IMG_FULL/mime
        URL url = new URL(coreBaseUrl + "/api/admin/v7.0/items/" + pid + "/streams/" + datastreamName + "/mime");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        //con.setRequestMethod("GET");
        setAuthHeaders(con);
        con.setConnectTimeout(3000);
        con.setReadTimeout(5000);
        int code = con.getResponseCode();
        if (logHttpRequests) {
            System.out.println(String.format("%s %s %d", con.getRequestMethod(), url, code));
        }
        if (code == 200) {
            InputStream is = null;
            is = con.getInputStream();
            return IoUtils.inputstreamToString(is);
        } else if (code == 404) {
            //not found
            return null;
        } else {
            throw new IOException(String.format("datastream %s of object %s not found or error reading it", datastreamName, pid));
        }
    }

    public static class Credentials {
        //@see cz.incad.kramerius.rest.apiNew.admin.v70.ClientAuthHeaders
        public final String client;
        public final String uid;
        public final String accessToken;

        public Credentials(String client, String uid, String accessToken) {
            this.client = client;
            this.uid = uid;
            this.accessToken = accessToken;
        }

        public String getClient() {
            return client;
        }

        public String getUid() {
            return uid;
        }

        public String getAccessToken() {
            return accessToken;
        }
    }

}
