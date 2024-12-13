package cz.incad.kramerius.services.iterators.compare;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.utils.IOUtils;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public enum ES {

    INDEX {

        private String bulkObject(String indexName, JSONObject item, boolean esType) {
            StringBuilder builder = new StringBuilder();

            JSONObject indexItemDesc = new JSONObject();
            JSONObject itemDesc = new JSONObject();
            itemDesc.put("_index", indexName);

            itemDesc.put("_id", item.getString("pid"));
            if (esType) {
                itemDesc.put("_type", "_doc");
            }

            indexItemDesc.put("index", itemDesc);

            builder.append(indexItemDesc.toString()).append("\n");
            builder.append(item.toString());
            return builder.toString();
        }

        @Override
        protected String request(Client client, String esAddress, String indexName, List<JSONObject> page, boolean esType) {
            String collect = page.stream().map(it -> {
                return bulkObject(indexName, it, esType);
            }).collect(Collectors.joining("\n"))+"\n";
            return toEs(client,esAddress,collect);
        }
    },

    DELETE {

        private String bulkObject(String indexName, JSONObject item, boolean esType) {
            StringBuilder builder = new StringBuilder();
            JSONObject indexItemDesc = new JSONObject();
            JSONObject itemDesc = new JSONObject();
            itemDesc.put("_index", indexName);
            itemDesc.put("_id", item.getString("pid"));
            if (esType) {
                itemDesc.put("_type", "_doc");
            }

            indexItemDesc.put("delete", itemDesc);

            builder.append(indexItemDesc.toString()).append("\n");
            return builder.toString();
        }
        @Override
        protected String request(Client client,String esAddress, String indexName, List<JSONObject> page, boolean esType) {
            String collect = page.stream().map(it -> {
                return bulkObject(indexName, it, esType);
            }).collect(Collectors.joining("\n"))+"\n";
            return toEs(client,esAddress,collect);
        }
    };

    protected abstract String request(Client client, String esAddress, String indexName, List<JSONObject> page, boolean esType);



    public static String toEs(Client client,String esAddress, String payload) {
            try {
                WebResource r = client.resource(esAddress+(esAddress.endsWith("/") ? "_bulk" :  "/_bulk"));
                ClientResponse resp = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(payload, MediaType.APPLICATION_JSON).post(ClientResponse.class);
                if (resp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    InputStream entityInputStream = resp.getEntityInputStream();
                    IOUtils.copyStreams(entityInputStream, bos);
                    return new String(bos.toByteArray(), "UTF-8");

                } else {

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    InputStream entityInputStream = resp.getEntityInputStream();
                    IOUtils.copyStreams(entityInputStream, bos);
                    return new String(bos.toByteArray(), "UTF-8");
                }
            } catch (UniformInterfaceException | ClientHandlerException | IOException e) {
                CompareLogsIterator.LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e);
            }
    }

    protected static void toEs(Client client, String indexName, List<JSONObject> page, boolean esType) {
    }
}
