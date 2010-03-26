<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false" %>
<%@ page import="java.util.*, cz.incad.Solr.CzechComparator, cz.incad.Solr.*" %>

<% 
currentFacet = facets.get(currentFacetName);
if(currentFacet!= null && currentFacet.getFacetsNumber()>0){%>



<div id="facet_<%=currentFacetName%>" class="ui-tabs ui-widget ui-widget-content ui-corner-all" >
        <ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all" style="padding:0 0.1em 0 0;">
            <li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active " style="width:100%;"><a class="box"><fmt:message><%=currentFacet.displayName%></fmt:message></a></li>
        </ul>
        <div id="suggestBody" class="ui-tabs-panel ui-widget-content ui-corner-bottom">  
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
    <div class="facetItem">
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
        </div>
<%}%>
