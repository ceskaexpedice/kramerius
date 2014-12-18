package cz.incad.kramerius.client.forward;

import javax.servlet.http.HttpServletRequest;


public class APIUrlPathModify implements URLPathModify{

    @Override
    public String modifyPath(String path, HttpServletRequest request) {
        int index = path.indexOf("api") + "api".length();
        String rest = path.substring(index);
        return rest;
    }

    @Override
    public String modifyQuery(String query, HttpServletRequest request) {
        return query;
    }

    
}
