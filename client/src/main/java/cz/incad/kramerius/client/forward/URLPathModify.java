package cz.incad.kramerius.client.forward;

import javax.servlet.http.HttpServletRequest;

public interface URLPathModify {

    public String modifyPath(String path, HttpServletRequest request);
    
    public String modifyQuery(String query, HttpServletRequest request);
}
