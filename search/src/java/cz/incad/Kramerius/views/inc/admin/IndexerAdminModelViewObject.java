package cz.incad.Kramerius.views.inc.admin;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.resourceindex.ResourceIndexException;
import cz.incad.kramerius.resourceindex.ResourceIndexService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


public class IndexerAdminModelViewObject {

    @Inject
    Provider<HttpServletRequest> requestProvider;

    public List<Map<String,String>>  getObjects() throws InstantiationException, IllegalAccessException, ClassNotFoundException, ResourceIndexException {
        HttpServletRequest httpServletRequest = requestProvider.get();
        String sortDir = httpServletRequest.getParameterMap().containsKey("sort_dir") ? httpServletRequest.getParameter("sort_dir") : "desc";
        String sort = httpServletRequest.getParameter("sort");
        String model = httpServletRequest.getParameter("model");
        String offsetStr = httpServletRequest.getParameterMap().containsKey("offset") ? httpServletRequest.getParameter("offset") : "0";
        return ResourceIndexService.getResourceIndexImpl().getObjects(model, 20, Integer.parseInt(offsetStr), sort, sortDir);
    }

    public List<Map<String,String>>  getSearchedObjects() throws InstantiationException, IllegalAccessException, ClassNotFoundException, ResourceIndexException {
        HttpServletRequest httpServletRequest = requestProvider.get();
        String query = httpServletRequest.getParameter("s");
        String offsetStr = httpServletRequest.getParameterMap().containsKey("offset") ? httpServletRequest.getParameter("offset") : "0";
        return ResourceIndexService.getResourceIndexImpl().search(query, 20, Integer.parseInt(offsetStr));
    }

    public List<Map<String,String>>  autocompleteObjects() throws InstantiationException, IllegalAccessException, ClassNotFoundException, ResourceIndexException {
        HttpServletRequest httpServletRequest = requestProvider.get();
        String sortDir = httpServletRequest.getParameterMap().containsKey("sort_dir") ? httpServletRequest.getParameter("sort_dir") : "desc";
        String sort = httpServletRequest.getParameter("sort");
        String model = httpServletRequest.getParameter("model");
        String offsetStr = httpServletRequest.getParameterMap().containsKey("offset") ? httpServletRequest.getParameter("offset") : "0";
        return ResourceIndexService.getResourceIndexImpl().getObjects(model, 20, Integer.parseInt(offsetStr), sort, sortDir);
    }

}
