package cz.incad.kramerius.client.forward;

import javax.servlet.http.HttpServletRequest;

public class MetadataURLPathModify implements URLPathModify{

    @Override
    public String modifyPath(String path, HttpServletRequest request) {
        String p = "";
        if (path.contains("client/metadata")) {
            //client/metadata
            int index = path.indexOf("client/metadata") + "client/metadata".length();
            String rest = path.substring(index);
            p = rest + (path.endsWith("/") ? "" : "/")+"inc/details/metadata.jsp";
            
        } else {
            p = path + (path.endsWith("/") ? "" : "/")+"inc/details/metadata.jsp";
        }
        return p;
    }

    @Override
    public String modifyQuery(String query, HttpServletRequest request) {
        return query;
    }
}
