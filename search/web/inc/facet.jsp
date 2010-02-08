<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false" %>
<%@ page import="java.util.*, cz.incad.Solr.CzechComparator, cz.incad.Solr.*" %>

<% 
currentFacet = facets.get(currentFacetName);
if(currentFacet!= null && currentFacet.getFacetsNumber()>0){%>
<div class="facet">
    <div class="facetTitle">
        <%=currentFacet.displayName%>
    </div>    
    <%
            try {
                if (currentFacet.name.contains("abeceda") || 
                        currentFacet.name.contains("rok") ||
                        currentFacet.name.equals("path")) {
                    Collections.sort(currentFacet.infos, new CzechComparator());
                }
                Iterator stepper = currentFacet.infos.iterator();
                while (stepper.hasNext()) {
                    FacetInfo current = (FacetInfo) stepper.next();
                    if (current != null) {
    %>
    <div>
        <a title="<fmt:message >Add_navigator</fmt:message>" 
           href="<%=current.url%>"><%=current.displayName%></a> (<%=current.count%>)
    </div>
    <%

            } else {
    %>
    <%                    }
                }
            } catch (Exception ex) {
                out.println(ex);
            }
    %>
</div>
<%}%>
