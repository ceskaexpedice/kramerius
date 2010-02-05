<%@ page import="java.util.*, cz.incad.Solr.CzechComparator, cz.incad.Solr.*" %>
<%@ page language="java" pageEncoding="UTF-8" %>
<%
Facet pathFacet = facets.get("path");
            if ((pathFacet != null) &&
                    (pathFacet.infos.size() > 1)){
                
                
int totalCount = 0;
int a=0;
try{
    for (int k = 0; k < pathFacet.infos.size(); k++) {
        FacetInfo facetInfo = pathFacet.infos.get(k);
        if(!facetInfo.name.contains("/")){     
            String filter = "fedora.model:\\\"info:fedora/model:" + facetInfo.name + "\\\" ";
            String pid = "";
%>
<div class="resultInTree">

    <a href='javascript:searchInTree("", "<%=filter%>", "node_<%=k%>_<%=facetInfo.name%>" );'><%=facetInfo.displayName%> (<%=facetInfo.count%>)</a>
<div class="resultsInTree" id="node_<%=k%>_<%=facetInfo.name%>"></div>
</div>
<%
            
        }
    }
    
// out.print(outText);
}catch(Exception e){
    out.write("Chyba " + e.toString());
}
}

%>